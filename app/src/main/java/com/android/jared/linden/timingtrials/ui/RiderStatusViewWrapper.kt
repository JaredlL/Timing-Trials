package com.android.jared.linden.timingtrials.ui

import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.domain.TimeLine
import com.android.jared.linden.timingtrials.util.ConverterUtils



enum class RiderStatus {
    NOT_STARTED, RIDING, FINISHED, DNF, DNS


}

class RiderStatusViewWrapper(val filledRider: FilledTimeTrialRider, val timeLine: TimeLine){

    val rider = filledRider.timeTrialData
    val number: String = rider.index.toString()

    val startTimeMilis: Long = timeLine.timeTrial.timeTrialHeader.startTimeMilis + timeLine.timeTrial.helper.getRiderStartTime(rider)
    //val b = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMilis), ZoneId.systemDefault())
    val startTimeDisplay: String = ConverterUtils.toSecondsDisplayString(timeLine.timeTrial.helper.getRiderStartTime(rider))

    var onPressedCallback: (TimeTrialRider) -> Unit ={}

    fun onPressed(){
        onPressedCallback(rider)
    }

    val status = timeLine.getRiderStatus(rider)

}