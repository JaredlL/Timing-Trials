package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.BR
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper
import java.text.SimpleDateFormat
import java.util.*


interface ISelectCourseViewModel{
    fun getAllCourses(): LiveData<List<CourseListViewWrapper>>
    var courseSelected: () -> Unit
}

class SelectCourseViewModelImpl(private val ttSetup: TimeTrialSetupViewModel): ISelectCourseViewModel{


    private fun selectedCourse() = ttSetup.timeTrial.value?.course

    private val mCourseWrapperList: LiveData<List<CourseListViewWrapper>>
            = Transformations.map(ttSetup.courseRepository.allCourses){ list -> list.map {course -> CourseListViewWrapper(course).apply {
        getSelected = {c -> selectedCourse()?.id == c.id}
        onSet = ::onCourseSelected
    } }}


    override var courseSelected: () -> Unit = { Unit}

    override fun getAllCourses(): LiveData<List<CourseListViewWrapper>> = mCourseWrapperList

    private fun onCourseSelected(course: Course) {

        if(selectedCourse()?.id != course.id){

            val oldCourseName = selectedCourse()?.courseName?: ""

            ttSetup.timeTrial.value?.let {tt->
                if(tt.ttName == ""){
                    val f = SimpleDateFormat("dd/MM/yy")
                    val c = Calendar.getInstance()
                    val formatString = f.format(c.time)

                    ttSetup.timeTrial.value = ttSetup.timeTrial.value.apply {
                        tt.ttName = course.courseName + " " + formatString
                        tt.course = course
                    }
                }else if( oldCourseName != ""  && tt.ttName.contains(oldCourseName, false)){

                    val oldTtName = tt.ttName
                    ttSetup.timeTrial.value = ttSetup.timeTrial.value.apply {
                        tt.ttName = oldTtName.replace(oldCourseName, course.courseName)
                        tt.course = course
                    }
                }
                else{
                    ttSetup.timeTrial.value = ttSetup.timeTrial.value.apply {
                        tt.course = course
                    }
                }
            }

            mCourseWrapperList.value?.let { cv ->
                cv.forEach{ it.notifyPropertyChanged(BR.courseIsSelected)}
            }
        }

        courseSelected()

    }

}
