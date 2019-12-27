package com.android.jared.linden.timingtrials.ui

import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.domain.TimeLine
import com.android.jared.linden.timingtrials.util.ConverterUtils
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId


enum class RiderStatus {
    NOT_STARTED, RIDING, FINISHED, DNF, DNS


}

class RiderStatusViewWrapper(val rider: TimeTrialRider, val timeLine: TimeLine){

    val number: String = rider.number.toString()

    val startTimeMilis: Long = timeLine.timeTrial.timeTrialHeader.startTimeMilis + timeLine.timeTrial.helper.getRiderStartTime(rider)
    //val b = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMilis), ZoneId.systemDefault())
    val startTimeDisplay: String = ConverterUtils.toSecondsDisplayString(timeLine.timeTrial.helper.getRiderStartTime(rider))

    var onPressedCallback: (TimeTrialRider) -> Unit ={}

    fun onPressed(){
        onPressedCallback(rider)
    }

    val status = timeLine.getRiderStatus(rider)
   //fun riderStatus(): RiderStatus {
    //   return  timeLine.getRiderStatus(rider)
    //}
}