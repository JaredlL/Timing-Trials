package com.android.jared.linden.timingtrials.viewmodels

import androidx.databinding.Bindable
import androidx.databinding.Observable
import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.util.ObservableViewModel

class CourseViewModel(val course: Course) : ObservableViewModel() {

    @Bindable
    fun getName(): String{
        return course.name
    }

    @Bindable
    fun setName(value: String){
        course.name = value
    }

    @Bindable
    fun getLength(): Double{
        return course.length
    }

    @Bindable
    fun setLength(value: Double){
        course.length = value
    }

    @Bindable
    fun getCttName(): String{
        return course.cttname
    }

    @Bindable
    fun setCttName(value: String){
        course.cttname = value
    }
}