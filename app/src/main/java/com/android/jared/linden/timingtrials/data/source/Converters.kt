package com.android.jared.linden.timingtrials.data.source

import androidx.room.TypeConverter
import com.android.jared.linden.timingtrials.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter


class Converters {


    @TypeConverter
    fun instantFromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(value) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun durationFromSeconds(value: Long?): Duration? {
        return value?.let { Duration.ofSeconds(value) }
    }

    @TypeConverter
    fun durationToLongSeconds(duration: Duration?): Long? {
        return duration?.seconds
    }


    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    @TypeConverter
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let {
            return formatter.parse(value, OffsetDateTime::from)
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun courseFromString(courseString:String?): Course? {
       return courseString?.let{
        val courseType = object : TypeToken<Course>() {}.type
         Gson().fromJson<Course>(courseString, courseType)}

    }

    @TypeConverter
    fun courseRecordsToString(cr:List<CourseRecord>?): String?{
        return cr?.let { Gson().toJson(cr)}
    }

    @TypeConverter
    fun courseRecordFromString(courseRecordString:String?): List<CourseRecord>? {
        return courseRecordString?.let{
            val courseType = object : TypeToken<List<CourseRecord>>() {}.type
            Gson().fromJson<List<CourseRecord>>(courseRecordString, courseType)}

    }

    @TypeConverter
    fun personalBestToString(pb: List<PersonalBest>?): String?{
        return pb?.let { Gson().toJson(pb)}
    }

    @TypeConverter
    fun personalBestFromString(personalBestString:String?): List<PersonalBest>? {
        return personalBestString?.let{
            val pbType = object : TypeToken<List<PersonalBest>>() {}.type
            Gson().fromJson<List<PersonalBest>>(personalBestString, pbType)}

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

    @TypeConverter fun intToGendar(gendarInt: Int): Gender?{
        return Gender.fromInt(gendarInt)
    }

    @TypeConverter fun gendarToInt(gender: Gender): Int{
        return gender.ordinal
    }
}