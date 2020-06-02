package com.jaredlinden.timingtrials.domain

import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.util.ConverterUtils
import com.jaredlinden.timingtrials.util.LengthConverter
import org.threeten.bp.OffsetDateTime

interface  IResultColumn{

    val key: String
    val description : String
    val descriptionResourceId : Int
    fun getValue(result: IResult): String
    val isVisible : Boolean
    val index: Int
    val sortOrder: Int
    val filterText: String
    fun compare(result1 : IResult, result2: IResult): Int
}

interface IColumnDefinition{
    val key: String
    val description : String
    val descriptionResourceId : Int
    fun getValue(result: IResult): String
    fun compare(result1 : IResult, result2: IResult): Int
}


data class ColumnData(val sortOrder: Int = 0, val filterText: String = "", val isVisible: Boolean = true, private val definition: IColumnDefinition){
    val key = definition.key
    val description = definition.description
    fun getValue(result: IResult): String = definition.getValue(result)
    fun compare(result1: IResult, result2: IResult): Int = definition.compare(result1, result2)

}

const val BLOBS_KEY = "rider"
class BlobsColumn(){

    override val key: String = RIDER_KEY
    override val description: String = "Rider"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.rider

    override fun getValue(result: IResult): String {
        return result.rider.fullName()
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return result1.rider.fullName().compareTo(result2.rider.fullName())
    }


}


const val RIDER_KEY = "rider"
class RiderNameColumn(override val index: Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true):IResultColumn{

    override val key: String = RIDER_KEY
    override val description: String = "Rider"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.rider

    override fun getValue(result: IResult): String {
        return result.rider.fullName()
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return result1.rider.fullName().compareTo(result2.rider.fullName())
    }


}

const val COURSE_KEY = "course"
class CourseNameColumn(override val index:Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true) : IResultColumn{

    override val key: String = COURSE_KEY
    override val description: String = "Course"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.course

    override fun getValue(result: IResult): String {
        return result.course.courseName
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return result1.course.courseName.compareTo(result2.course.courseName)
    }
}

const val CLUB_KEY = "club"
class ClubColumn(override val index: Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true) : IResultColumn{

    override val key: String = CLUB_KEY
    override val description: String = "Club"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.club

    override fun getValue(result: IResult): String {
        return result.riderClub
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return result1.riderClub.compareTo(result2.riderClub)
    }
}

const val CATEGORY_KEY = "cat"
class CategoryColumn(override val index: Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true) : IResultColumn{

    override val key: String = CATEGORY_KEY
    override val description: String = "Category"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.category

    override fun getValue(result: IResult): String {
        return result.category
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return result1.category.compareTo(result2.category)
    }
}

const val GENDER_KEY = "gen"
class GenderColumn(override val index: Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true) : IResultColumn{

    override val key: String = GENDER_KEY
    override val description: String = "Gender"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.gender


    override fun getValue(result: IResult): String {
        return result.gender.fullString()
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return result1.gender.compareTo(result2.gender)
    }
}

const val TIME_KEY = "time"
class TimeColumn(override val index: Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true) : IResultColumn{

    override val key: String = TIME_KEY
    override val description: String = "Time"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.time

    override fun getValue(result: IResult): String {
        return result.resultTime?.let { ConverterUtils.toSecondsDisplayString(it) }?:""
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return (result1.resultTime?:Long.MAX_VALUE).compareTo(result2.resultTime?:Long.MAX_VALUE)
    }
}

const val DATE_KEY = "date"
class DateColumn(override val index: Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true) : IResultColumn{

    override val key: String = DATE_KEY
    override val description: String = "Date"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.date
    override fun getValue(result: IResult): String {
        return result.dateSet?.let { ConverterUtils.dateToDisplay(it) }?:""
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return (result1.dateSet?: OffsetDateTime.MIN).compareTo(result2.dateSet?: OffsetDateTime.MIN)
    }
}

const val DISTANCE_KEY = "dist"
class DistanceColumn(val distConverter: LengthConverter, override val index: Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true) : IResultColumn{

    override val key: String = DISTANCE_KEY
    override val description: String = "Distance"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.distance
    override fun getValue(result: IResult): String {
        return result.course.length.let { distConverter.lengthToDisplay(it * result.laps) }
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return (result1.course.length * result1.laps).compareTo(result2.course.length * result2.laps)
    }
}

const val LAPS_KEY = "dist"
class LapsColumn(val distConverter: LengthConverter, override val index: Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true) : IResultColumn{

    override val key: String = LAPS_KEY
    override val description: String = "Laps"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.laps
    override fun getValue(result: IResult): String {
        return result.laps.toString()
    }

    override fun compare(result1: IResult, result2: IResult): Int {
        return (result1.laps).compareTo(result2.laps)
    }
}

const val SPEED_KEY = "speed"
class SpeedColumn(val distConverter: LengthConverter, override val index: Int, override val sortOrder: Int = 0, override val filterText: String = "", override val isVisible: Boolean = true) : IResultColumn{

    override val key: String = SPEED_KEY
    override val description: String = "Average Speed"
    override val descriptionResourceId: Int = com.jaredlinden.timingtrials.R.string.average_speed
    override fun getValue(result: IResult): String {

        val rt = result.resultTime
        return if(rt != null && rt != 0L){
            val averageSpeedMetersPerMilisecond = distConverter.convert(result.course.length * result.laps) * (3600000.0 / rt.toDouble())
            return "%2.2f".format(averageSpeedMetersPerMilisecond)
        }else{
            ""
        }
    }


    override fun compare(result1: IResult, result2: IResult): Int {
        return (result1.course.length * result1.laps).compareTo(result2.laps)
    }
}