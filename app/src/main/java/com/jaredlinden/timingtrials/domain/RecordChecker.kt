package com.jaredlinden.timingtrials.domain

//class RecordChecker(val _mTimeTrial: TimeTrial,val riderRepository: IRiderRepository, val courseRepository: ICourseRepository) {
//
//    val notesMap: Map<Long, MutableLiveData<String>> = _mTimeTrial.riderList.asSequence().mapNotNull { it.rider.id }.associate { r -> Pair(r, MutableLiveData<String>() ) }
//
//    val ridersToUpdate = mutableListOf<Rider>()
//    var courseToUpdate: Course? = null
//
//    suspend fun checkRecords(){
//        val courseId = _mTimeTrial.timeTrialHeader.course?.id
//        val results = _mTimeTrial.helper.results
//       if(courseId != null && results.isNotEmpty()){
//
//           val course = courseRepository.getCourseSuspend(courseId)
//           val riderList = riderRepository.ridersFromIds(_mTimeTrial.riderList.mapNotNull { it.rider.id })
//           val updatingCourseRecords = course.courseRecords.associate { cr -> Pair(cr.category.categoryId(), cr) }.toMutableMap()
//
//           riderList.forEach { rider ->
//               val result = results.first { it.timeTrialRider.rider.id == rider.id }
//               val pb = rider.allResults.firstOrNull{it.courseId == course.id}
//               if (pb != null) {
//                   if(result.totalTime < pb.millisTime) {
//                       val newPbs = rider.allResults.filter { it.courseId != course.id }.toMutableList()
//                       newPbs.add(PersonalBest(courseId, _mTimeTrial.timeTrialHeader.id, course.courseName, course.length, result.totalTime, _mTimeTrial.timeTrialHeader.startTime))
//                       ridersToUpdate.add(rider.copy(allResults = newPbs))
//                   }
//               }else{
//                   val newPbs = rider.allResults.filter { it.courseId != course.id }.toMutableList()
//                   newPbs.add(PersonalBest(courseId, _mTimeTrial.timeTrialHeader.id, course.courseName, course.length, result.totalTime, _mTimeTrial.timeTrialHeader.startTime))
//                   ridersToUpdate.add(rider.copy(allResults = newPbs))
//               }
//
//
//               if(updatingCourseRecords[rider.getCategoryStandard().categoryId()]?.timeMillis?: Long.MAX_VALUE > result.totalTime){
//                   updatingCourseRecords[rider.getCategoryStandard().categoryId()] = CourseRecord(rider.id, rider.fullName(), _mTimeTrial.timeTrialHeader.id, rider.club, rider.getCategoryStandard(), result.totalTime, _mTimeTrial.timeTrialHeader.startTime)
//               }
//           }
//           results.forEach {result->
//               val helper = CourseRecordHelper(updatingCourseRecords.map { it.value })
//               val rt = helper.getRecordType(result)
//               result.timeTrialRider.rider.id?.let { id->
//                   val note = when(rt){
//                       RecordType.ABSOLUTE -> "Course Record!"
//                       RecordType.GENDER -> "${result.timeTrialRider.rider.gender.fullString()} CR!"
//                       RecordType.CATEGORY -> "${result.timeTrialRider.rider.getCategoryStandard().categoryId()} CR"
//                       RecordType.NONE-> null
//                   }
//                  note?.let { notesMap[id]?.postValue(it)}
//               }
//
//           }
//           courseToUpdate = course.copy(courseRecords = updatingCourseRecords.map { it.value })
//       }
//
//    }
//
//}