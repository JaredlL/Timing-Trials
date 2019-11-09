package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.ui.SelectableCourseData
import com.android.jared.linden.timingtrials.ui.SelectableCourseViewModel
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*


interface ISelectCourseViewModel{
    fun getAllCourses(): LiveData<SelectableCourseData>
    fun setSelectedCourse(course: Course)

class SelectCourseViewModelImpl(private val ttSetup: SetupViewModel): ISelectCourseViewModel {


    private fun selectedCourse(): Course? {
        return ttSetup.timeTrial.value?.timeTrialHeader?.course
    }


    override fun getAllCourses(): LiveData<SelectableCourseData> = Transformations.switchMap(ttSetup.timeTrial) {
        Transformations.map(ttSetup.courseRepository.allCoursesLight) { courseList ->
            SelectableCourseData(courseList.map { c -> SelectableCourseViewModel(c) }, selectedCourse()?.id)
        }
    }


    override fun setSelectedCourse(course: Course) {

        if (selectedCourse()?.id != course.id) {
            val oldCourseName = selectedCourse()?.courseName ?: ""

            ttSetup.timeTrial.value?.let { ttd ->
                val tt = ttd.timeTrialHeader
                if (tt.ttName == "") {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val dateString = ZonedDateTime.now().format(formatter)

                    ttSetup.updateDefinition(tt.copy(ttName = course.courseName + " " + dateString, course = course))

                } else if (oldCourseName != "" && tt.ttName.contains(oldCourseName, false)) {

                    val oldTtName = tt.ttName
                    val newDef = tt.copy(
                            ttName = oldTtName.replace(oldCourseName, course.courseName),
                            course = course)

                    ttSetup.updateDefinition(newDef)
                } else {
                    ttSetup.updateDefinition(tt.copy(course = course))
                }
            }
        }
    }
}
}
