package com.jaredlinden.timingtrials.domain.csv

import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.threeten.bp.Month

class LineToTimeTrialConverterTest {

    @Before
    fun setUp() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        AndroidThreeTen.init(context)
    }

    @Test
    fun importLine() {
        val headingLine = "timetrial name,timetrial date,laps"
        var dataLine = "Vintage Tankard,10/08/2018,2"

        val ttReader = LineToTimeTrialConverter()

        ttReader.setHeading(headingLine)
        var tt = ttReader.importLine(dataLine.split(","))

        assertEquals(tt?.laps, 2)
        assertEquals(tt?.startTime?.year, 2018)
        assertEquals(tt?.startTime?.month, Month.AUGUST)
        assertEquals(tt?.startTime?.dayOfMonth, 10)

         dataLine = "Vintage Tankard,10-08-2018,2"

        ttReader.setHeading(headingLine)
         tt = ttReader.importLine(dataLine.split(","))

        assertEquals(tt?.laps, 2)
        assertEquals(tt?.startTime?.year, 2018)
        assertEquals(tt?.startTime?.month, Month.AUGUST)
        assertEquals(tt?.startTime?.dayOfMonth, 10)

    }
}