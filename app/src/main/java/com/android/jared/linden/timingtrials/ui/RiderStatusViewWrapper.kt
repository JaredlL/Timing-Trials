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


    private val riderEvents = timeTrial.eventList.filter { it.riderId == rider.rider.id }

    var onPressedCallback: (TimeTrialRider) -> Unit ={}

    fun onPressed(){
        onPressedCallback(rider)
    }

   fun riderStatus(): RiderStatus {
       return  timeTrial.getRiderStatus(rider.rider.id?:0)
    }



}