package com.android.jared.linden.timingtrials.ui

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.domain.TimeLine

enum class RiderStatus {
    NOT_STARTED, RIDING, FINISHED
}

class RiderStatusViewWrapper(val rider: TimeTrialRider, val timeLine: TimeLine){

    val number: String = rider.number.toString()

    var onPressedCallback: (TimeTrialRider) -> Unit ={}

    fun onPressed(){
        onPressedCallback(rider)
    }

   fun riderStatus(): RiderStatus {
       return  timeLine.getRiderStatus(rider)
    }
}