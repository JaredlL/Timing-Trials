package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.*
import com.android.jared.linden.timingtrials.domain.csv.ImportTimeTrial
import com.android.jared.linden.timingtrials.domain.csv.LineToCourseConverter
import com.android.jared.linden.timingtrials.domain.csv.LineToRiderConverter
import com.android.jared.linden.timingtrials.domain.csv.LineToTimeTrialConverter
import com.android.jared.linden.timingtrials.util.ConverterUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import kotlin.Exception

class ImportViewModel @Inject constructor(private val riderRespository: IRiderRepository,
                                          private val courseRepository: ICourseRepository,
                                          private val timeTrialRepository: ITimeTrialRepository,
                                          private val  timeTrialRiderRepository: TimeTrialRiderRepository): ViewModel() {




    fun readInput(title: String?, inputStream: InputStream){
        viewModelScope.launch(Dispatchers.IO) {
            readInputIntoDb(inputStream)
        }
    }

    val READING_TT = 0
    val READING_COURSE = 1
    val READING_RIDER = 2


    suspend fun readInputIntoDb(inputStream: InputStream){
        val reader = BufferedReader(InputStreamReader(inputStream))

        val lineToTt = LineToTimeTrialConverter()
        val lineToCourse = LineToCourseConverter()
        val lineToRider = LineToRiderConverter()

        var currentLine = reader.readLine()
        val timeTrialList: MutableList<ImportTimeTrial> = mutableListOf()
        var state = READING_TT

        while (currentLine != null){
            if(lineToTt.isHeading(currentLine)){
                lineToTt.setHeading(currentLine)
                timeTrialList.add(ImportTimeTrial())
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
                            timeTrialList.lastOrNull()?.header = it
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
                        val rider = lineToRider.importLine(currentLine)
                        rider?.let {
                            timeTrialList.lastOrNull()?.importRiderList?.add(it)
                        }
                    }
                }
            }
            currentLine = reader.readLine()
        }

        timeTrialList.forEach {
            addImportTtToDb(it)
        }

    }

    suspend fun addImportTtToDb(importTt: ImportTimeTrial){
        val header = importTt.header
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
            val headerToInsert = header.copy(ttName = headerName, courseId = courseInDb?.id, status = TimeTrialStatus.FINISHED)

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
        importTt.importRiderList.filter { it.firstName.isNotBlank() }.forEach {importRider->

            val existingRiders = riderRespository.ridersFromFirstLastName(importRider.firstName, importRider.lastName?:"")
            //var timeTrialRider:TimeTrialRider? = null
            val riderInDbId:Rider = when(existingRiders.size){
                0->{
                    val newRider = Rider(importRider.firstName, importRider.lastName?:"", importRider.club?:"", null, importRider.category?:"", importRider.gender)
                    val id = riderRespository.insert(newRider)
                    newRider.copy(id = id)
                }
                1->{
                    existingRiders.first()
                }
                else->{
                    val byGender = existingRiders.filter { importRider.gender!= Gender.UNKNOWN && it.gender == importRider.gender }
                    if(byGender.isEmpty()){
                        val newRider = Rider(importRider.firstName, importRider.lastName?:"", importRider.club?:"", null, importRider.category?:"", importRider.gender)
                        val id = riderRespository.insert(newRider)
                        newRider.copy(id = id)
                    }else{
                        byGender.first()
                    }
                }
            }

            riderInDbId.id?.let {
                val fTime = importRider.finishTime
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
                            number = 0,
                            finishTime = fTime,
                            splits = importRider.splits,
                            category = importRider.category,
                            gender = importRider.gender,
                            club = importRider.club?:"",
                            resultNote = importRider.notes?:""
                    )
                    insertTimeTrialRider(timeTrialRider)
                }

            }



        }

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







