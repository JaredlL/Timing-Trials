package com.android.jared.linden.timingtrials.data

import org.threeten.bp.OffsetDateTime

data class CourseRecord(val riderId: Long?, val riderName: String, val timeTrialId: Long?, val club: String, val category: RiderCategoryStandard, val timeMillis: Long, val dateTime: OffsetDateTime? = null){

}

enum class RecordType{
    ABSOLUTE,
    GENDER,
    CATEGORY,
    NONE
}

class CourseRecordHelper(val courseRecords: List<CourseRecord>){


    fun getRecordType(result: TimeTrialResult){
        val rider = result.timeTrialRider.rider
        val courseRecord = courseRecords.sortedByDescending { it.timeMillis }.firstOrNull()
        val genderRecord = if(rider.gender == Gender.MALE || rider.gender == Gender.FEMALE) courseRecords.filter { it.category.gender == rider.gender }.sortedByDescending { it.timeMillis }.firstOrNull() else null
        val categoryRecord = courseRecords.firstOrNull { it.category.categoryId() == rider.getCategoryStandard().categoryId() }
    }

}