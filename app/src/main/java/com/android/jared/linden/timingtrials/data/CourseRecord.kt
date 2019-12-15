package com.android.jared.linden.timingtrials.data

import org.threeten.bp.OffsetDateTime

data class CourseRecord(val riderId: Long?, val riderName: String, val timeTrialId: Long?, val club: String, val gender: Gender, val category: String, val timeMillis: Long, val dateTime: OffsetDateTime? = null){

}

enum class RecordType{
    ABSOLUTE,
    GENDER,
    CATEGORY,
    NONE
}

class CourseRecordHelper(private val courseRecords: List<CourseRecord>){


//    fun getRecordType(result: TimeTrialResult): RecordType{
//        val rider = result.timeTrialRider.rider
//        val courseRecord = courseRecords.minBy { it.timeMillis }
//        if(courseRecord == null || courseRecord.timeMillis >= result.totalTime){
//            return RecordType.ABSOLUTE
//        }
//
//        val genderRecord = if(rider.gender == Gender.MALE || rider.gender == Gender.FEMALE) courseRecords.filter { it.gender == rider.gender }.sortedBy { it.timeMillis }.firstOrNull() else null
//        if (genderRecord == null || genderRecord.timeMillis >= result.totalTime){
//            return RecordType.GENDER
//        }
//
//        val categoryRecord = courseRecords.firstOrNull { it.category == rider.category }
//        if(categoryRecord == null || categoryRecord.timeMillis >= result.totalTime){
//            return RecordType.CATEGORY
//        }
//        return RecordType.NONE
//    }

}