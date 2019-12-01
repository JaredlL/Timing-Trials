package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.ui.SelectableCourseViewModel
import javax.inject.Inject


class ListViewModel @Inject constructor(private val courseRepository: ICourseRepository,
                                        private val riderRepository: IRiderRepository,
                                        val timeTrialRepository: ITimeTrialRepository) : ViewModel() {



    val allCourses: LiveData<List<SelectableCourseViewModel>> = Transformations.map(courseRepository.allCoursesLight){c -> c.map { SelectableCourseViewModel(it)}}
    val allRiders = riderRepository.allRidersLight
    val allTimeTrials = timeTrialRepository.allTimeTrialsHeader

}

