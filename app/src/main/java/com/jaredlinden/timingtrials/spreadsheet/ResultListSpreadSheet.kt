package com.jaredlinden.timingtrials.spreadsheet

import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.util.ConverterUtils
import com.jaredlinden.timingtrials.util.LengthConverter
import com.jaredlinden.timingtrials.util.Utils




//class  ResultListSpreadSheet(val results: List<IResult>, val distConverter: LengthConverter):ISheetLayoutManagerOptions {
//
//
//
//
//
//}


class RiderResultListSpreadSheet(val results: List<IResult>, val distConverter: LengthConverter):ISheetLayoutManagerOptions{

    override val data: List<List<String>> = results.map { listFromResult(it) }

    private fun listFromResult(res:IResult) : List<String>{
        val dateString = res.dateSet?.let { ConverterUtils.dateToDisplay(it) }?:""
        val timeString = res.resultTime?.let { ConverterUtils.toSecondsDisplayString(it) }?:""
        val distDisplayString = res.course.length.let { distConverter.lengthToDisplay(it * res.laps) }
        val averageSpeedMetersPerMilisecond = averageSpeed(res)
        return listOf(res.course.courseName, timeString, dateString,  distDisplayString, res.laps.toString(), averageSpeedMetersPerMilisecond)
    }

    fun averageSpeed(res:IResult) : String{
        val rt = res.resultTime
       return if(rt != null && rt != 0L){
           val averageSpeedMetersPerMilisecond = distConverter.convert(res.course.length * res.laps) * (3600000.0 / rt.toDouble())
        return "%2.2f".format(averageSpeedMetersPerMilisecond)
        }else{
            ""
        }
    }

    override val headings: List<String> = listOf("Course", "Time", "Date", "Distance (${distConverter.unitDef.miniString})", "Laps", "Avg Speed (${distConverter.unitDef.miniString}/h)")


    override val numberOfColumns: Int = headings.count()


    override val numberOfRows: Int  = results.count()

    override val isEmpty: Boolean = results.isEmpty()


    val headingWidths = headings.map { it.length }
    val colWidths: List<Int> = data.fold(headings.map { it.length }, {currentLengths,strings -> currentLengths.zip(strings).map { if(it.first >= it.second.length ) it.first else it.second.length } })

    override fun getColumnWidth(column: Int): Int {
        return if(headingWidths[column] != colWidths[column]) colWidths[column] * 8 + 10 else colWidths[column] * 8
    }

    override fun getRowHeight(row: Int): Int {
        return 30
    }

}

class CourseResultListSpreadSheet(val results: List<IResult>, val distConverter: LengthConverter):ISheetLayoutManagerOptions{

    override val data: List<List<String>> = results.map { listFromResult(it) }

    private fun listFromResult(res:IResult) : List<String>{
        val dateString = res.dateSet?.let { ConverterUtils.dateToDisplay(it) }?:""
        val timeString = res.resultTime?.let { ConverterUtils.toSecondsDisplayString(it) }?:""
        //val distDisplayString = res.course.length.let { distConverter.lengthToDisplay(it * res.laps) }
        val averageSpeedMetersPerMilisecond = averageSpeed(res)
        return listOf(res.rider.fullName(), timeString, dateString,  res.riderClub,  res.gender.smallString(),res.category,  averageSpeedMetersPerMilisecond, res.laps.toString())
    }

    fun averageSpeed(res:IResult) : String{
        val rt = res.resultTime
        return if(rt != null && rt != 0L){
            val averageSpeedMetersPerMilisecond = distConverter.convert(res.course.length * res.laps) * (3600000.0 / rt.toDouble())
            return "%2.2f".format(averageSpeedMetersPerMilisecond)
        }else{
            ""
        }
    }

    override val headings: List<String> = listOf("Rider", "Time", "Date", "Club", "Gender", "Category","Avg Speed ${distConverter.unitDef.miniString}/h", "Laps" )


    override val numberOfColumns: Int = headings.count()


    override val numberOfRows: Int  = results.count()

    override val isEmpty: Boolean = results.isEmpty()


    val headingWidths = headings.map { it.length }
    val colWidths: List<Int> = data.fold(headings.map { it.length }, {currentLengths,strings -> currentLengths.zip(strings).map { if(it.first >= it.second.length ) it.first else it.second.length } })

    override fun getColumnWidth(column: Int): Int {
        return if(headingWidths[column] != colWidths[column]) colWidths[column] * 8 + 10 else colWidths[column] * 8
    }

    override fun getRowHeight(row: Int): Int {
        return 30
    }

}