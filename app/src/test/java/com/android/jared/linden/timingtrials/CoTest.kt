package com.android.jared.linden.timingtrials

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Test

import org.junit.Assert.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.system.measureTimeMillis

class CoTest {

    @Test
    fun dateTimeFormattertest(){

        val dateString = "09/10/1990"

        val dateTimeFormatter = DateTimeFormatter.ofPattern("d/M/y")

        val date = LocalDate.parse(dateString, dateTimeFormatter)

        val b = date.year
    }
}