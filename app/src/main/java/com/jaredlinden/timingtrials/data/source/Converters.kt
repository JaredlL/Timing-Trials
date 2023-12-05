package com.jaredlinden.timingtrials.data.source

import androidx.room.TypeConverter
import com.jaredlinden.timingtrials.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
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

    private val localDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let {
            return localDateFormatter.parse(value, LocalDate::from)
        }
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(localDateFormatter)
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
    fun courseLightFromString(courseString:String?): Course? {
        return courseString?.let{
            val courseType = object : TypeToken<Course>() {}.type
            Gson().fromJson<Course>(courseString, courseType)}
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
    fun longListToString(splits: List<Long>?): String?{
        return splits?.joinToString(separator = ",")
    }

    @TypeConverter
    fun longListFromString(splitsString:String?): List<Long>? {
        return if(splitsString.isNullOrBlank()){
             listOf()
        }else{
             splitsString.split(",").map { it.toLong() }
        }
    }

    @TypeConverter
    fun courseLightToString(course: Course?): String?{
        return course?.let { Gson().toJson(course)}
    }


    @TypeConverter fun intToGender(genderInt: Int): Gender?{
        return Gender.fromInt(genderInt)
    }

    @TypeConverter fun genderToInt(gender: Gender): Int{
        return gender.ordinal
    }

    @TypeConverter fun intToStatus(statusInt: Int): TimeTrialStatus?{
        return TimeTrialStatus.fromInt(statusInt)
    }

    @TypeConverter fun statusToInt(status: TimeTrialStatus): Int{
        return status.ordinal
    }

    @TypeConverter fun stringToNumberRule(str: String): NumberRules?{
        return NumberRules.fromString(str)
    }

    @TypeConverter fun numberRulesToString(rules: NumberRules): String{
        return NumberRules.toString(rules)
    }
}