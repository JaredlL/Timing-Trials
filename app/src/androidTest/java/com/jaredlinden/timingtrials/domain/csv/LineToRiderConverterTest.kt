package com.jaredlinden.timingtrials.domain.csv

import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LineToRiderConverterTest {

    @Before
    fun setUp() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        AndroidThreeTen.init(context)
    }

    @Test
    fun should_importRiderDetails_when_csvIsValid() {
        // [arrange]
        var heading = "firstname,lastname,club,time"
        var line = "jared,linden,RDFCC,23:40"

        val riderImporter = LineToResultRiderConverter()
        riderImporter.setHeading(heading)

        // [act]
        var import = riderImporter.importLine(line.split(","))

        // [assert]
        assertEquals("jared", import?.firstName)
        assertEquals("linden", import?.lastName)
    }
}