package com.android.jared.linden.timingtrials.domain

import android.os.Build.VERSION_CODES.M
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.RoomCourseRepository

class RecordChecker(val timeTrial: TimeTrial,val riderRepository: IRiderRepository, val courseRepository: ICourseRepository) {

    val notesMap: Map<Long, MutableLiveData<String>> = timeTrial.riderList.asSequence().mapNotNull { it.rider.id }.associate { r -> Pair(r, MutableLiveData<String>() ) }

    val ridersToUpdate = mutableListOf<Rider>()
    var courseToUpdate: Course? = null

    suspend fun checkRecords(){
        val courseId = timeTrial.timeTrialHeader.course?.id
        val results = timeTrial.helper.results
       if(courseId != null && results.isNotEmpty()){

           val course = courseRepository.getCourseSuspend(courseId)
           val idList = timeTrial.riderList.mapNotNull { it.rider.id }
           val riderList = riderRepository.ridersFromIds(timeTrial.riderList.mapNotNull { it.rider.id })
           //val results = timeTrial.helper.results
           val updatingCourseRecords = course.courseRecords.toMutableList()

           riderList.forEach { rider ->
               val result = results.first { it.timeTrialRider.rider.id == rider.id }
               val pb = rider.personalBests.firstOrNull{it.courseId == course.id}
               var note = ""
               if (pb != null) {
                   if(result.totalTime < pb.millisTime) {
                       val newPbs = rider.personalBests.filter { it.courseId != course.id }.toMutableList()
                       newPbs.add(PersonalBest(courseId, timeTrial.timeTrialHeader.id, course.courseName, course.length, result.totalTime, timeTrial.timeTrialHeader.startTime))
                       ridersToUpdate.add(rider.copy(personalBests = newPbs))
                       note = "New PB!"
                   }
               }else{
                   val newPbs = rider.personalBests.filter { it.courseId != course.id }.toMutableList()
                   newPbs.add(PersonalBest(courseId, timeTrial.timeTrialHeader.id, course.courseName, course.length, result.totalTime, timeTrial.timeTrialHeader.startTime))
                   ridersToUpdate.add(rider.copy(personalBests = newPbs))
               }

               val helper = CourseRecordHelper(updatingCourseRecords)
               val rt = helper.getRecordType(result)
               when(rt){
                   RecordType.ABSOLUTE -> note = "Course Record!"
                   RecordType.GENDER -> note = "${rider.gender.fullString()} CR!"
                   RecordType.CATEGORY -> note = "${rider.getCategoryStandard().categoryId()} CR"
               }
               if(rt!= RecordType.NONE){
                   val index = updatingCourseRecords.indexOfFirst{cr -> cr.category.categoryId() != rider.getCategoryStandard().categoryId()}
                   if(index >= 0){
                       updatingCourseRecords.removeAt(index)
                   }

                   updatingCourseRecords.add(CourseRecord(rider.id, rider.fullName(), timeTrial.timeTrialHeader.id, rider.club, rider.getCategoryStandard(), result.totalTime, timeTrial.timeTrialHeader.startTime))
               }
               if(note != ""){
                   notesMap[rider.id]?.postValue(note)
               }
           }
           results.forEach {result->
               val helper = CourseRecordHelper(updatingCourseRecords)
               val rt = helper.getRecordType(result)
               when(rt){
                   //RecordType.ABSOLUTE -> note = "Course Record!"
                  // RecordType.GENDER -> note = "${result.timeTrialRider.rider.gender.fullString()} CR!"
                  // RecordType.CATEGORY -> note = "${result.timeTrialRider.rider.getCategoryStandard().categoryId()} CR"
               }
           }
           courseToUpdate = course.copy(courseRecords = updatingCourseRecords)
       }

    }

}