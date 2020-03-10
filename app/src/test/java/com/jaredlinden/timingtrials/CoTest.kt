package com.jaredlinden.timingtrials

import org.junit.Test

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class CoTest {

    @Test
    fun dateTimeFormattertest(){

        val dateString = "09/10/1990"

        val dateTimeFormatter = DateTimeFormatter.ofPattern("d/M/y")

        val date = LocalDate.parse(dateString, dateTimeFormatter)

        val b = date.year
    }
}