package com.android.jared.linden.timingtrials.data.source

import androidx.room.TypeConverter
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.data.Rider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter


object Converters {


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
    @JvmStatic
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let {
            return formatter.parse(value, OffsetDateTime::from)
        }
    }

    @TypeConverter
    @JvmStatic
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