package com.android.jared.linden.timingtrials.ui

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider

class ResultViewWrapper(val rider: TimeTrialRider, val timeTrial: TimeTrial){

    val number: String = rider.number.toString()

    var onPressedCallback: (TimeTrialRider) -> Unit ={}

    fun onPressed(){
        onPressedCallback(rider)
    }

}