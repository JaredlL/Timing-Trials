package com.jaredlinden.timingtrials.ui
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.data.ITimingTrialsEntity
import com.jaredlinden.timingtrials.util.LengthConverter

data class SelectableCourseData(val courses: List<Course>, val selectedId: Long?)

open class SelectableCourseViewModel(
    val nameString: String,
    val distString: String,
    val cttNameString: String,
    override val id: Long? = null): ITimingTrialsEntity
{
    val course: Course = Course(nameString, 0.0, cttNameString, id)
    constructor(course: Course, converter: LengthConverter):
            this (
                course.courseName,
                course.length?.let {
                    converter.lengthToDisplay(course.length)
                } ?: "", course.cttName, course.id)
}