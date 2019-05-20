package com.android.jared.linden.timingtrials.timing

import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.RiderPassedEvent
import com.android.jared.linden.timingtrials.testutils.AndroidTestObjects
import org.junit.Assert.*
import org.junit.Test

class TimingViewModelTest{

    @Test
    fun updateEventsTest() {

        val testTt = AndroidTestObjects.createTestTimeTrial()
        val withoutEvents = testTt.copy(eventList = listOf())

        val timeLaterThan3Riders = (testTt.timeTrialHeader.firstRiderStartOffset + testTt.timeTrialHeader.interval * 3) * 1000L + 100


        val addStarts = updateEvents(timeLaterThan3Riders, withoutEvents)

        assertEquals(3, addStarts.eventList.size)

    }


    private fun updateEvents(millisSinceStart: Long, tt: TimeTrial): TimeTrial{

        val sparse = tt.helper.sparseRiderStartTimes
        val index = sparse.indexOfKey(millisSinceStart)
        val startedIndexes = if(index >= 0){ index }else{ Math.abs(index) - 2 }
        val shouldHaveStartedIds = mutableListOf<Long>()
        var i = 0
        while (i <= startedIndexes) {
            val obj = sparse.valueAt(i)
            obj.rider.id?.let { shouldHaveStartedIds.add(it) }
            i++
        }

        val started = tt.helper.departedRidersFromEvents.asSequence().map { it.rider.id }
        val newStartingIds = shouldHaveStartedIds.asSequence().filter { !started.contains(it) }
        val newEvents = newStartingIds.map { id -> tt.helper.getRiderById(id)}.mapNotNull { it?.let {ttr->  RiderPassedEvent(tt.timeTrialHeader.id?:0, ttr.rider.id, tt.helper.getRiderStartTime(ttr), EventType.RIDER_STARTED) }  }.toList()

        if(newEvents.isNotEmpty()){ return tt.copy(eventList = tt.eventList.plus(newEvents)) }
        return  tt

    }

}