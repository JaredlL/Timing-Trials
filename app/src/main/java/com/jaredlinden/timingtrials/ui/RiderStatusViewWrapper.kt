package com.jaredlinden.timingtrials.ui

import com.jaredlinden.timingtrials.data.FilledTimeTrialRider
import com.jaredlinden.timingtrials.data.TimeTrialRider
import com.jaredlinden.timingtrials.domain.TimeLine
import com.jaredlinden.timingtrials.util.ConverterUtils



enum class RiderStatus {
    NOT_STARTED, RIDING, FINISHED, DNF, DNS


}

class RiderStatusViewWrapper(val filledRider: FilledTimeTrialRider, val timeLine: TimeLine){

    val timeTrialRider = filledRider.timeTrialData
    val number: String = timeLine.timeTrial.getRiderNumber(filledRider.riderData.id).toString()

    val startTimeMilis: Long = timeLine.timeTrial.timeTrialHeader.startTimeMilis + timeLine.timeTrial.helper.getRiderStartTime(timeTrialRider)
    //val startTimeDisplay: String = ConverterUtils.offsetToHmsDisplayString(timeLine.timeTrial.timeTrialHeader.startTime.plusSeconds((timeLine.timeTrial.timeTrialHeader.firstRiderStartOffset + timeLine.timeTrial.timeTrialHeader.interval * filledRider.timeTrialData.index).toLong()))
    val startTimeDisplay: String = ConverterUtils.offsetToHmsDisplayString(timeLine.timeTrial.timeTrialHeader.startTime?.plusSeconds(timeLine.timeTrial.helper.getRiderStartTime(timeTrialRider)/1000))
    var onPressedCallback: (TimeTrialRider) -> Unit ={}

    fun onPressed(){
        onPressedCallback(timeTrialRider)
    }

    val status = timeLine.getRiderStatus(timeTrialRider)

}