package com.android.jared.linden.timingtrials.timing

import android.os.Debug
import android.util.Log
import androidx.core.util.size
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.RiderPassedEvent
import com.android.jared.linden.timingtrials.domain.TimeLine
import com.android.jared.linden.timingtrials.testutils.AndroidTestObjects
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import org.threeten.bp.Instant

class TimingViewModelTest{

    @Test
    fun getStatusStringTest() {

        val tt = AndroidTestObjects.createTestTimeTrial()

        val now = Instant.now()
        var millisSinceStart = now.toEpochMilli() - tt.timeTrialHeader.startTime.toInstant().toEpochMilli()

        Debug.startMethodTracing("Predict")

        var start = System.currentTimeMillis()
        var sString = getStatusString(millisSinceStart, tt)
        var tll = TimeLine(tt, millisSinceStart)
        var tl = tll.timeLine.last().eventType.name
        var tot = System.currentTimeMillis() - start
        System.out.println(tot.toString())



        millisSinceStart = now.toEpochMilli() - tt.timeTrialHeader.startTime.toInstant().toEpochMilli()
        start = System.currentTimeMillis()
        sString = getStatusString(millisSinceStart, tt)
        tl = tll.timeLine.last().eventType.name
        tot = System.currentTimeMillis() - start
        System.out.println(tot.toString())

        millisSinceStart = now.toEpochMilli() - tt.timeTrialHeader.startTime.toInstant().toEpochMilli()
        start = System.currentTimeMillis()
        sString = getStatusString(millisSinceStart, tt)
        tl = tll.timeLine.last().eventType.name
        tot = System.currentTimeMillis() - start
        System.out.println(tot.toString())


        Debug.stopMethodTracing()


    }

    private fun getStatusString(millisSinceStart: Long, tte: TimeTrial): String{

        val ttIntervalMilis: Long = tte.timeTrialHeader.interval * 1000L
        val sparse = tte.helper.sparseRiderStartTimes
        val index = sparse.indexOfKey(millisSinceStart)
        val prevIndex = if(index >= 0){ index }else{ Math.abs(index) - 2 }
        val nextIndex = prevIndex + 1

        //val ridersWhoShouldHaveStarted = tte.helper.riderStartTimes.headMap(millisSinceStart)
        //val nextRiderStart = tte.helper.riderStartTimes.tailMap(millisSinceStart)

        val ss = tte.helper.sparseRiderStartTimes.indexOfKey(millisSinceStart)

        if(nextIndex < tte.helper.sparseRiderStartTimes.size){

            //If we are more than 1 min before TT start time
            val nextStartMilli = sparse.keyAt(nextIndex)
            if((nextStartMilli - millisSinceStart) > 60000){
                return "TimeTrial starts at 0:00:00:0"
            }

            val nextStartRider = sparse.valueAt(nextIndex)
            val millisToNextRider = (nextStartMilli - millisSinceStart)

            val riderString = "(${nextStartRider.number}) ${nextStartRider.rider.firstName} ${nextStartRider.rider.lastName}"
            return when(millisToNextRider){
                in ttIntervalMilis - 3000..ttIntervalMilis ->
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