package com.jaredlinden.timingtrials.domain.csv

import com.jaredlinden.timingtrials.data.Gender
import com.jaredlinden.timingtrials.util.LengthConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

val RIDER = "rider"
val FIRSTNAME = "firstname"
val SURNAME = "surname"
val LASTNAME = "last name"
val CLUB = "club"
val CATEGORY = "category"
val DATE = "date"
val LAPS = "laps"
val DESCRIPTION = "description"
val NOTES = "note"
val COURSE = "course"
val CTTNAME = "ctt"
val DISTANCE = "distance"
val LENGTH = "length"
val TIME = "time"
val TIME_TRIAL_NAME = "time trial"
val GENDER = "gender"
val SPLIT = "split"

object ObjectFromString
{

    fun time(valString: String): Long?{
        val splitAtPoint = valString.split(".")
        return if(splitAtPoint.size > 1){

            val ms = splitAtPoint.last().let { ".$it" }.toDoubleOrNull()?.let { it * 1000}?.toInt()?:0

            val splits = splitAtPoint.first().split(":").reversed()
            val sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
            val min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
            val hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0

            val sum = (hour + min + sec + ms).toLong()
            val resultMillis = if(sum > 0) sum else null
            resultMillis

        }else{
            val splits = splitAtPoint.first().split(":").reversed()
            var ms = 0
            var sec = 0
            var min = 0
            var hour = 0

            if(splits.firstOrNull()?.length == 1){
                ms = splits.getOrNull(0)?.let { ".$it" }?.toDoubleOrNull()?.let { it * 1000}?.toInt()?:0
                sec = splits.getOrNull(1)?.toIntOrNull()?.times(1000)?:0
                min = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60)?:0
                hour = splits.getOrNull(3)?.toIntOrNull()?.times(1000 * 60 * 60)?:0
            }else{
                sec = splits.getOrNull(0)?.toIntOrNull()?.times(1000)?:0
                min = splits.getOrNull(1)?.toIntOrNull()?.times(1000 * 60)?:0
                hour = splits.getOrNull(2)?.toIntOrNull()?.times(1000 * 60 * 60)?:0
            }
            val sum = (hour + min + sec + ms).toLong()
            val resultMillis = if(sum > 0) sum else null
            resultMillis
        }
    }

    fun gender(valString: String): Gender{
        return when {
            valString.equals("male", true) -> { Gender.MALE }
            valString.equals("female", true) -> { Gender.FEMALE }
            valString.equals("m", true) -> { Gender.MALE }
            valString.equals("f", true) -> { Gender.FEMALE }
            valString.equals("other", true) -> { Gender.OTHER }
            else -> Gender.UNKNOWN
        }
    }

    fun distance(valString: String): Double?{
        return LengthConverter.stringToInternalUnits(valString)
    }

    val formatList = listOf("d/m/y", "d-M-y","d/M/y", "d M y", "d MMMM yyyy")
    fun date(valString: String): LocalDate?{
        var date: LocalDate? = null
        for(pattern in formatList){
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern)
                date = LocalDate.parse(valString, formatter)
                break
            }catch(e:Exception) {
                // todo - notify user, dont squash general exceptions
            }
        }
        if(date == null){
            val cleanedDateString = valString.split(" ").mapIndexed { index, s -> if(index == 0) Regex("[^0-9]").replace(s, "") else s  }.joinToString(" ")
            for(pattern in formatList){
                try {
                    val formatter = DateTimeFormatter.ofPattern(pattern)
                    date = LocalDate.parse(cleanedDateString, formatter)
                    break
                }catch(e:Exception) {
                    // todo - notify user, dont squash general exceptions
                }
            }
        }
        return date
    }
}