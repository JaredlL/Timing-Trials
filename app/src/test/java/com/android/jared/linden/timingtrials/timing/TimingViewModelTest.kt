package com.android.jared.linden.timingtrials.timing

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.testutils.TestObjects
import org.junit.Test

import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId

class TimingViewModelTest {

    @Test
    fun getStatusStringBlobs() {

        val now = Instant.now()
        val startTime = OffsetDateTime.ofInstant (now.minusMillis(58000), ZoneId.systemDefault())
        val timeTrialHeader = TestObjects.createTestHeader().copy(startTime = startTime)
        var timeTrial = TimeTrial(timeTrialHeader, listOf(), listOf())
        timeTrial = timeTrial.helper.addRidersAsTimeTrialRiders(TestObjects.createRiderList())


        val millisSinceStart = now.toEpochMilli() - timeTrial.timeTrialHeader.startTime.toInstant().toEpochMilli()

        val sString = getStatusStringBlobs(millisSinceStart, timeTrial)
        System.out.println(sString)
    }

    fun getStatusStringBlobs(millisSinceStart: Long, tte: TimeTrial): String{

        val sparse = tte.helper.sparseRiderStartTimes
        val index = sparse.indexOfKey(millisSinceStart)
        val prevIndex = if(index >= 0){ index }else{ Math.abs(index) - 2 }
        val nextIndex = prevIndex + 1
        val ttIntervalMilis = (tte.timeTrialHeader.interval * 1000L)

        val ss = tte.helper.sparseRiderStartTimes.indexOfKey(millisSinceStart)

        if(nextIndex < tte.helper.sparseRiderStartTimes.size()){

            //If we are more than 1 min before TT start time
            val nextStartMilli = sparse.keyAt(nextIndex)
            if((nextStartMilli - millisSinceStart) > 60000){
                return "${tte.timeTrialHeader.ttName} starts at 0:00:00:0"
            }

            val nextStartRider = sparse.valueAt(nextIndex)
            val millisToNextRider = (nextStartMilli - millisSinceStart)

            val riderString = "(${nextStartRider.number}) ${nextStartRider.rider.firstName} ${nextStartRider.rider.lastName}"
            return when(millisToNextRider){
                in (ttIntervalMilis - 3000)..ttIntervalMilis ->
                {
                    if(prevIndex >= 0){
                        val prevRider = sparse.valueAt(prevIndex)
                        "(${prevRider.rider.firstName} ${prevRider.rider.lastName}) GO GO GO!!!"
                    }else{
                        "Next rider is $riderString"
                    }

                }
                in 0L..10000 -> {
                    var x = millisToNextRider
                    if(x > 1000){
                        do{x /= 10} while (x > 9)
                    }else{
                        x = 0
                    }
                    "${nextStartRider.rider.firstName} ${nextStartRider.rider.lastName} - ${x+1}!"
                }
                in 5..ttIntervalMilis/4 ->
                    "$riderString starts in ${ttIntervalMilis/4000} seconds!"
                in ttIntervalMilis/4.. ttIntervalMilis/2 ->
                    "$riderString starts in ${ttIntervalMilis/2000} seconds"
                else ->
                    "Next rider is $riderString"
            }

            //return "NULL"
        }else{
            return "${tte.helper.finishedRidersFromEvents.size} riders have finished, ${tte.riderList.size - tte.helper.finishedRidersFromEvents.size} riders on course"
        }

    }
}