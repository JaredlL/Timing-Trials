package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.ICourseRepository
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject


class ListViewModel @Inject constructor(private val courseRepository: ICourseRepository,
                                              private val riderRepository: IRiderRepository,
                                              val timeTrialRepository: ITimeTrialRepository) : ViewModel() {


    private fun getCourseWrapperList(): LiveData<List<CourseListViewWrapper>>{
       return Transformations.map(courseRepository.allCourses){c -> c.map { CourseListViewWrapper(it)
       }}}

    val allCourses: LiveData<List<CourseListViewWrapper>> = getCourseWrapperList()

    val allRiders = riderRepository.allRiders
    val allTimeTrials = timeTrialRepository.allTimeTrials

}

