package com.jaredlinden.timingtrials.domain

import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.util.ConverterUtils

class Filter(val filterString: String) {

    fun passes(rider: Rider): Boolean{
        return rider.fullName().contains(filterString, ignoreCase = true) ||
                rider.club.contains(filterString, ignoreCase = true) ||
                rider.category.contains(filterString, ignoreCase = true)
    }

    fun passes(course: Course): Boolean{
        return  course.courseName.contains(filterString, ignoreCase = true) ||
                course.cttName.contains(filterString, ignoreCase = true)
    }

    fun passes(timeTrial: TimeTrialHeader): Boolean{
        return timeTrial.ttName.contains(filterString, ignoreCase = true) ||
                ConverterUtils.dateToDisplay(timeTrial.startTime).contains(filterString, ignoreCase = true)
    }
}