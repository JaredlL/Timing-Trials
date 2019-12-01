package com.android.jared.linden.timingtrials.timing

import android.os.Debug
import android.util.Log
import androidx.core.util.size
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.RiderPassedEvent
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.domain.TimeLine
import com.android.jared.linden.timingtrials.testutils.AndroidTestObjects
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit

class TimingViewModelAndroidTest{

    @Test
    fun getStatusStringTest() {

        val tt = AndroidTestObjects.createTestTimeTrial()

        val header1 = tt.timeTrialHeader.copy(startTime =  OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.MINUTES).plusSeconds(10), ZoneId.systemDefault()))
        val header2 = tt.timeTrialHeader.copy(startTime =  OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.MINUTES).plusSeconds(10), ZoneId.systemDefault()))
        val header3 = tt.timeTrialHeader.copy(startTime =  OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.MINUTES).plusSeconds(10), ZoneId.systemDefault()))

        val tt1 = tt.copy(timeTrialHeader = header1)
        val tt2 = tt.copy(timeTrialHeader = header2)
        val tt3 = tt.copy(timeTrialHeader = header3)


        Debug.startMethodTracing("Predict")

        var millisSinceStart1 = Instant.now().toEpochMilli() - tt1.timeTrialHeader.startTime.toInstant().toEpochMilli()
        millisSinceStart1 = -4000
        var sString1 = getStatusString(millisSinceStart1, tt1)


        var millisSinceStart2 = Instant.now().toEpochMilli() - tt2.timeTrialHeader.startTime.toInstant().toEpochMilli()
        millisSinceStart2 = -3000
        var sString2 = getStatusString(millisSinceStart2, tt2)


        var millisSinceStart3 = Instant.now().toEpochMilli() - tt3.timeTrialHeader.startTime.toInstant().toEpochMilli()
        millisSinceStart3 = -1500
        var sString3 = getStatusString(millisSinceStart3, tt3)

        millisSinceStart3 = -500
        var sString4 = getStatusString(millisSinceStart3, tt3)

        millisSinceStart3 = -200
        var sString5 = getStatusString(millisSinceStart3, tt3)

        millisSinceStart3 = 10
        var sString6 = getStatusString(millisSinceStart3, tt3)

        millisSinceStart3 = 1000
        var sString7 = getStatusString(millisSinceStart3, tt3)
        millisSinceStart3 = 2000
        var sString8 = getStatusString(millisSinceStart3, tt3)
        Debug.stopMethodTracing()


    }

    @Test
    fun getStatusString2() {

        val now = Instant.now()
        val startTime = OffsetDateTime.ofInstant (now.minusMillis(58000), ZoneId.systemDefault())
        val timeTrialHeader = AndroidTestObjects.createTestHeader().copy(startTime = startTime)
        var timeTrial = TimeTrial(timeTrialHeader, listOf(), listOf())
        timeTrial = timeTrial.helper.addRidersAsTimeTrialRiders(AndroidTestObjects.createRiderList())


        val millisSinceStart = now.toEpochMilli() - timeTrial.timeTrialHeader.startTime.toInstant().toEpochMilli()

        val sString = getStatusString(millisSinceStart, timeTrial)
        System.out.println(sString)
    }

    private fun getStatusString(millisSinceStart: Long, tte: TimeTrial): String{

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