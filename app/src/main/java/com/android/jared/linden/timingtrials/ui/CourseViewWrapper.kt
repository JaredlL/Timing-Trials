package com.android.jared.linden.timingtrials.ui

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.jared.linden.timingtrials.BR
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.CourseLight
import com.android.jared.linden.timingtrials.util.ConverterUtils
import java.math.BigDecimal
import java.math.RoundingMode

open class CourseListViewWrapper(var course: CourseLight) : BaseObservable(){

    var sel:Boolean = false


    var getSelected: (CourseLight) -> Boolean = { _ -> false}

    var onSet = {(course): CourseLight -> Unit}

    @Bindable
    fun getCourseIsSelected(): Boolean {
        return getSelected(course)
    }

    fun setCourseIsSelected(value: Boolean){
        if(sel != value){
            onSet(course)
            notifyPropertyChanged(BR.courseIsSelected)
        }
    }


    open var convertedLengthString: String = BigDecimal(ConverterUtils.toLengthDisplayUnit(course.length)).setScale(3, RoundingMode.HALF_EVEN).toString()
}