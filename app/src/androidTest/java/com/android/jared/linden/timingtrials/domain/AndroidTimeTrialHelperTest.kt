package com.android.jared.linden.timingtrials.domain


import androidx.test.runner.AndroidJUnit4
import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialEvent
import com.android.jared.linden.timingtrials.testutils.AndroidTestObjects


import org.junit.Test


import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class AndroidTimeTrialHelperTest{

    @Test
    fun getSparseRiderStartTimes() {
        val tt = AndroidTestObjects.createTestTimeTrial()
        val expected = (tt.timeTrialHeader.firstRiderStartOffset + 4 * tt.timeTrialHeader.interval) * 1000L
        val time = tt.helper.sparseRiderStartTimes
        val ind = time.get(expected)
        val dex = time.indexOfKey(expected)
        assertEquals(ind.rider.firstName, "Lauren")
        assertEquals(dex, 3)
    }

    @Test
    fun getSparseIndexes() {
        val tt = AndroidTestObjects.createTestTimeTrial()
        val current = (tt.timeTrialHeader.firstRiderStartOffset + 4 * tt.timeTrialHeader.interval) * 1000L + 300
        val time = tt.helper.sparseRiderStartTimes
        val ind = time.indexOfKey(current)
        assertEquals(ind, -5)

        val abs = Math.abs(ind) - 1
        val nextIndex = tt.helper.sparseRiderStartTimes.keyAt(abs)
        val nextRider = tt.helper.sparseRiderStartTimes[nextIndex]
        assertEquals(nextRider.rider.firstName, "Steve")

        val prevIndex = tt.helper.sparseRiderStartTimes.keyAt(abs - 1)
        val prevRider = tt.helper.sparseRiderStartTimes[prevIndex]
        assertEquals(prevRider.rider.firstName, "Lauren")
    }

    @Test
    fun assignRiderToEvent(){
        val tt = AndroidTestObjects.createTestTimeTrial()



        //Number 6 Earl Smith, index = 5
        val rider6StartTime = (tt.timeTrialHeader.firstRiderStartOffset + 6 * tt.timeTrialHeader.interval) * 1000L

        val eventToTry = TimeTrialEvent(eventType = EventType.RIDER_PASSED, riderId = null, timeStamp = rider6StartTime + 30, id = 99, timeTrialId = tt.timeTrialHeader.id?:0)
        val updated = insertEvent(eventToTry, tt)

        val res = updated.helper.assignRiderToEvent(riderId =  5L, eventTimestamp =  eventToTry.timeStamp)
        assertEquals("Rider must have started", res.message)



        val res2 = updated.helper.assignRiderToEvent(riderId =  7L, eventTimestamp =  eventToTry.timeStamp)
        assertEquals("Rider has already finished", res2.message)

        val newEvs = updated.eventList.filterNot { it.riderId == 7L && it.eventType == EventType.RIDER_PASSED }
        val updated2 = updated.copy(eventList = newEvs)

        val res3 = updated2.helper.assignRiderToEvent(riderId =  7L, eventTimestamp =  eventToTry.timeStamp)
        assertTrue(res3.succeeded)



    }

    fun insertEvent(event: TimeTrialEvent, tt: TimeTrial): TimeTrial{
        val mutList = tt.eventList.toMutableList()
        mutList.add(event.copy(timeTrialId = tt.timeTrialHeader.id?:0))
        return tt.copy(eventList = mutList)
    }

}
