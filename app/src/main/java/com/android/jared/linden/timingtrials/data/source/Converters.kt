package com.android.jared.linden.timingtrials.data.source

import androidx.room.TypeConverter
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.Rider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
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
    fun ridersFromString(ridersString:String): List<Rider>{
        val riderListType = object : TypeToken<List<Rider>>() {}.type
        return Gson().fromJson<List<Rider>>(ridersString, riderListType)

    }

    @TypeConverter
    fun ridersToString(riders: List<Rider>): String{
        return Gson().toJson(riders)
    }

    @TypeConverter
    fun courseFromString(courseString:String): Course {
        val courseType = object : TypeToken<Course>() {}.type
        return Gson().fromJson<Course>(courseString, courseType)

    }

    @TypeConverter
    fun courseToString(course: Course): String{
        return Gson().toJson(course)
    }
}