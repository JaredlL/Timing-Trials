package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.android.jared.linden.timingtrials.domain.JsonResultsWriter
import com.android.jared.linden.timingtrials.domain.TimeTrialIO
import com.android.jared.linden.timingtrials.domain.TimingTrialsExport
import com.android.jared.linden.timingtrials.domain.csv.LineToCourseConverter
import com.android.jared.linden.timingtrials.domain.csv.LineToResultRiderConverter
import com.android.jared.linden.timingtrials.domain.csv.LineToTimeTrialConverter
import com.android.jared.linden.timingtrials.util.ConverterUtils
import com.android.jared.linden.timingtrials.util.Event
import com.google.gson.Gson
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.ICSVParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import java.io.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipInputStream
import javax.inject.Inject


class IOViewModel @Inject constructor(private val riderRespository: IRiderRepository,
                                      private val courseRepository: ICourseRepository,
                                      private val timeTrialRepository: ITimeTrialRepository,
                                      private val  timeTrialRiderRepository: TimeTrialRiderRepository): ViewModel() {


    val writeAllResult: MutableLiveData<Event<String>> = MutableLiveData()

    val isIoInProgress = AtomicBoolean()

    fun writeAllTimeTrialsToPath(outputStream: OutputStream){
        viewModelScope.launch(Dispatchers.IO) {
            if(isIoInProgress.compareAndSet(false, true)){
                try {
                    val allTts = timeTrialRepository.allTimeTrials()
                    JsonResultsWriter().writeToPath(outputStream, allTts)
                    writeAllResult.postValue(Event("Success"))
                }catch (e:Exception){
                    writeAllResult.postValue(Event(e.message?:"Error"))
                }finally {
                    isIoInProgress.set(false)
                }
            }

        }
    }

    fun readInput(title: String?, inputStream: InputStream){
        viewModelScope.launch(Dispatchers.IO) {
            if(isIoInProgress.compareAndSet(false, true)){
                try {
                    val buf = BufferedInputStream(inputStream)
                    buf.mark(100)
                    val zis = ZipInputStream(buf)
                    val nextZipped = zis.nextEntry
                    var fString = ""
                    if(nextZipped!= null){
                        //val reader2 = BufferedReader(BufferedInputStream(zis))
                        val sb = StringBuilder()
                        val buffer = ByteArray(1024)
                        var read = 0

                        while (zis.read(buffer, 0, 1024).also { read = it } >= 0) {
                            //read = zis.read(buffer, 0, 1024)
                            sb.append(String(buffer, 0, read))
                        }
                        fString = sb.toString()
                        zis.close()
                    }else{
                        buf.reset()
                        val reader = BufferedReader(InputStreamReader(buf))
                        fString = reader.readText()
                        reader.close()
                    }


                    //reader.close()
                    val firstNonWhitespaceChar = fString.asSequence().first { !it.isWhitespace() }

                    val msg = if(firstNonWhitespaceChar == "{".first() || firstNonWhitespaceChar == "[".first()){
                        readJsonInputIntoDb(fString)
                    }else{
                        readCsvInputIntoDb(title?:"TimeTrial", fString)
                    }

                    importMessage.postValue(Event(msg))
                }catch (e: Exception){
                    importMessage.postValue(Event(e.message?:"Error reading file: ${e.message}"))
                }finally {
                    isIoInProgress.set(false)
                }
            }


            //readCsvInputIntoDb(inputStream)
        }
    }

    val READING_TT = 0
    val READING_COURSE = 1
    val READING_RIDER = 2
    val READING_CTT_RIDER = 3

    val importMessage: MutableLiveData<Event<String>> = MutableLiveData<Event<String>>()

    private suspend fun readJsonInputIntoDb(fileString: String): String{
       return try{
            val result = Gson().fromJson(fileString, TimingTrialsExport::class.java)
           for(tt in result.timingTrialsData){
               addImportTtToDb(tt)
           }
           "Imported ${result.timingTrialsData.count()}"

        }catch (e:Exception){
            "Failed to parse JSON file: ${e.message}"
        }
    }


    private suspend fun readCsvInputIntoDb(fileName: String, fileContents: String): String{
        try{
            val lineToTt = LineToTimeTrialConverter()
            val lineToCourse = LineToCourseConverter()
            val lineToRider = LineToResultRiderConverter()

            val timeTrialList: MutableList<TimeTrialIO> = mutableListOf()
            var state = READING_TT

            val allLines = CSVReader(StringReader(fileContents)).readAll()
            for  (fileLine in allLines){

                val currentLine = fileLine.joinToString()
                val cLineList = fileLine.toList()
                if(lineToTt.isHeading(currentLine)){
                    lineToTt.setHeading(currentLine)
                    timeTrialList.add(TimeTrialIO())
                    state = READING_TT

                }else if (lineToCourse.isHeading(currentLine)){
                    lineToCourse.setHeading(currentLine)
                    state = READING_COURSE

                }else if (lineToRider.isHeading(currentLine)){
                    lineToRider.setHeading(currentLine)
                    if(timeTrialList.isEmpty()){
                        timeTrialList.add(TimeTrialIO(timeTrialHeader = lineToTt.fromCttTitle(fileName)))
                    }
                    state = READING_RIDER
                }
                else{
                    when(state){
                        READING_TT->{
                            val tt = lineToTt.importLine(cLineList)
                            tt?.let {
                                timeTrialList.lastOrNull()?.timeTrialHeader = it

                            }
                        }
                        READING_COURSE->{
                            val course = lineToCourse.importLine(cLineList)
                            course?.let {
                                timeTrialList.lastOrNull()?.course = it
                            }
                        }
                        READING_RIDER->{
                            lineToRider.importLine(cLineList)?.let {
                                timeTrialList.lastOrNull()?.timeTrialRiders?.add(it)
                            }
                        }
                    }
                }
            }

            timeTrialList.forEach {
                addImportTtToDb(it)
            }
            return "Imported ${timeTrialList.count()} timetrials"
        }catch (e:Exception){
            return "Failed to import csv data ${e.message}"
        }


    }

    suspend fun  addImportTtToDb(importTt: TimeTrialIO){
        val header = importTt.timeTrialHeader
        val course = importTt.course

        var headerInDb: TimeTrialHeader? = null
        var courseInDb: Course? = null

        if(course != null && course.courseName.isNotBlank()){

            val courseList = if(course.cttName.isNotBlank()){
                courseRepository.getCoursesByName(course.courseName)
            }else{
                courseRepository.getCoursesByName(course.courseName).filter { it.cttName == course.cttName }
            }

            when(courseList.size){
                0->{
                    val id = courseRepository.insert(course)
                    courseInDb = courseRepository.getCourseSuspend(id)
                }
                1->{
                    courseInDb = courseList.first()
                }
                else-> courseInDb = courseList.minBy { it.length - course.length }!!
            }

        }

        if(header != null){
            val headerName = if(header.ttName.isNotBlank()){
                header.ttName
            }else{
                val c = courseInDb
                if (header.startTime != OffsetDateTime.MIN && c != null){
                    "${c.courseName} ${ConverterUtils.dateToDisplay(header.startTime)}"
                }else if(c != null){
                    "${c.courseName} TT"
                }else if(header.startTime != OffsetDateTime.MIN){
                    "${ConverterUtils.dateToDisplay(header.startTime)} TT"
                }else{
                    "Unknown TT"
                }
            }
           val status = when {
               importTt.timeTrialRiders.all { it.finishTime == null } -> {
                   TimeTrialStatus.SETTING_UP
               }
               importTt.timeTrialRiders.all { it.startTime == null } -> {
                   TimeTrialStatus.FINISHED
               }
               else -> {
                   header.status
               }
           }
            val firstRider = importTt.timeTrialRiders.firstOrNull()
            var firstRiderStartOffset = header.firstRiderStartOffset
            var startTime = header.startTime
            if(status == TimeTrialStatus.SETTING_UP && firstRider != null) {
                firstRider.startTime?.let {
                    startTime = OffsetDateTime.of(header.startTime.year, header.startTime.monthValue, header.startTime.dayOfMonth, it.hour, it.minute - 1, it.second, 0, ZoneId.systemDefault().rules.getOffset(Instant.now()))
                    firstRiderStartOffset = 60
                }
            }

            var numberRules = header.numberRules
            var interval = header.interval
            val secondRider = importTt.timeTrialRiders.getOrNull(1)
            if(firstRider != null && secondRider != null){
                val fst = firstRider.startTime
                val sst = secondRider.startTime
                if(fst != null && sst != null){
                    interval = sst.toSecondOfDay() - fst.toSecondOfDay()
                }
                var firstBib = 1
                var direction = NumbersDirection.ASCEND
                if(firstRider.bib != null && secondRider.bib != null){
                    firstBib = firstRider.bib
                    direction = if(secondRider.bib < firstRider.bib){
                        NumbersDirection.DESCEND
                    }else{
                        NumbersDirection.ASCEND
                    }
                }
                numberRules = NumberRules(firstBib, true, direction, listOf())
            }

            val headerToInsert = header.copy(
                    ttName = headerName,
                    notes = header.notes,
                    courseId = courseInDb?.id,
                    status = status,
                    interval = interval,
                    firstRiderStartOffset = firstRiderStartOffset,
                    numberRules = numberRules,
            startTime = startTime)

            val headerList = timeTrialRepository.getHeadersByName(headerName)

            headerInDb = when(headerList.size){
                0->{
                    val id = timeTrialRepository.insertNewHeader(headerToInsert)
                    headerToInsert.copy(id = id)
                }
                1->{
                    headerList.first()
                }
                else->{
                    val onCourseList = headerList.filter { it.courseId == courseInDb?.id }.sortedBy { it.startTimeMilis - headerToInsert.startTimeMilis }
                    onCourseList.firstOrNull()

                }
            }

        }
        importTt.timeTrialRiders.asSequence().filter { it.firstName.isNotBlank() }.sortedBy { it.bib?:it.startTime?.toSecondOfDay()?:0 }.forEachIndexed { index, importRider ->

            val existingRiders = riderRespository.ridersFromFirstLastName(importRider.firstName, importRider.lastName)
            //var timeTrialRider:TimeTrialRider? = null
            val riderInDbId:Rider = when(existingRiders.size){
                0->{
                    val newRider = Rider(importRider.firstName, importRider.lastName, importRider.club, null, importRider.category?:"", importRider.gender)
                    val id = riderRespository.insert(newRider)
                    newRider.copy(id = id)
                }
                1->{
                    existingRiders.first()
                }
                else->{
                    val byGender = existingRiders.filter { importRider.gender!= Gender.UNKNOWN && it.gender == importRider.gender }
                    if(byGender.isEmpty()){
                        val newRider = Rider(importRider.firstName, importRider.lastName, importRider.club, null, importRider.category, importRider.gender)
                        val id = riderRespository.insert(newRider)
                        newRider.copy(id = id)
                    }else{
                        byGender.first()
                    }
                }
            }

            riderInDbId.id?.let {
                val gen = if(importRider.gender != Gender.UNKNOWN)
                {
                    importRider.gender}
                else{
                    riderInDbId.gender
                }


                    val timeTrialRider = TimeTrialRider(
                            riderId = it,
                            timeTrialId = headerInDb?.id,
                            courseId = courseInDb?.id,
                            index = index,
                            finishTime = if (headerInDb?.status == TimeTrialStatus.FINISHED) importRider.finishTime else null,
                            splits = if (headerInDb?.status == TimeTrialStatus.FINISHED) transformSplits(importRider.splits) else listOf(),
                            category = importRider.category,
                            gender = gen,
                            club = importRider.club,
                            resultNote = importRider.notes
                    )
                    insertTimeTrialRider(timeTrialRider)


            }



        }

    }

    fun transformSplits(splits: List<Long>): List<Long>{

        if(splits.size <= 1) return splits

        var newSplits: MutableList<Long> = mutableListOf()

        var current = splits.first()
        newSplits.add(current)
        for (s in splits.drop(1)){
            current += s
            newSplits.add(current)
        }
        return newSplits

    }

    suspend fun insertTimeTrialRider(timeTrialRider: TimeTrialRider){

        val riderId = timeTrialRider.riderId
        val timeTrialId = timeTrialRider.timeTrialId

        if(timeTrialId != null){
            val existing = timeTrialRiderRepository.getByRiderTimeTrialIds(riderId, timeTrialId)

            when(existing.size){
                0->{
                    val id = timeTrialRiderRepository.insert(timeTrialRider)
                }
                1->{

                }
                else -> throw Exception("Multi Riders")
            }

        }

    }

}







