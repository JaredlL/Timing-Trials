package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.*
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

data class ImportResult(val result: Boolean, val message:String, val addedRiders: Int, val duplicateRiders: Int)

data class ImportTimeTrial(var header: TimeTrialHeader? = null, var course:Course? = null, val importRiderList: MutableList<ImportRider> = mutableListOf())

data class ImportRider(val firstName:String, val lastName:String?, val club: String?, val category:String?, val gender: Gender = Gender.UNKNOWN, val finishTime: Long, val splits: List<Long>, val notes:String?){
    companion object{
        fun createBlank():ImportRider{
            return ImportRider("", "", "",null, Gender.UNKNOWN, 0L, listOf(), null)
        }
    }
}


interface ILineToObjectConverter<T>{
    fun isHeading(line:String): Boolean
    fun setHeading(headingLine: String)
    fun importLine(dataLine: String): T?
}



abstract class StringToObjectField<T>
{
    abstract val fieldIndex: Int?

    fun applyFieldToObject(row: List<String>, target:T): T{
        val fi = fieldIndex
        if(fi !=null){
            row.getOrNull(fi)?.let {
                if(it.isNotBlank()){
                    return applyFieldFromString(it, target)
                }

            }
        }
        return target
    }

    abstract fun applyFieldFromString(valString: String, target: T):T
}

class LineToRiderConverter: ILineToObjectConverter<ImportRider>{

    class ToRiderFirstName(private val heading: List<String>):StringToObjectField<ImportRider>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { it.contains("first", true) && it.contains("name", true) }

        override fun applyFieldFromString(valString: String, target: ImportRider): ImportRider {
            return target.copy(firstName = valString)
        }
    }

    class LastNameFieldSetter(private val heading: List<String>):StringToObjectField<ImportRider>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("sur", true) || it.contains("last", true)) && it.contains("name", true) }

        override fun applyFieldFromString(valString: String, target: ImportRider): ImportRider {
            return target.copy(lastName = valString)
        }
    }

    class FullNameFieldSetter(private val heading: List<String>):StringToObjectField<ImportRider>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { it.contains("name", true) }

        override fun applyFieldFromString(valString: String, target: ImportRider): ImportRider {
            val split = valString.split(" ", ignoreCase = true)
            return target.copy(firstName = split.first(), lastName = split.drop(1).joinToString(" "))
        }
    }

    class ClubFieldSetter(private val heading: List<String>):StringToObjectField<ImportRider>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("club", true) || it.contains("team", true)) }

        override fun applyFieldFromString(valString: String, target: ImportRider): ImportRider {
            return target.copy(club = valString)
        }
    }



    class CategoryFieldSetter(private val heading: List<String>):StringToObjectField<ImportRider>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("category", true))}

        override fun applyFieldFromString(valString: String, target: ImportRider): ImportRider {
            return target.copy(category = valString)}
    }

    class GenderFieldSetter(private val heading: List<String>):StringToObjectField<ImportRider>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("gender", true)) }

        override fun applyFieldFromString(valString: String, target: ImportRider): ImportRider {
            val gen = when {
                valString.equals("male", true) -> { Gender.MALE }
                valString.equals("female", true) -> { Gender.FEMALE }
                valString.equals("m", true) -> { Gender.MALE }
                valString.equals("f", true) -> { Gender.FEMALE }
                valString.equals("other", true) -> { Gender.OTHER }
                else -> Gender.UNKNOWN
            }

            return target.copy(gender = gen)
        }
    }

    class FinishTimeFieldSetter(private val heading: List<String>):StringToObjectField<ImportRider>() {

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { (it.contains("time", true)) }

        override fun applyFieldFromString(valString: String, target: ImportRider): ImportRider {

            //val times = valString.split(":",".").reversed()

            val splitAtPoint = valString.split(".")
            if(splitAtPoint.size > 1){

                val ms = splitAtPoint.last().let { ".$it" }.toDoubleOrNull()?.let { it * 1000}?.toInt()?:0

                val splits = splitAtPoint.first().split(":").reversed()
                val sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
                val min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
                val hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0


                return target.copy(finishTime = (hour + min + sec + ms).toLong())

            }else{
                val splits = splitAtPoint.first().split(":").reversed()
                val sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
                val min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
                val hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0

                return target.copy(finishTime = (hour + min + sec).toLong())
            }

            //val ms = times.getOrNull(0)?.let { ".$it" }?.toDouble()?.let { it * 1000 }?.toInt()?:0

        }
    }

    class SplitFieldSetter(private val heading: List<String>, val splitIndex: Int):StringToObjectField<ImportRider>() {

        override val fieldIndex: Int?
            get() = splitIndex

        override fun applyFieldFromString(valString: String, target: ImportRider): ImportRider {

            val times = valString.split(":",".").reversed()

            val ms = times.getOrNull(0)?.let { ".$it" }?.toDouble()?.let { it * 1000 }?.toInt()?:0
            val sec = times.getOrNull(1)?.toIntOrNull()?:0 * 1000
            val min = times.getOrNull(2)?.toIntOrNull()?:0 * 1000 * 60
            val hour = times.getOrNull(3)?.toIntOrNull()?:0 * 1000 * 60 * 60


            return target.copy(splits = target.splits + (hour + min + sec + ms).toLong())
        }
    }
    class NotesNameFieldSetter(private val heading: List<String>):StringToObjectField<ImportRider>(){

        override val fieldIndex: Int?
            get() = heading.indexOfFirst { it.contains("note", true) }

        override fun applyFieldFromString(valString: String, target: ImportRider): ImportRider {
            return target.copy(notes = valString)
        }
    }



    override fun isHeading(line: String): Boolean {
        return line.contains("rider", ignoreCase = true) || line.contains("athlete", ignoreCase = true)
    }

    var stringToFieldList: List<StringToObjectField<ImportRider>> = listOf()

    override fun setHeading(headingLine: String) {
       val splitHeading = headingLine.split(",")
        stringToFieldList = listOf(
                ToRiderFirstName(splitHeading),
                LastNameFieldSetter(splitHeading),
                ClubFieldSetter(splitHeading),
                CategoryFieldSetter(splitHeading),
                GenderFieldSetter(splitHeading),
                FinishTimeFieldSetter(splitHeading),
                NotesNameFieldSetter(splitHeading)
                )
        val splitsList = splitHeading.mapIndexedNotNull  { index, s ->
            if (s.contains("split")) {
                SplitFieldSetter(splitHeading, index)
            }else{
                null
            }
        }

        stringToFieldList = stringToFieldList + splitsList

    }

    override fun importLine(dataLine: String): ImportRider? {
        var importRider = ImportRider("", "", "",null, Gender.UNKNOWN, 0L, listOf(), null)

        for(fieldFiller in stringToFieldList){
            importRider = fieldFiller.applyFieldToObject(dataLine.split(","), importRider)
        }
        val ir = importRider
        return if(ir.firstName.isNotBlank() && ir.finishTime > 0L){
            ir
        }else{
            null
        }
    }


}

