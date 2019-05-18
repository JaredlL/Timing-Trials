package com.android.jared.linden.timingtrials.data

data class TimeTrialResult(val timeTrialRider:TimeTrialRider, val splits: List<Long>){
    val totalTime = splits.sum()


}
