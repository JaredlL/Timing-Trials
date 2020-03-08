package com.android.jared.linden.timingtrials.domain

import com.android.jared.linden.timingtrials.data.Course
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.util.ConverterUtils
import com.android.jared.linden.timingtrials.util.LengthConverter

class Filter(val filterString: String) {

    fun passes(rider: Rider): Boolean{
        return rider.fullName().contains(filterString, ignoreCase = true) || rider.club.contains(filterString, ignoreCase = true) || rider.category.contains(filterString, ignoreCase = true)
    }

    fun passes(course: Course): Boolean{
        return  course.courseName.contains(filterString, ignoreCase = true) || course.cttName.contains(filterString, ignoreCase = true)
    }

    fun passes(timeTrial: TimeTrialHeader): Boolean{
        return timeTrial.ttName.contains(filterString, ignoreCase = true) || ConverterUtils.dateToDisplay(timeTrial.startTime).contains(filterString, ignoreCase = true)
    }

}