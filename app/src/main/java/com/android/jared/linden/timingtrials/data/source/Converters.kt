package com.android.jared.linden.timingtrials.data.source

import androidx.room.TypeConverter
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.data.Rider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {

    //inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }


    @TypeConverter
    fun ridersFromString(ridersString:String?): List<Rider>?{
        return ridersString?.let {
            val riderListType = object : TypeToken<List<Rider>>() {}.type
            Gson().fromJson<List<Rider>>(ridersString, riderListType)
        }

    }

    @TypeConverter
    fun ridersToString(riders: List<Rider>?): String?{
        return riders?.let{Gson().toJson(riders)}
    }

    @TypeConverter
    fun courseFromString(courseString:String?): Course? {
       return courseString?.let{
        val courseType = object : TypeToken<Course>() {}.type
         Gson().fromJson<Course>(courseString, courseType)}

    }

    @TypeConverter
    fun courseToString(course: Course?): String?{
        return course?.let { Gson().toJson(course)}
    }

    @TypeConverter fun intToEventType(eventId: Int): EventType?{
        return EventType.fromInt(eventId)
    }

    @TypeConverter fun eventTypeToInt(eventType: EventType): Int{
        return eventType.type
    }
}