package com.android.jared.linden.timingtrials.util

import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

object ConverterUtils{

    val lengthDisplayUnitConversion = 1 / 1609.34

    fun toLengthDisplayUnit(length: Double): Double {
        return length * lengthDisplayUnitConversion
    }

    fun instantToSecondsDisplayString(instant: Instant): String{
        val  f:DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
        return (f.format(instant))
    }

    fun toTenthsDisplayString(instant: Instant): String{
        val  f:DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:S").withZone(ZoneId.systemDefault())
        return (f.format(instant))
    }

    fun toTenthsDisplayString(duration:Duration): String{
        val milis =Math.abs(duration.toMillis())
        val secs = Math.abs(duration.seconds)
        return String.format("%d:%02d:%02d:%1d", secs / 3600, (secs % 3600) / 60, (secs % 60),  (milis % 1000) / 100)
    }



}