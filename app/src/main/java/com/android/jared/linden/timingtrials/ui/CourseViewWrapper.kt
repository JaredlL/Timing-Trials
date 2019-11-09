package com.android.jared.linden.timingtrials.ui
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.util.ConverterUtils
import java.math.BigDecimal
import java.math.RoundingMode


data class SelectableCourseData(val courses: List<SelectableCourseViewModel>, val selectedId: Long?)

open class SelectableCourseViewModel(val course: Course){
    open var convertedLengthString: String = BigDecimal(ConverterUtils.toLengthDisplayUnit(course.length)).setScale(3, RoundingMode.HALF_EVEN).toString()
}