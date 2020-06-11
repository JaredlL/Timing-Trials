package com.jaredlinden.timingtrials.domain

import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.util.ConverterUtils
import com.jaredlinden.timingtrials.util.LengthConverter
import org.threeten.bp.OffsetDateTime

//interface  IResultColumn{
//
//    val key: String
//    val description : String
//    val descriptionResourceId : Int
//    fun getValue(result: IResult): String
//    val isVisible : Boolean
//    val index: Int
//    val sortOrder: Int
//    val filterText: String
//    fun compare(result1 : IResult, result2: IResult): Int
//}

enum class SortType{
    NONE, ASCENDING, DESCENDING
}

interface IColumnDefinition{
    val key: String
    val description : String
    val descriptionResourceId : Int
    val imageResourceId : Int
    fun getValue(result: IResult): String
    fun compare(result1 : IResult, result2: IResult): Int
    fun passesFilter(filterText: String, result:IResult):Boolean
}


data class ColumnData(val definition: IColumnDefinition, val sortOrder: Int = 0, val filterText: String = "", val isVisible: Boolean = true, val sortType:SortType = SortType.NONE){
    val key = definition.key
    val description = definition.description
    val imageRes = definition.imageResourceId
    fun getValue(result: IResult): String = definition.getValue(result)
    fun compare(result1: IResult, result2: IResult): Int = definition.compare(result1, result2)
    fun passesFilter(result: IResult):Boolean = definition.passesFilter(filterText, result)
}



class RiderNameColumn:IColumnDefinition{

    companion object{
        const val columnKey = "rider"
    }

    override val key: String = columnKey
    override val description: String = "Rider"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.rider
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_directions_bike_black_24dp

    override fun getValue(result: IResult): String {
        return result.rider.fullName()
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return -result1.rider.fullName().compareTo(result2.rider.fullName())
    }

    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return result.rider.fullName().contains(filterText, true)
    }


}


class CourseNameColumn : IColumnDefinition{

    companion object{
        const val columnKey = "co"
    }

    override val key: String = columnKey
    override val description: String = "Course"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.course
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_directions_black_24dp

    override fun getValue(result: IResult): String {
        return result.course.courseName
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return -result1.course.courseName.compareTo(result2.course.courseName)
    }

    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return result.course.courseName.contains(filterText, true)
    }
}

class ClubColumn : IColumnDefinition{


    companion object{
        const val columnKey = "clu"
    }

    override val key: String = columnKey
    override val description: String = "Club"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.club
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_baseline_group_24

    override fun getValue(result: IResult): String {
        return result.riderClub
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return -result1.riderClub.compareTo(result2.riderClub)
    }

    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return result.riderClub.contains(filterText, true)
    }
}

class CategoryColumn : IColumnDefinition{

    companion object{
        const val columnKey = "cat"
    }

    override val key: String = columnKey
    override val description: String = "Category"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.category
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_baseline_category_24

    override fun getValue(result: IResult): String {
        return result.category
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return -result1.category.compareTo(result2.category)
    }

    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return result.category.contains(filterText, true)
    }
}


class GenderColumn : IColumnDefinition{

    companion object{
        const val columnKey = "gen"
    }

    override val key: String = columnKey
    override val description: String = "Gender"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.gender
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_baseline_wc_24


    override fun getValue(result: IResult): String {
        return result.gender.fullString()
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return -result1.gender.compareTo(result2.gender)
    }
    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return result.gender.smallString().contains(filterText, true)
    }
}

class TimeColumn : IColumnDefinition{

    companion object{
        const val columnKey = "tim"
    }

    override val key: String = columnKey
    override val description: String = "Time"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.time
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_timer_black_24dp

    override fun getValue(result: IResult): String {
        return result.resultTime?.let { ConverterUtils.toTenthsDisplayString(it) }?:""
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return (result1.resultTime?:Long.MAX_VALUE).compareTo(result2.resultTime?:Long.MAX_VALUE)
    }
    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return getValue(result).contains(filterText, true)
    }
}

class DateColumn : IColumnDefinition{

    companion object{
        const val columnKey = "dat"
    }

    override val key: String = columnKey
    override val description: String = "Date"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.date
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_baseline_date_range_24

    override fun getValue(result: IResult): String {
        return result.dateSet?.let { ConverterUtils.dateToDisplay(it) }?:""
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return (result1.dateSet?: OffsetDateTime.MIN).compareTo(result2.dateSet?: OffsetDateTime.MIN)
    }
    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return getValue(result).contains(filterText, true)
    }
}

class DistanceColumn(private val distConverter: LengthConverter) : IColumnDefinition{

    companion object{
        const val columnKey = "dis"
    }

    override val key: String = columnKey
    override val description: String = "Distance"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.distance
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_baseline_timeline_24

    override fun getValue(result: IResult): String {
        return result.course.length.let { distConverter.lengthToDisplay(it * result.laps) }
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return (result1.course.length * result1.laps).compareTo(result2.course.length * result2.laps)
    }
    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return getValue(result).contains(filterText, true)
    }
}


class LapsColumn : IColumnDefinition{

    companion object{
        const val columnKey = "lap"
    }

    override val key: String = columnKey
    override val description: String = "Laps"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.laps
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_baseline_repeat_24

    override fun getValue(result: IResult): String {
        return result.laps.toString()
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return (result1.laps).compareTo(result2.laps)
    }
    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return getValue(result).contains(filterText, true)
    }
}

class SpeedColumn(private val distConverter: LengthConverter) : IColumnDefinition{

    companion object{
        const val columnKey = "spe"
    }

    override val key: String = columnKey
    override val description: String = "Average Speed"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.average_speed
    override val imageResourceId: Int = com.jaredlinden.timingtrials.R.drawable.ic_baseline_speed_24

    override fun getValue(result: IResult): String {

        val rt = result.resultTime
        return if(rt != null && rt != 0L){
            val averageSpeedMetersPerMilisecond = distConverter.convert(result.course.length * result.laps) * (3600000.0 / rt.toDouble())
            return "%2.2f".format(averageSpeedMetersPerMilisecond)
        }else{
            ""
        }
    }

    fun  averageSpeed(result: IResult): Double{
        val rt = result.resultTime
        return if(rt!= null && rt > 0){
            (result.course.length * result.laps) / rt.toDouble()
        }
        else{
            Double.MAX_VALUE
        }
    }


    override fun compare(result1: IResult, result2: IResult): Int {
        return averageSpeed(result1).compareTo(averageSpeed(result2))
    }
    override fun passesFilter(filterText: String, result: IResult): Boolean {
        return getValue(result).contains(filterText, true)
    }
}