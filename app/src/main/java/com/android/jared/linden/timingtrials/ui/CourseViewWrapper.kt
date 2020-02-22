package com.android.jared.linden.timingtrials.ui
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.util.ConverterUtils
import com.android.jared.linden.timingtrials.util.LengthConverter
import java.math.BigDecimal
import java.math.RoundingMode


data class SelectableCourseData(val courses: List<Course>, val selectedId: Long?)

open class SelectableCourseViewModel(val nameString: String, val distString: String, val cttNameString: String, val id: Long? = null){

    val course: Course = Course(nameString, 0.0, cttNameString, id)

    constructor(course: Course, converter: LengthConverter): this (course.courseName, converter.lengthToDisplay(course.length), course.cttName, course.id)


}