package com.jaredlinden.timingtrials.testutils

import com.jaredlinden.timingtrials.util.ConverterUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Parameterized::class)
class ConverterUtilsTest(
    private val inputMillis: Long,
    private val expectedString: String,
    private val testCaseName: String
) {

    @Test
    fun should_formatReadableString_when_millisecondsProvided() {
        val actualString = ConverterUtils.toSecondMinuteHour(inputMillis)
        Assert.assertEquals("Test Case: $testCaseName", expectedString, actualString)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{2}") // Uses the 3rd parameter (testCaseName) for the test display name
        fun data(): Collection<Array<Any>> {
            return listOf(
                // Millisecond tests
                arrayOf(1L, "0.001 sec", "MS: 1ms"),
                arrayOf(30L, "0.03 sec", "MS: 30ms"),
                arrayOf(34L, "0.03 sec", "MS: 34ms"),
                arrayOf(150L, "0.1 sec", "MS: 150ms"),
                arrayOf(38L, "0.03 sec", "MS: 38ms"),
                arrayOf(56L, "0.05 sec", "MS: 56ms"),
                arrayOf(180L, "0.1 sec", "MS: 180ms"),

                // Second tests
                arrayOf(1000L, "1 sec", "SEC: 1000ms (1s)"),
                arrayOf(1300L, "1.3 sec", "SEC: 1300ms (1.3s)"),
                arrayOf(12450L, "12.4 sec", "SEC: 12450ms (12.4s)"),
                arrayOf(9999L, "9.9 sec", "SEC: 9999ms (9.9s)"),
                arrayOf(20000L, "20 sec", "SEC: 20000ms (20s)"),

                // Minute tests
                arrayOf(60000L, "1 min 0 sec", "MIN: 60000ms (1m 0s)"),
                arrayOf(63300L, "1 min 3 sec", "MIN: 63300ms (1m 3s)"),
                arrayOf(120000L, "2 min 0 sec", "MIN: 120000ms (2m 0s)"),
                arrayOf(119999L, "1 min 59 sec", "MIN: 119999ms (1m 59s)"),
            )
        }
    }

}