class LineToCourseConverter: ILineToObjectConverter<Course>{
    override fun isHeading(line: String): Boolean {
        return line.splitToSequence(",", ignoreCase = true).any{it.contains("course", true)}
    }

    val defaultConversion = 1000.0
    var nameIndex:Int? = 0
    var distanceIndex:Int? = 2
    var cttNameIndex:Int? = 1
    var conversion: Double = defaultConversion

    val conversions = mapOf("km" to 1000.0, "miles" to 1609.34, "mi" to 1609.34, "meters" to 1.0)


    override fun setHeading(headingLine: String) {
        val splitLine = headingLine.splitToSequence(",", ignoreCase = true)

        nameIndex = splitLine.withIndex().firstOrNull { it.value.contains("name", true) }?.index
        distanceIndex= splitLine.withIndex().firstOrNull { it.value.contains("distance", true) || it.value.contains("length", true) }?.index
        val distanceString = distanceIndex?.let { splitLine.elementAtOrNull(it)}
        conversion = distanceString?.let {ds -> conversions.filter{ds.contains(it.key, ignoreCase =  true)}.values.firstOrNull() } ?:defaultConversion
        cttNameIndex= splitLine.withIndex().firstOrNull { it.value.contains("ctt", true) }?.index
    }

    override fun importLine(dataLine: String): Course? {
        try{
            val dataList = dataLine.split(",", ignoreCase =  true)
            val courseName = nameIndex?.let { dataList.getOrNull(it)}
            val distance = distanceIndex?.let { dataList.getOrNull(it)?.toIntOrNull()?.times(conversion)}
            val cctName = cttNameIndex?.let { dataList.getOrNull(it) }

            return if(courseName.isNullOrBlank()){
                null
            }else{
                Course(courseName, distance?:0.0, cctName?:"")
            }


        }catch (e:Exception){
            throw Exception("Error reading course data", e)
        }
    }


}

class LineToTimeTrialConverter() : ILineToObjectConverter<TimeTrialHeader>{

    override fun isHeading(line:String): Boolean{
        return line.splitToSequence(",", ignoreCase = true).any{it.contains("timetrial", true)}
    }

    var nameIndex:Int? = null
    var dateindex:Int? = null
    var lapsIndex:Int? = null


    override fun setHeading(headingLine: String){
        val splitLine = headingLine.splitToSequence(",", ignoreCase = true)

        nameIndex = splitLine.withIndex().firstOrNull { it.value.contains("name", true) }?.index
        dateindex= splitLine.withIndex().firstOrNull { it.value.contains("date", true) }?.index
        lapsIndex= splitLine.withIndex().firstOrNull { it.value.contains("laps", true) }?.index

    }

    val formatList = listOf("d/m/y", "d-m-y","dd/mm/yyyy")

    override fun importLine(dataLine: String):TimeTrialHeader? {

        try{
            val dataList = dataLine.split(",", ignoreCase =  true)
            val ttName = nameIndex?.let { dataList.getOrNull(it)}?:""
            val dateString = dateindex?.let { dataList.getOrNull(it) }
            var date: LocalDate? = null
            for(pattern in formatList){
                try {
                    val formatter = DateTimeFormatter.ofPattern(pattern)
                    date = LocalDate.parse(dateString, formatter)
                    break
                }catch(e:Exception) {

                }
            }
            val offsetDateTime = date?.let { OffsetDateTime.of(it, LocalTime.of(19,0,0), ZoneOffset.of(ZoneId.systemDefault().id) )}
            val laps = lapsIndex?.let { dataList.getOrNull(it)?.toIntOrNull() }?:1

            return TimeTrialHeader(ttName, null, laps,60, offsetDateTime?:OffsetDateTime.MIN, status = TimeTrialStatus.FINISHED)

        }catch (e:Exception){
            throw Exception("Error reading timetrial data", e)
        }

    }

}