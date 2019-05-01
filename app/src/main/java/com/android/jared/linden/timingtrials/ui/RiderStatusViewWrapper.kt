package com.android.jared.linden.timingtrials.ui


import android.graphics.Color
import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider

enum class RiderStatus {
    NOT_STARTED, RIDING, FINISHED
}

class RiderStatusViewWrapper(val rider: TimeTrialRider, val timeTrial: TimeTrial){

    val number: String = rider.number?.toString()?: "NaN"

    private val riderEvents = timeTrial.eventList.filter { it.riderId == rider.id }

    fun riderStatus(): RiderStatus {
        var status = RiderStatus.NOT_STARTED
        if(riderEvents.any { it.eventType == EventType.RIDER_FINISHED }) return RiderStatus.FINISHED
        if(riderEvents.any { it.eventType == EventType.RIDER_STARTED }) return RiderStatus.RIDING
        return status
    }



}