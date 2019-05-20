package com.android.jared.linden.timingtrials.domain


import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.RiderPassedEvent
import com.android.jared.linden.timingtrials.testutils.AndroidTestObjects


import org.junit.Test


import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class AndroidTimeTrialHelperTest{

    @Test
    fun getSparseRiderStartTimes() {
        val tt = AndroidTestObjects.createTestTimeTrial()
        val expected = (tt.timeTrialHeader.firstRiderStartOffset + (tt.timeTrialHeader.interval * 3)) * 1000L
        val time = tt.helper.sparseRiderStartTimes
        val ind = time.get(expected)
        val dex = time.indexOfKey(expected)
        assertEquals( 3, dex)
        assertEquals("Lauren", ind.rider.firstName)

    }

    @Test
    fun getSparseIndexes() {
        val tt = AndroidTestObjects.createTestTimeTrial()
        val current = (tt.timeTrialHeader.firstRiderStartOffset + (4 * tt.timeTrialHeader.interval)) * 1000L + 300
        val time = tt.helper.sparseRiderStartTimes
        val ind = time.indexOfKey(current)
        assertEquals(-6, ind)

        val abs = Math.abs(ind) - 1
        val nextIndex = tt.helper.sparseRiderStartTimes.keyAt(abs)
        val nextRider = tt.helper.sparseRiderStartTimes[nextIndex]
        assertEquals("Earl", nextRider.rider.firstName)

        val prevIndex = tt.helper.sparseRiderStartTimes.keyAt(abs - 1)
        val prevRider = tt.helper.sparseRiderStartTimes[prevIndex]
        assertEquals("Steve", prevRider.rider.firstName)
    }

    @Test
    fun assignRiderToEvent(){
        var tt = AndroidTestObjects.createTestTimeTrial()

        val eveList1 = tt.eventList.filterNot { it.riderId == 5L && it.eventType == EventType.RIDER_PASSED }

        tt = tt.copy(eventList = eveList1)



        //Number 6 Earl Smith, index = 5
        val rider6StartTime = (tt.timeTrialHeader.firstRiderStartOffset + 5 * tt.timeTrialHeader.interval) * 1000L

        val eventToTry = RiderPassedEvent(eventType = EventType.RIDER_PASSED, riderId = null, timeStamp = rider6StartTime + 30, id = 99, timeTrialId = tt.timeTrialHeader.id?:0)
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

    fun insertEvent(event: RiderPassedEvent, tt: TimeTrial): TimeTrial{
        val mutList = tt.eventList.toMutableList()
        mutList.add(event.copy(timeTrialId = tt.timeTrialHeader.id?:0))
        return tt.copy(eventList = mutList)
    }

}
