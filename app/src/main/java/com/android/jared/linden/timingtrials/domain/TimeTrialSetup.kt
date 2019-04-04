package com.android.jared.linden.timingtrials.domain

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import java.util.*
import javax.inject.Inject



//class TimeTrialSetup(private val timetrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository, private val courseRepository: ICourseRepository)
//    : ITimeTrialRepository by timetrialRepository, IRiderRepository by riderRepository, ICourseRepository by courseRepository{
//
//    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()
//    val selectedOrderedRiders: MediatorLiveData<List<Rider>> = MediatorLiveData()
//    val selectedCourse: MediatorLiveData<Course> = MediatorLiveData()
//
//
//    init {
//
//        timeTrial.addSource(timetrialRepository.getSetupTimeTrial()){tt->
//            timeTrial.value = tt
//        }
//        selectedCourse.addSource(timeTrial){tt->
//            selectedCourse.value = tt.course
//        }
//        timeTrial.addSource(selectedCourse){
//            timeTrial.value?.course = it
//        }
//
//        /**
//         * Need to remember which ids were selected when a rider is added/removed
//         * Also need to update selected riders if they are modfied in the DB
//         */
//        selectedOrderedRiders.value = (listOf())
//        selectedOrderedRiders.addSource(allRiders) { result: List<Rider>? ->
//            result?.let {
//
//                val oldSelected:LinkedHashMap<Long, Rider> =  LinkedHashMap(selectedOrderedRiders.value?.associateBy { r -> r.id ?: 0 })
//                val retainedIds: MutableSet<Long> = mutableSetOf()
//                result.forEach{rider ->
//                   rider.id?.let {id ->
//                       if (oldSelected.containsKey(id)) {
//                           //Update selected rider details
//                           oldSelected[id] = rider
//                           retainedIds.add(id)
//                       }
//                   }
//                }
//
//                //Only keep riders which are still in the DB
//                val newList = oldSelected.filter { i -> (retainedIds.contains(i.key))}.values.toList()
//                selectedOrderedRiders.postValue(newList)
//                timeTrial.value?.riders = newList
//            }
//        }
//
//    }
//
//    fun riderSelectionChangeHandler(rider:Rider, sel:Boolean){
//        val oldList = selectedOrderedRiders.value?.toMutableList()?: ArrayList()
//        val newList = ArrayList<Rider>()
//        oldList.forEach { r -> if(r.id != rider.id){newList.add(r)}}
//
//        if(sel){newList.add(rider)}
//
//        selectedOrderedRiders.postValue(newList)
//        timeTrial.value?.riders = newList
//    }
//
//    fun riderIsSelected(rider:Rider): Boolean{
//        return selectedOrderedRiders.value?.map { r -> r.id }?.contains(rider.id) ?: false
//    }
//
//
//}


