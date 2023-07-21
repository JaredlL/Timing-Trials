package com.jaredlinden.timingtrials.domain.csv

import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.domain.ILineToObjectConverter
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter

object CSVUtils{
    fun lineToList(input: String): List<String>{
        val result: MutableList<String> = ArrayList()
        var start = 0
        var inQuotes = false
        for (current in input.indices) {
            if (input[current] == '\"') inQuotes = !inQuotes // toggle state
            val atLastChar = current == input.length - 1
            if (atLastChar) {
                val lastString = input.substring(start).replace(""""""", "")
                val sToAdd = if(lastString.endsWith(",")){
                    lastString.dropLast(1)
                }else{
                    lastString
                }
                result.add(sToAdd)
            }
            else if (input[current] == ',' && !inQuotes) {
                result.add(input.substring(start, current).replace(""""""", ""))
                start = current + 1
            }
        }
        return result
    }
}

class LineToTimeTrialConverter : ILineToObjectConverter<TimeTrialHeader> {

    val NAME = "name"
    val DATE = "date"
    val LAPS = "laps"
    val STATUS = "status"
    val DESCRIPTION = "description"
    val NOTES = "notes"
    val ROW_START = ">>time trial"

    override fun isHeading(line:String): Boolean{
        return line.startsWith(">>time trial", true)
    }

    var nameIndex:Int? = null
    var dateindex:Int? = null
    var lapsIndex:Int? = null
    var statusIndex:Int? = null
    var notesIndex:Int? = null


    override fun setHeading(headingLine: String){
        val splitLine = CSVUtils.lineToList(headingLine)

        nameIndex = splitLine.withIndex().firstOrNull { it.value.contains(NAME, true) }?.index
        dateindex= splitLine.withIndex().firstOrNull { it.value.contains(DATE, true) }?.index
        lapsIndex= splitLine.withIndex().firstOrNull { it.value.contains(LAPS, true) }?.index
        statusIndex = splitLine.withIndex().firstOrNull { it.value.contains(STATUS, true) }?.index
        notesIndex = splitLine.withIndex().firstOrNull { it.value.contains(DESCRIPTION, true) || it.value.contains(NOTES, true) }?.index
    }

    val formatList = listOf("d/m/y", "d-M-y","d/M/y")

    override fun importLine(dataLine: List<String>): TimeTrialHeader? {

        try{
            val dataList =dataLine// CSVUtils.lineToList(dataLine)
            val ttName = nameIndex?.let { dataList.getOrNull(it)}?:""
            val dateString = dateindex?.let { dataList.getOrNull(it) }
            val status = statusIndex?.let { if((dataList.getOrNull(it)?:"").contains("setting up", ignoreCase = true)) TimeTrialStatus.SETTING_UP else TimeTrialStatus.FINISHED }
            val date= dateString?.let {  ObjectFromString.date(it)}
            val notes = notesIndex?.let { (dataList.getOrNull(it)) }?:""
            val offsetDateTime = date?.let { OffsetDateTime.of(it, LocalTime.of(19,0,0), ZoneId.systemDefault().rules.getOffset(Instant.now()))}
            val laps = lapsIndex?.let { dataList.getOrNull(it)?.toIntOrNull() }?:1

            return TimeTrialHeader(ttName, null, laps,60, offsetDateTime?: OffsetDateTime.MIN, status = status?:TimeTrialStatus.FINISHED, description = notes)

        }catch (e:Exception){
            throw Exception("Error reading timetrial data", e)
        }

    }

    fun fromCttTitle(cttTitle: String): TimeTrialHeader{
        val titleString = cttTitle.replace(".csv", "", ignoreCase = true).replace("startsheet-", "", ignoreCase = true).replace("results-", "", ignoreCase = true)

        val localDate = Regex("""\d{1,2}[-]\d{1,2}[-]\d{1,2}""").find(titleString)?.value?.let {
            LocalDate.parse(it, DateTimeFormatter.ofPattern("d-M-y"))
        }

        val date = localDate?.let {
            OffsetDateTime.of(
                LocalDate.of(2000 +localDate.year, localDate.month,localDate.dayOfMonth),
                LocalTime.of(1,0,0),ZoneId.systemDefault().rules.getOffset(Instant.now()))
        }

        val datePortion = Regex("""[-]\d{1,2}[-]\d{1,2}[-]\d{1,2}""").find(titleString)?.value

        val cleanedTitle = datePortion?.let {
            titleString.replace(datePortion, "").replace("-", " ").replace("  ", " ") + " ${datePortion.drop(1)}"
        }?:titleString

        val status = if(cttTitle.contains("startsheet")) TimeTrialStatus.SETTING_UP else TimeTrialStatus.FINISHED
        return TimeTrialHeader(ttName = cleanedTitle, startTime = date, status = status)
    }

}