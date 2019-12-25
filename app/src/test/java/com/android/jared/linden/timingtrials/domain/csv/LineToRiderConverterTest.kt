package com.android.jared.linden.timingtrials.domain.csv

import org.junit.Test

import org.junit.Assert.*

class LineToRiderConverterTest {

    @Test
    fun importLine() {

        var heading = "firstname,lastname,club,time"
        var line = "jared,linden,RDFCC,23:40"

        val riderImporter = LineToRiderConverter()
        riderImporter.setHeading(heading)
        var import = riderImporter.importLine(line)

        assertEquals( "jared",import?.firstName)
        assertEquals("linden", import?.lastName)

         heading = "name,club,time"
         line = "jared linden,RDFCC,23:40"

        riderImporter.setHeading(heading)
        import = riderImporter.importLine(line)

        assertEquals( "jared",import?.firstName)
        assertEquals("linden", import?.lastName)

    }
}