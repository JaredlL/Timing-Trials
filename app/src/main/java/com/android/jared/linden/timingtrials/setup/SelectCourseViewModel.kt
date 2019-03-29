package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.BR
import com.android.jared.linden.timingtrials.domain.TimeTrialSetup
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SelectCourseViewModel @Inject constructor(private val timeTrialSetup: TimeTrialSetup) : ViewModel() {

    private var parentJob = Job()

    // By default all the coroutines launched in this scope should be using the Main dispatcher

    private val scope = CoroutineScope(Dispatchers.Main + parentJob)

    // Using LiveData and caching what getAllRiders returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.

    private val mCourseWrapperList: LiveData<List<CourseListViewWrapper>>
            = Transformations.map(timeTrialSetup.allCourses){ list -> list.map {course -> CourseListViewWrapper(course).apply {
        getSelected = {c -> selectedCourse.value?.course?.id == c.id}
        onSet = ::setOnSelected
    } }}

    fun getAllCourses(): LiveData<List<CourseListViewWrapper>> {
        return mCourseWrapperList
    }

    fun setOnSelected(sCourse: Course){

        if(selectedCourse.value?.course?.id != sCourse.id){

            val oldCourseName = selectedCourse.value?.course?.courseName?: ""

            timeTrialSetup.timeTrial.value?.let {tt->
                if(tt.ttName == ""){
                    val f = SimpleDateFormat("dd/mm/yy")
                    val c = Calendar.getInstance()
                    val formatString = f.format(c.time)

                    timeTrialSetup.timeTrial.value = timeTrialSetup.timeTrial.value.apply { tt.ttName = sCourse.courseName + " " + formatString}
                }else if( oldCourseName != ""  && tt.ttName.contains(oldCourseName, false)){

                    val oldTtName = tt.ttName
                    timeTrialSetup.timeTrial.value = timeTrialSetup.timeTrial.value.apply { tt.ttName = oldTtName.replace(oldCourseName, sCourse.courseName)}
                }
            }

            selectedCourse.value = CourseListViewWrapper(sCourse)
            timeTrialSetup.selectedCourse.value = sCourse

            mCourseWrapperList.value?.let { cv ->
                cv.forEach{ it.notifyPropertyChanged(BR.courseIsSelected)}
            }
        }

        courseSelected()
    }

    var courseSelected: () -> Unit = { Unit}


    val selectedCourse: MediatorLiveData<CourseListViewWrapper> = MediatorLiveData()

    init {

        timeTrialSetup.selectedCourse.value?.let{selectedCourse.value = CourseListViewWrapper(it)}
        selectedCourse.addSource(timeTrialSetup.selectedCourse){result:Course? ->
            if (result == null) {
                insertOrUpdate(Course("New Course", 1000.0, ""))
                //selectedCourse.value = CourseListViewWrapper(Course("", 0.0, ""))
            } else {
                selectedCourse.value = CourseListViewWrapper(result)
            }
        }
    }



    private fun insertOrUpdate(course: Course) = scope.launch(Dispatchers.IO) {
        if(course.courseName != ""){
            timeTrialSetup.insertOrUpdate(course)
        }

    }

    var editCourse = {(course): Course -> Unit}

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}