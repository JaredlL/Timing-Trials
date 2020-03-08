package com.android.jared.linden.timingtrials.domain.csv

import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.domain.ILineToObjectConverter
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter

object CSVUtils{
    fun lineToList(input: String): List<String>{
        val result: MutableList<String> = ArrayList()
        var start = 0
        var inQuotes = false
        for (current in 0 until input.length) {
            if (input[current] == '\"') inQuotes = !inQuotes // toggle state
            val atLastChar = current == input.length - 1
            if (atLastChar) {
                result.add(input.substring(start).replace(""""""", ""))
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

    override fun isHeading(line:String): Boolean{
        return line.splitToSequence(",", ignoreCase = true).any{it.contains("TimeTrial Name", true)}
    }

    var nameIndex:Int? = null
    var dateindex:Int? = null
    var lapsIndex:Int? = null
    var statusIndex:Int? = null


    override fun setHeading(headingLine: String){
        val splitLine = CSVUtils.lineToList(headingLine)

        nameIndex = splitLine.withIndex().firstOrNull { it.value.contains("name", true) }?.index
        dateindex= splitLine.withIndex().firstOrNull { it.value.contains("date", true) }?.index
        lapsIndex= splitLine.withIndex().firstOrNull { it.value.contains("laps", true) }?.index
        statusIndex = splitLine.withIndex().firstOrNull { it.value.contains("status", true) }?.index

    }

    val formatList = listOf("d/m/y", "d-M-y","d/M/y")

    override fun importLine(dataLine: String): TimeTrialHeader? {

        try{
            val dataList = CSVUtils.lineToList(dataLine)
            val ttName = nameIndex?.let { dataList.getOrNull(it)}?:""
            val dateString = dateindex?.let { dataList.getOrNull(it) }
            val status = statusIndex?.let { if((dataList.getOrNull(it)?:"").contains("setting up", ignoreCase = true)) TimeTrialStatus.SETTING_UP else TimeTrialStatus.FINISHED }
            var date: LocalDate? = null
            for(pattern in formatList){
                try {
                    val formatter = DateTimeFormatter.ofPattern(pattern)
                    date = LocalDate.parse(dateString, formatter)
                    break
                }catch(e:Exception) {
                    val b = e
                }
            }
            val offsetDateTime = date?.let { OffsetDateTime.of(it, LocalTime.of(19,0,0), ZoneId.systemDefault().rules.getOffset(Instant.now()))}
            val laps = lapsIndex?.let { dataList.getOrNull(it)?.toIntOrNull() }?:1

            return TimeTrialHeader(ttName, null, laps,60, offsetDateTime?: OffsetDateTime.MIN, status = status?:TimeTrialStatus.FINISHED)

        }catch (e:Exception){
            throw Exception("Error reading timetrial data", e)
        }

    }

    fun fromCttTitle(cttTitle: String): TimeTrialHeader{
        val titleString = cttTitle.replace(".csv", "", ignoreCase = true).replace("startsheet-", "", ignoreCase = true).replace("results-", "", ignoreCase = true)
        val dateList = titleString.split("-").reversed().mapNotNull { it.toIntOrNull() }
        val date = if(dateList.size > 2){

            OffsetDateTime.of(LocalDate.of(2000+dateList[0], dateList[1],dateList[2]), LocalTime.of(1,0,0),ZoneId.systemDefault().rules.getOffset(Instant.now()))
        }else{
            OffsetDateTime.MIN
        }
        val datePortion = Regex("""[-]\d{1,2}[-]\d{1,2}[-]\d{1,2}""").find(titleString)?.value

        val cleanedTitle = datePortion?.let {
            titleString.replace(datePortion, "").replace("-", " ").replace("  ", " ") + " ${datePortion.drop(1)}"
        }?:titleString

        val status = if(cttTitle.contains("startsheet")) TimeTrialStatus.SETTING_UP else TimeTrialStatus.FINISHED
        return TimeTrialHeader(ttName = cleanedTitle, startTime = date, status = status)
    }

}