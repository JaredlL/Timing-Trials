package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.ICourseRepository
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch



class CourseListViewModel(private val repository: ICourseRepository) : ViewModel() {

    private var parentJob = Job()

    // By default all the coroutines launched in this scope should be using the Main dispatcher

    private val scope = CoroutineScope(Dispatchers.Main + parentJob)

    // Using LiveData and caching what getAllRiders returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.

    private val mCourseWrapperList: LiveData<List<CourseListViewWrapper>>
            = Transformations.map(repository.allCourses){c -> c.map { CourseListViewWrapper(it) }}

    fun getAllCourses(): LiveData<List<CourseListViewWrapper>>{
        return mCourseWrapperList
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */

    fun insertOrUpdate(course: Course) = scope.launch(Dispatchers.IO) {
        if(course.courseName != ""){
            repository.insertOrUpdate(course)
        }

    }

    var editCourse = {(course): Course -> Unit}

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}

