package com.android.jared.linden.timingtrials.domain.csv

import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.domain.ILineToObjectConverter
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter

class LineToTimeTrialConverter : ILineToObjectConverter<TimeTrialHeader> {

    override fun isHeading(line:String): Boolean{
        return line.splitToSequence(",", ignoreCase = true).any{it.contains("TimeTrial Name", true)}
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

    val formatList = listOf("d/m/y", "d-M-y","d/M/y")

    override fun importLine(dataLine: String): TimeTrialHeader? {

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
                    val b = e
                }
            }
            val offsetDateTime = date?.let { OffsetDateTime.of(it, LocalTime.of(19,0,0), ZoneId.systemDefault().rules.getOffset(Instant.now()))}
            val laps = lapsIndex?.let { dataList.getOrNull(it)?.toIntOrNull() }?:1

            return TimeTrialHeader(ttName, null, laps,60, offsetDateTime?: OffsetDateTime.MIN, status = TimeTrialStatus.FINISHED)

        }catch (e:Exception){
            throw Exception("Error reading timetrial data", e)
        }

    }

}