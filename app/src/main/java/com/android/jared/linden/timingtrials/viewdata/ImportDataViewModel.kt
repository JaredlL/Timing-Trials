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
import com.android.jared.linden.timingtrials.domain.csv.LineToRiderConverter
import com.android.jared.linden.timingtrials.domain.csv.LineToTimeTrialConverter
import com.android.jared.linden.timingtrials.util.ConverterUtils
import com.android.jared.linden.timingtrials.util.Event
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import java.io.*
import java.util.zip.ZipInputStream
import javax.inject.Inject


class IOViewModel @Inject constructor(private val riderRespository: IRiderRepository,
                                      private val courseRepository: ICourseRepository,
                                      private val timeTrialRepository: ITimeTrialRepository,
                                      private val  timeTrialRiderRepository: TimeTrialRiderRepository): ViewModel() {


    val writeAllResult: MutableLiveData<Event<String>> = MutableLiveData()

    fun writeAllTimeTrialsToPath(outputStream: OutputStream){
        viewModelScope.launch(Dispatchers.IO) {
            try {


                val allTts = timeTrialRepository.allTimeTrials()
                JsonResultsWriter().writeToPath(outputStream, allTts)
                writeAllResult.postValue(Event("Success"))
            }catch (e:Exception){
                writeAllResult.postValue(Event(e.message?:"Error"))
            }
        }
    }

    fun readInput(title: String?, inputStream: InputStream){
        viewModelScope.launch(Dispatchers.IO) {

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
                readCsvInputIntoDb(fString)
            }

            importMessage.postValue(Event(msg))

            //readCsvInputIntoDb(inputStream)
        }
    }

    val READING_TT = 0
    val READING_COURSE = 1
    val READING_RIDER = 2

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

    private suspend fun readCsvInputIntoDb(fileString: String): String{
        try{
            val lineToTt = LineToTimeTrialConverter()
            val lineToCourse = LineToCourseConverter()
            val lineToRider = LineToRiderConverter()

            val lineList = fileString.lineSequence()

            val timeTrialList: MutableList<TimeTrialIO> = mutableListOf()
            var state = READING_TT

            for  (fileLine in fileString.lineSequence()){
                val currentLine = fileLine.replace(""""""", "")
                if(lineToTt.isHeading(currentLine)){
                    lineToTt.setHeading(currentLine)
                    timeTrialList.add(TimeTrialIO())
                    state = READING_TT

                }else if (lineToCourse.isHeading(currentLine)){
                    lineToCourse.setHeading(currentLine)
                    state = READING_COURSE

                }else if (lineToRider.isHeading(currentLine)){
                    lineToRider.setHeading(currentLine)
                    state = READING_RIDER
                }
                else{
                    when(state){
                        READING_TT->{
                            val tt = lineToTt.importLine(currentLine)
                            tt?.let {
                                timeTrialList.lastOrNull()?.timeTrialHeader = it
                                //state = READING_COURSE

                            }
                        }
                        READING_COURSE->{
                            val course = lineToCourse.importLine(currentLine)
                            course?.let {
                                timeTrialList.lastOrNull()?.course = it
                                //state = READING_RIDER
                            }
                        }
                        READING_RIDER->{
                            lineToRider.importLine(currentLine)?.let {
                                timeTrialList.lastOrNull()?.results?.add(it)
                            }
                        }
                    }
                }
//                currentLine = reader.readLine()
//                if(currentLine != null){
//                    currentLine = currentLine.replace(""""""", "")
//                    currentLine = currentLine.replace("""'""", "")
//                }
            }

            timeTrialList.forEach {
                addImportTtToDb(it)
            }
            return "Imported ${timeTrialList.count()} timetrials"
        }catch (e:Exception){
            return "Failed to import data ${e.message}"
        }


    }

    suspend fun addImportTtToDb(importTt: TimeTrialIO){
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
            val headerToInsert = header.copy(ttName = headerName, courseId = courseInDb?.id, status = header.status)

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
        importTt.results.filter { it.firstName.isNotBlank() }.forEach {importRider->

            val existingRiders = riderRespository.ridersFromFirstLastName(importRider.firstName, importRider.lastName?:"")
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
                val fTime =  importRider.finishTime
                val gen = if(importRider.gender != Gender.UNKNOWN)
                {
                    importRider.gender}
                else{
                    riderInDbId.gender
                }

                if(fTime > 0){



                    val timeTrialRider = TimeTrialRider(
                            riderId = it,
                            timeTrialId = headerInDb?.id,
                            courseId = courseInDb?.id,
                            index = 0,
                            finishTime = if (headerInDb?.status == TimeTrialStatus.FINISHED) fTime else 0L,
                            splits = if (headerInDb?.status == TimeTrialStatus.FINISHED) transformSplits(importRider.splits, importRider.finishTime) else listOf(),
                            category = importRider.category,
                            gender = gen,
                            club = importRider.club,
                            resultNote = importRider.notes
                    )
                    insertTimeTrialRider(timeTrialRider)
                }

            }



        }

    }

    fun transformSplits(splits: List<Long>, targetTime: Long): List<Long>{

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
        val courseId = timeTrialRider.courseId
        val timeTrialId = timeTrialRider.timeTrialId

        if(courseId != null && timeTrialId != null){
            val existing = timeTrialRiderRepository.getByRiderCourseTimeTrialIds(riderId, courseId, timeTrialId)

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







