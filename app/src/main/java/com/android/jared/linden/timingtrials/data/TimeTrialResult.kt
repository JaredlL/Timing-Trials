package com.android.jared.linden.timingtrials.data

data class TimeTrialResult(val timeTrialRider:TimeTrialRider,
                           val splits: List<Long>,
                           val timeTrial: TimeTrial){
    val totalTime = splits.sum()
    val category: RiderCategoryStandard = timeTrialRider.getCategory(timeTrial.timeTrialHeader.startTime)



}




interface IRiderResult {
    val rider: Rider
    val time: Long
    val course: Course
    val timeTrial: TimeTrialHeader?
    val note: String
}

//data class RiderResult(override val rider: RiderLight, override val time: Long, override val course: CourseLight, override val timeTrial: TimeTrialHeader? = null, override val note: String) : IRiderResult
