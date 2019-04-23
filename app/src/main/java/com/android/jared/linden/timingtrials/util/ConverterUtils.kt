package com.android.jared.linden.timingtrials.util

import org.threeten.bp.Instant
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

object ConverterUtils{

    val lengthDisplayUnitConversion = 1 / 1609.34

    fun toLengthDisplayUnit(length: Double): Double {
        return length * lengthDisplayUnitConversion
    }

    fun instantToSecondsDisplayString(date: Instant): String{
        val  f:Format = SimpleDateFormat("HH:mm:ss")
        return (f.format(date))
    }

    fun instantTenthsDisplayString(date: Instant): String{
        val  f:Format = SimpleDateFormat("HH:mm:ss:S")
        return (f.format(date))
    }


}