package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.*
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
                                          private val  resultRepository: TimeTrialRiderRepository): ViewModel() {




    fun readInput(title: String?, inputStream: InputStream){
        viewModelScope.launch(Dispatchers.IO) {
            readInputIntoDb(inputStream)
        }
    }

    val READING_TT = 0
    val READING_COURSE = 1
    val READING_RIDDER = 2


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

            }else if (lineToCourse.isHeading(currentLine)){

            }else (lineToRider.isHeading(currentLine)){

            }
        }

    }



}

data class ImportTimeTrial(val header: TimeTrialHeader, val course:Course, val importRiderList: List<ImportRider>)

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

class StringToObjectFieldHelper<T>(val indexOfFieldHeading: (headingString: List<String>) -> Int?, val setField: (string:String,objectToSetField:T)->T){

    var index: Int? = null
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

            val times = valString.split(":",".").reversed()

            val ms = times.getOrNull(0)?.let { ".$it" }?.toDouble()?.let { it * 1000 }?.toInt()?:0
            val sec = times.getOrNull(1)?.toIntOrNull()?:0 * 1000
            val min = times.getOrNull(2)?.toIntOrNull()?:0 * 1000 * 60
            val hour = times.getOrNull(3)?.toIntOrNull()?:0 * 1000 * 60 * 60


            return target.copy(finishTime = (hour + min + sec + ms).toLong())
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
            importRider = fieldFiller.applyFieldFromString(dataLine, importRider)
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
    var nameIndex:Int? = null
    var distanceIndex:Int? = null
    var cttNameIndex:Int? = null
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

            return Course(courseName?:"", distance?:0.0, cctName?:"")

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

    val formatList = listOf("d/m/y", "d-m-y")

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

            return TimeTrialHeader(ttName, null, laps,60, offsetDateTime?:OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))

        }catch (e:Exception){
            throw Exception("Error reading timetrial data", e)
        }

    }

}