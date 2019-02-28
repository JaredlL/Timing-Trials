package com.android.jared.linden.timingtrials.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.android.jared.linden.timingtrials.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CourseListViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()

    // By default all the coroutines launched in this scope should be using the Main dispatcher

    private val scope = CoroutineScope(Dispatchers.Main + parentJob)

    private val repository: CourseRepository
    // Using LiveData and caching what getAllRiders returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private val mCourseList: LiveData<List<Course>>



    init {
        val courseDao = TimingTrialsDatabase.getDatabase(application, scope).courseDao()
        repository = CourseRepository(courseDao)
        mCourseList =  repository.allCourses
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */

    fun insertOrUpdate(course: Course) = scope.launch(Dispatchers.IO) {
        if(course.name != ""){
            repository.insertOrUpdate(course)
        }

    }

    fun deleteCourse(course: Course) = scope.launch(Dispatchers.IO) {
        repository.delete(course)
    }




    var mSelectable = false

    fun setSelectable(value: Boolean){
        mSelectable = value
    }

    fun  getSelectable(): Boolean{
        return  mSelectable
    }

    var editCourse = {(course): Course -> Unit}

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}