package com.jaredlinden.timingtrials.testutils

import androidx.core.util.size
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.source.TimingTrialsDatabase
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit

object AndroidTestObjects{
    fun createTestTimeTrial(): TimeTrial{
        return createMockEvents(TimeTrial.createBlank())
    }

    fun createTestHeader(): TimeTrialHeader {
        return TimeTrialHeader.createBlank()
    }

    fun createMockEvents(timeTrial: TimeTrial):TimeTrial{

        return timeTrial
    }

    fun createTestCourse():Course{
        return Course.createBlank().copy(courseName = "Test Course")
    }

    fun createRiderList():List<Rider>{
        val mList = mutableListOf<Rider>()
        return mList.mapIndexed { index, rider ->  rider.copy(id = index * 1L) }
    }
}

