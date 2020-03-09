package com.android.jared.linden.timingtrials.testutils

import androidx.core.util.size
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.source.TimingTrialsDatabase
import com.android.jared.linden.timingtrials.util.ConverterUtils
import org.junit.Assert
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit



class TextUtils{

    @Test
    fun testMsString(){

        var s = ConverterUtils.toSecondMinuteHour(1L)
        assert(s == "0.001 sec")

        s = ConverterUtils.toSecondMinuteHour(30L)
        assert(s == "0.03 sec")

        s = ConverterUtils.toSecondMinuteHour(34L)
        assert(s == "0.03 sec")

        s = ConverterUtils.toSecondMinuteHour(150L)
        assert(s == "0.1 sec")

        s = ConverterUtils.toSecondMinuteHour(38L)
        assert(s == "0.03 sec")

        s = ConverterUtils.toSecondMinuteHour(56L)
        assert(s == "0.05 sec")

        s = ConverterUtils.toSecondMinuteHour(180L)
        assert(s == "0.1 sec")
    }

    @Test
    fun testSecondString(){

        var s = ConverterUtils.toSecondMinuteHour(1000L)
        assert(s == "1 sec")

        s = ConverterUtils.toSecondMinuteHour(1300L)
        assert(s == "1.3 sec")

        s = ConverterUtils.toSecondMinuteHour(12450)
        assert(s == "12.4 sec")

        s = ConverterUtils.toSecondMinuteHour(9999)
        assert(s == "9.9 sec")

        s = ConverterUtils.toSecondMinuteHour(20000)
        assert(s == "20 sec")

    }

    @Test
    fun testMinuteString(){

        var s = ConverterUtils.toSecondMinuteHour(60000)
        assert(s == "1 min 0 sec")

        s = ConverterUtils.toSecondMinuteHour(63300)
        assert(s == "1 min 3 sec")

        s = ConverterUtils.toSecondMinuteHour(120000)
        assert(s == "2 min 0 sec")

        s = ConverterUtils.toSecondMinuteHour(119999)
        assert(s == "1 min 59 sec")

        s = ConverterUtils.toSecondMinuteHour(20000)
        assert(s == "20 sec")

    }

}

