package com.android.jared.linden.timingtrials.data

data class TimeTrialResult(val timeTrialRider:TimeTrialRider, val splits: List<Long>, val timeTrial: TimeTrial){
    val totalTime = splits.sum()

    val category: RiderCategoryStandard = timeTrialRider.getCategory(timeTrial.timeTrialHeader.startTime)


}
