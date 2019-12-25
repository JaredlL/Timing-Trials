package com.android.jared.linden.timingtrials.domain.csv

import org.junit.Test

import org.junit.Assert.*
import org.threeten.bp.Month

class LineToTimeTrialConverterTest {


    @Test
    fun importLine() {

        val headingLine = "timetrial name,timetrial date,laps"
        var dataLine = "Vintage Tankard,10/08/2018,2"

        val ttReader = LineToTimeTrialConverter()

        ttReader.setHeading(headingLine)
        var tt = ttReader.importLine(dataLine)

        assertEquals(tt?.laps, 2)
        assertEquals(tt?.startTime?.year, 2018)
        assertEquals(tt?.startTime?.month, Month.AUGUST)
        assertEquals(tt?.startTime?.dayOfMonth, 10)

         dataLine = "Vintage Tankard,10-08-2018,2"

        ttReader.setHeading(headingLine)
         tt = ttReader.importLine(dataLine)

        assertEquals(tt?.laps, 2)
        assertEquals(tt?.startTime?.year, 2018)
        assertEquals(tt?.startTime?.month, Month.AUGUST)
        assertEquals(tt?.startTime?.dayOfMonth, 10)

    }
}