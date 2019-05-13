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

class SelectCourseViewModelImpl(private val ttSetup: SetupViewModel): ISelectCourseViewModel{



    //private val timeTrialDef = Transformations.map(ttSetup.timeTrial){it.timeTrialHeader}
    //private val selectedCourse = ttSetup.timeTrial.value?.timeTrialHeader?.course

    private fun selectedCourse(): Course? { return ttSetup.timeTrial.value?.timeTrialHeader?.course }

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

            ttSetup.timeTrial.value?.let { ttd->
                val tt = ttd.timeTrialHeader
                if(tt.ttName == ""){

                    //Todo: Use Threeten
                    val f = SimpleDateFormat("dd/MM/yy")
                    val c = Calendar.getInstance()
                    val formatString = f.format(c.time)

                    ttSetup.updateDefinition(tt.copy(ttName = course.courseName + " " + formatString, course = course))

                }else if( oldCourseName != ""  && tt.ttName.contains(oldCourseName, false)){

                    val oldTtName = tt.ttName
                   val newDef = tt.copy(
                        ttName = oldTtName.replace(oldCourseName, course.courseName),
                        course = course)

                    ttSetup.updateDefinition(newDef)
                }
                else{
                    ttSetup.updateDefinition(tt.copy(course = course))
                }
            }

            mCourseWrapperList.value?.let { cv ->
                cv.forEach{ it.notifyPropertyChanged(BR.courseIsSelected)}
            }
        }

        courseSelected()

    }

}
