package com.jaredlinden.timingtrials.viewdata

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.roomrepo.ICourseRepository
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.domain.JsonResultsWriter
import com.jaredlinden.timingtrials.domain.TimeTrialIO
import com.jaredlinden.timingtrials.domain.TimingTrialsExport
import com.jaredlinden.timingtrials.domain.csv.LineToCourseConverter
import com.jaredlinden.timingtrials.domain.csv.LineToResultRiderConverter
import com.jaredlinden.timingtrials.domain.csv.LineToTimeTrialConverter
import com.jaredlinden.timingtrials.util.ConverterUtils
import com.jaredlinden.timingtrials.util.Event
import com.google.gson.Gson
import com.jaredlinden.timingtrials.domain.csv.LineToCompleteResult
import com.jaredlinden.timingtrials.functors.Either
import com.jaredlinden.timingtrials.functors.Left
import com.jaredlinden.timingtrials.functors.Right
import com.opencsv.CSVReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.threeten.bp.*
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.security.InvalidParameterException
import java.util.zip.ZipInputStream
import javax.inject.Inject

@HiltViewModel
class IOViewModel @Inject constructor(private val riderRepository: IRiderRepository,
                                      private val courseRepository: ICourseRepository,
                                      private val timeTrialRepository: ITimeTrialRepository,
                                      private val timeTrialRiderRepository: TimeTrialRiderRepository): ViewModel() {

    val allResultsWrittenEvent: MutableLiveData<Event<Boolean>> = MutableLiveData()
    private val ioLock = Mutex()

    fun writeAllTimeTrialsToPath(outputStream: OutputStream){
        viewModelScope.launch(Dispatchers.IO) {
            ioLock.withLock {
                try {
                    val allTts = timeTrialRepository.allTimeTrials()
                    if(allTts.any()){
                        JsonResultsWriter().writeToPath(outputStream, allTts)
                        allResultsWrittenEvent.postValue(Event(true))
                    }else{
                        importMessage.postValue(Event(Right("Nothing to export")))
                    }
                } catch (e: Exception) {
                    importMessage.postValue(Event(Left(Pair("Error writing results", e))))
                }
            }
        }
    }

    fun readInput(inputStream: InputStream){
        readInput(null,null, inputStream)
    }

    fun readUrlInput(url:URL){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val connection: URLConnection = url.openConnection()
                connection.connect()
                val input: InputStream = BufferedInputStream(url.openStream(), 8192)
                readInput(input)
            }catch (e:Exception){
                val left = Left(Pair("Error accessing URL", e))
                importMessage.postValue(Event(left))
            }
        }
    }

    val readList: MutableList<Uri> = mutableListOf()

    fun readInput(title: String?, uri: Uri?, inputStream: InputStream){

        //To prevent reading in the same file multiple times
        if(!readList.contains(uri)){
            viewModelScope.launch(Dispatchers.IO) {
                ioLock.withLock{
                    try {
                        val bufferedInputStream = BufferedInputStream(inputStream)
                        bufferedInputStream.mark(100)
                        val zipInputStream = ZipInputStream(bufferedInputStream)
                        val nextZipped = zipInputStream.nextEntry
                        var fString = ""
                        if(nextZipped!= null){
                            val sb = StringBuilder()
                            val buffer = ByteArray(1024)
                            var read = 0

                            while (zipInputStream.read(buffer, 0, 1024).also { read = it } >= 0)
                            {
                                sb.append(String(buffer, 0, read))
                            }
                            fString = sb.toString()
                            zipInputStream.close()
                        }else{
                            bufferedInputStream.reset()
                            val reader = BufferedReader(InputStreamReader(bufferedInputStream))
                            fString = reader.readText()
                            reader.close()
                        }

                        val firstNonWhitespaceChar = fString.asSequence().first { !it.isWhitespace() }

                        val msg = if(firstNonWhitespaceChar == "{".first() || firstNonWhitespaceChar == "[".first()){
                            readJsonInputIntoDb(fString)
                        }else{
                            readCsvInputIntoDb(title?:"Time Trial", fString)
                        }

                        importMessage.postValue(Event(msg))
                    }
                    catch (e: Exception)
                    {
                        val left = Left(Pair("Error reading file", e))
                        importMessage.postValue(Event(left))
                    }
                }
            }
        }
    }

    val READING_TT = 0
    val READING_COURSE = 1
    val READING_RIDER = 2
    val READING_CTT_RIDER = 3
    val READING_COMPLETE_RESULT_HEADING = 4
    val READING_COMPLETE_RESULT = 5

    val importMessage: MutableLiveData<Event<Either<Pair<String, Exception>,String>>> = MutableLiveData()

    private suspend fun readJsonInputIntoDb(fileString: String): Either<Pair<String, Exception>,String>{

        return try{
            val result = Gson().fromJson(fileString, TimingTrialsExport::class.java)
            val numAdded = result.timingTrialsData.map { addImportTtToDb(it) }.filter { it }.count()
            if(numAdded > 0){
                Right("Imported $numAdded")
            }else{
                Right("Found no new time trials to import")
            }
        }catch (e:Exception){
            Left(Pair("Failed to parse JSON file", e))
        }
    }

    fun isCttStartsheet(fileName:String):Boolean{
        return fileName.contains("startsheet-")
    }

    private suspend fun readCsvInputIntoDb(fileName: String, fileContents: String): Either<Pair<String, Exception>,String>{
        try{
            val lineToTt = LineToTimeTrialConverter()
            val lineToCourse = LineToCourseConverter()
            val lineToRider = LineToResultRiderConverter()
            val lineToCompleteResult = LineToCompleteResult()


            val timeTrialList: MutableList<TimeTrialIO> = mutableListOf()
            val completeRowlist: MutableList<CompleteInformationRow> = mutableListOf()
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
                else if(lineToCompleteResult.isHeading(currentLine)){
                    //lineToCompleteResult.setHeading(currentLine)
                    state = READING_COMPLETE_RESULT_HEADING
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
                        READING_COMPLETE_RESULT_HEADING->{
                            lineToCompleteResult.setHeading(currentLine)
                            state = READING_COMPLETE_RESULT

                        }
                        READING_COMPLETE_RESULT->{
                            lineToCompleteResult.importLine(cLineList)?.let {
                                completeRowlist.add(it)
                            }
                        }
                    }
                }
            }

            val numImported = timeTrialList.map {  addImportTtToDb(it) }
            val rows = addCompleteListToDb(completeRowlist)
            val num = numImported.filter { it }.count()
           return if(num > 0){
               Right("Imported $num time trials")
           }else if(rows > 0){
               Right("Imported $rows rows")
           }else{
               Right("Found no new time trials to import")
           }

        }catch (e:Exception){
            return Left(Pair("Failed to import csv data", e))
        }
    }

    suspend fun addCompleteListToDb(completeList: List<CompleteInformationRow>): Int{
        return completeList.filter { addCompleteToDb(it) }.size
    }

    suspend fun addCompleteToDb(row: CompleteInformationRow): Boolean{
        val rider = row.rider()
        val course = row.course()
        val header = row.timeTrialHeader()
        if(rider != null && course != null && header != null){

            val dbCourse = getDbCourse(course)
            val dbRider = getDbRider(rider)
            val insertHeader = header.copy(courseId = dbCourse.id)
            val ttId = when{
                row.timeTrialDate != null ->{
                    getTimeTrialByCourseDate(dbCourse, row.timeTrialDate)?.id?:timeTrialRepository.insertNewHeader(insertHeader)
                }
                row.timeTrialName != null->{
                    val headerList = timeTrialRepository.getHeadersByName(row.timeTrialName)
                    when(headerList.size){
                        0->{
                            timeTrialRepository.insertNewHeader(insertHeader)
                        }
                        1->{
                            headerList.first().id
                        }
                        else->{
                            val onCourseList = headerList.filter { it.courseId == dbCourse.id }.sortedBy { it.startTimeMillis - insertHeader.startTimeMillis }
                            onCourseList.firstOrNull()?.id
                        }
                    }
                }
               else -> null
            }
            if(ttId != null && dbRider.id != null && dbCourse.id != null){
                val fullTimeTrial = timeTrialRepository.getResultTimeTrialByIdSuspend(ttId)
                if(!fullTimeTrial.riderList.any { it.riderId() == dbRider.id}){

                    val timeTrialRider = TimeTrialRider(
                            riderId = dbRider.id,
                            timeTrialId = ttId,
                            courseId = dbCourse.id,
                            index = 0,
                            finishCode = if (fullTimeTrial.timeTrialHeader.status == TimeTrialStatus.FINISHED) row.resultTime else null,
                            splits = if (fullTimeTrial.timeTrialHeader.status == TimeTrialStatus.FINISHED) transformSplits(row.resultSplits) else listOf(),
                            category = row.riderCategory,
                            gender = row.riderGender,
                            club = row.riderClub,
                            notes = row.resultNotes
                    )

                    timeTrialRiderRepository.insert(timeTrialRider)
                    return true
                }
            }
        }
        return false
    }

    suspend fun getTimeTrialByCourseDate(course: Course, localDate: LocalDate): TimeTrialHeader?{
        return course.id?.let {
            val tts = timeTrialRepository.allTimeTrialsOnCourse(course.id)
            val ds = tts.map { Pair(it.startTime?.dayOfYear, it.startTime?.year) }
             tts.firstOrNull { it.startTime?.dayOfYear == localDate.dayOfYear && it.startTime.year == localDate.year }
        }
    }

    suspend fun getDbCourse(course:Course): Course{

        if(course.courseName.isBlank()){
            throw InvalidParameterException()
        }

        val courseInDb: Course
        val courseList = courseRepository.getCoursesByName(course.courseName)

        courseInDb = when(courseList.size){
            0->{
                val id = courseRepository.insert(course)
                courseRepository.getCourseSuspend(id)
            }
            1->{
                courseList.first()
            }
            else-> courseList.first()
        }
        return courseInDb
    }

    suspend fun getDbRider(importRider:Rider): Rider{
        val riderInDbId:Rider

            val existingRiders = riderRepository.ridersFromFirstLastName(importRider.firstName, importRider.lastName)
            //var timeTrialRider:TimeTrialRider? = null
            riderInDbId = when(existingRiders.size){
                0->{
                    val newRider = Rider(importRider.firstName, importRider.lastName, importRider.club, null, importRider.category?:"", importRider.gender)
                    val id = riderRepository.insert(newRider)
                    newRider.copy(id = id)
                }
                1->{
                    existingRiders.first()
                }
                else->{
                    val byGender = existingRiders.filter { importRider.gender!= Gender.UNKNOWN && it.gender == importRider.gender }
                    if(byGender.isEmpty()){
                        val newRider = Rider(importRider.firstName, importRider.lastName, importRider.club, null, importRider.category, importRider.gender)
                        val id = riderRepository.insert(newRider)
                        newRider.copy(id = id)
                    }else{
                        byGender.first()
                    }
                }
            }

        return riderInDbId
    }

    suspend fun  addImportTtToDb(importTt: TimeTrialIO): Boolean{
        val header = importTt.timeTrialHeader
        val course = importTt.course
        var inserted = false
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
                else-> courseInDb = courseList.minByOrNull { (it.length?:0.0) - (course.length?:0.0) }!!
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
                    val ttSt = header.startTime?: OffsetDateTime.now()
                    startTime = OffsetDateTime.of(ttSt.year, ttSt.month.value, ttSt.dayOfMonth, it.hour, it.minute - 1, it.second, 0, ZoneId.systemDefault().rules.getOffset(Instant.now()))
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
                numberRules = NumberRules().copy( indexRules =  IndexNumberRules(firstBib,  direction, listOf()))
            }

            val headerToInsert = header.copy(
                    ttName = headerName,
                    description = header.description,
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
                    inserted = true
                    headerToInsert.copy(id = id)
                }
                1->{
                    headerList.first()
                }
                else->{
                    val onCourseList = headerList.filter { it.courseId == courseInDb?.id }.sortedBy { it.startTimeMillis - headerToInsert.startTimeMillis }
                    onCourseList.firstOrNull()
                }
            }
        }
        importTt.timeTrialRiders.asSequence().filter { it.firstName.isNotBlank() }.sortedBy { it.bib?:it.startTime?.toSecondOfDay()?:0 }.forEachIndexed { index, importRider ->

            val existingRiders = riderRepository.ridersFromFirstLastName(importRider.firstName, importRider.lastName)
            val riderInDbId:Rider = when(existingRiders.size){
                0->{
                    val newRider = Rider(importRider.firstName, importRider.lastName, importRider.club, null, importRider.category?:"", importRider.gender)
                    val id = riderRepository.insert(newRider)
                    newRider.copy(id = id)
                }
                1->{
                    existingRiders.first()
                }
                else->{
                    val byGender = existingRiders.filter { importRider.gender!= Gender.UNKNOWN && it.gender == importRider.gender }
                    if(byGender.isEmpty()){
                        val newRider = Rider(importRider.firstName, importRider.lastName, importRider.club, null, importRider.category, importRider.gender)
                        val id = riderRepository.insert(newRider)
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
                            timeTrialId = headerInDb?.id?:0L,
                            courseId = courseInDb?.id,
                            index = index,
                            finishCode = if (headerInDb?.status == TimeTrialStatus.FINISHED) importRider.finishTime else null,
                            splits = if (headerInDb?.status == TimeTrialStatus.FINISHED) transformSplits(importRider.splits) else listOf(),
                            category = importRider.category,
                            gender = gen,
                            club = importRider.club,
                            notes = importRider.notes
                    )
                    insertTimeTrialRider(timeTrialRider)
            }
        }
        return inserted
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
                    timeTrialRiderRepository.insert(timeTrialRider)
                }
                1->{

                }
                else -> throw Exception("Multi Riders")
            }
        }
    }
}







