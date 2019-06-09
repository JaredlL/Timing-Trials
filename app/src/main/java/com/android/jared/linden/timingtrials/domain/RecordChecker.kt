package com.android.jared.linden.timingtrials.domain

import android.os.Build.VERSION_CODES.M
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.RoomCourseRepository

class RecordChecker(val timeTrial: TimeTrial,val riderRepository: IRiderRepository, val courseRepository: ICourseRepository) {

    val notesMap: Map<Long, MutableLiveData<String>> = timeTrial.riderList.asSequence().mapNotNull { it.id }.associate { r -> Pair(r, MutableLiveData<String>() ) }

    val ridersToUpdate = mutableListOf<Rider>()

    suspend fun checkRecords(){
        val courseId = timeTrial.timeTrialHeader.course?.id
       if(courseId != null){

           val course = courseRepository.getCourseSuspend(courseId)
           val riderList = riderRepository.ridersFromIds(timeTrial.riderList.mapNotNull { it.rider.id })
           val results = timeTrial.helper.results

           var updatingCourseRecords = course.courseRecords.toMutableList()

           riderList.forEach { rider ->
               val result = results.first { it.timeTrialRider.id == rider.id }
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


               val courseRecord = updatingCourseRecords.sortedByDescending { it.timeMillis }.firstOrNull()
               val genderRecord = if(rider.gender == Gender.MALE || rider.gender == Gender.FEMALE) updatingCourseRecords.filter { it.category.gender == rider.gender }.sortedByDescending { it.timeMillis }.firstOrNull() else null
               val categoryRecord = updatingCourseRecords.firstOrNull { it.category.categoryId() == rider.getCategoryStandard().categoryId() }

               if(courseRecord == null || courseRecord.timeMillis > result.totalTime){
                   updatingCourseRecords.r
                   updatingCourseRecords.add(CourseRecord(rider.id, rider.fullName(), timeTrial.timeTrialHeader.id, rider.club, rider.getCategoryStandard(), result.totalTime, timeTrial.timeTrialHeader.startTime))

               }else if (genderRecord == null || genderRecord.timeMillis > result.totalTime){

                   updatingCourseRecords.add(CourseRecord(rider.id, rider.fullName(), timeTrial.timeTrialHeader.id, rider.club, rider.getCategoryStandard(), result.totalTime, timeTrial.timeTrialHeader.startTime))

               }else if(categoryRecord == null || categoryRecord.timeMillis > result.totalTime){

                   updatingCourseRecords.add(CourseRecord(rider.id, rider.fullName(), timeTrial.timeTrialHeader.id, rider.club, rider.getCategoryStandard(), result.totalTime, timeTrial.timeTrialHeader.startTime))

               }
           }

       }

    }

}