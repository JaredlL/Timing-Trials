package com.android.jared.linden.timingtrials.util

import androidx.databinding.BindingConversion
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object ConverterUtils{

    val lengthDisplayUnitConversion = 1 / 1609.34

    fun toLengthDisplayUnit(length: Double): Double {
        return length * lengthDisplayUnitConversion
    }

    fun instantToSecondsDisplayString(instant: Instant): String{
        val  f:DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
        return (f.format(instant))
    }

    @BindingConversion
    @JvmStatic
    fun dateToDisplay(dateTime: OffsetDateTime): String{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return dateTime.format(formatter)
    }


    fun toSecondsDisplayString(milliseconds: Long): String{
        val millis = abs(milliseconds)
        val secs =  (millis/1000)
        return String.format("%d:%02d:%02d", secs / 3600, (secs % 3600) / 60, (secs % 60))
    }

    fun toTenthsDisplayString(milliseconds: Long): String{
        if(milliseconds == Long.MAX_VALUE) return ""
        val milis = abs(milliseconds)
        val secs =  (milis/1000)
        return String.format("%d:%02d:%02d.%1d", secs / 3600, (secs % 3600) / 60, (secs % 60),  (milis % 1000) / 100)
    }




}