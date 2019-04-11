package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.ViewModel
import com.android.jared.linden.timingtrials.data.ICourseRepository
import com.android.jared.linden.timingtrials.data.IRiderRepository
import com.android.jared.linden.timingtrials.data.ITimeTrialRepository
import javax.inject.Inject

class ViewDataViewModel @Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository
) : ViewModel(){

    val allRiders = riderRepository.allRiders
    val allCourses = courseRepository.allCourses
    val allTimeTrials = timeTrialRepository.allTimeTrials

}