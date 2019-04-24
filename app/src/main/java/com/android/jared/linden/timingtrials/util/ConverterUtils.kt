package com.android.jared.linden.timingtrials.util

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

    fun instantTenthsDisplayString(instant: Instant): String{
        val  f:DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:S").withZone(ZoneId.systemDefault())
        return (f.format(instant))
    }


}