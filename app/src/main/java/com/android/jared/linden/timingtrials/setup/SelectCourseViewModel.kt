package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.ui.SelectableCourseData
import com.android.jared.linden.timingtrials.ui.SelectableCourseViewModel
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter


interface ISelectCourseViewModel{
    fun getAllCourses(): LiveData<SelectableCourseData>
    fun setSelectedCourse(course: Course)

class SelectCourseViewModelImpl(private val ttSetup: SetupViewModel): ISelectCourseViewModel {



    override fun getAllCourses(): LiveData<SelectableCourseData> = Transformations.switchMap(ttSetup.timeTrial) {
        Transformations.map(ttSetup.courseRepository.allCoursesLight) { courseList ->
            SelectableCourseData(courseList.map { c -> SelectableCourseViewModel(c) }, it?.course?.id)
        }
    }


    override fun setSelectedCourse(course: Course) {



            ttSetup.timeTrial.value?.let { currentTimeTrial ->
                if (currentTimeTrial.course?.id != course.id) {
                    val oldCourseName =currentTimeTrial.course?. courseName ?: ""

                    val tt = currentTimeTrial.timeTrialHeader
                    if (tt.ttName == "") {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val dateString = ZonedDateTime.now().format(formatter)
                        val newHeader = tt.copy(ttName = course.courseName + " " + dateString)
                        val newTt = currentTimeTrial.updateHeader(newHeader).updateCourse(course)
                        ttSetup.updateTimeTrial(newTt)

                    } else if (oldCourseName != "" && tt.ttName.contains(oldCourseName, false)) {

                        val oldTtName = tt.ttName
                        val newDef = tt.copy(
                                ttName = oldTtName.replace(oldCourseName, course.courseName),
                                courseId = course.id)

                        val newTtWithCourse = currentTimeTrial.updateHeader(newDef).updateCourse(course)

                        ttSetup.updateTimeTrial(newTtWithCourse)
                    } else {
                        ttSetup.updateTimeTrial(currentTimeTrial.updateCourse(course))
                    }
                }
            }
        }
    }
}

