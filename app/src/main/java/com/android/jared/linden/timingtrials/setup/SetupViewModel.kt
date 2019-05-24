package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

interface ITimeTrialSetupViewModel{
    val orderRidersViewModel: IOrderRidersViewModel
    val selectCourseViewModel: ISelectCourseViewModel
    val selectRidersViewModel: ISelectRidersViewModel
    val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel
    val setupConformationViewModel: ISetupConformationViewModel
}

class SetupViewModel @Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository
) : ViewModel(), ITimeTrialSetupViewModel{



    //val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()

    //var syncTt: TimeTrial? = null
    val timeTrial = MediatorLiveData<TimeTrial>().apply { addSource(timeTrialRepository.getSetupTimeTrial()) { res ->
        if (res != null) {
            if(value != res){
                value = res
            }

        } else {
            updateTimeTrial(TimeTrial.createBlank())
        }
    }
    }

    val riderMed = MediatorLiveData<Rider>()

    fun updateTimeTrial(newtt: TimeTrial){
        //timeTrial.value = newtt
        if(timeTrial.value != newtt){
            timeTrial.value = newtt
            viewModelScope.launch(Dispatchers.IO) {
                timeTrialRepository.insertOrUpdate(newtt)
            }
        }

    }

//    fun updateUiTimeTrial(newtt: TimeTrial){
//        //timeTrial.value = newtt
//        if(syncTt != newtt){
//            timeTrial.value = newtt
//            syncTt = newtt
//            viewModelScope.launch(Dispatchers.IO) {
//                timeTrialRepository.insertOrUpdate(newtt)
//                syncTt = newtt
//            }
//        }
//
//    }

    fun updateDefinition(ttHeader: TimeTrialHeader){
        timeTrial.value?.let {
            updateTimeTrial(it.copy(timeTrialHeader = ttHeader))
        }
    }

    override val orderRidersViewModel: IOrderRidersViewModel = object: IOrderRidersViewModel {
        override fun moveItem(fromPosition: Int, toPosition: Int) {
            timeTrial.value?.let { tt->
                val mutList = tt.riderList.map { it.rider }.toMutableList()

                if(fromPosition > toPosition){
                    mutList.add(toPosition, mutList[fromPosition])
                    mutList.removeAt(fromPosition + 1)
                }else{
                    mutList.add(toPosition + 1, mutList[fromPosition])
                    mutList.removeAt(fromPosition)
                }

                //Collections.swap(mutList, fromPosition, toPosition)


                    //it.riderList = mutList
                updateTimeTrial( tt.helper.addRidersAsTimeTrialRiders(mutList))

            }


        }
        override fun getOrderableRiders(): LiveData<List<RiderLight>> = Transformations.map(timeTrial){it.riderList.map { r -> r.rider }}
    }

    override val selectCourseViewModel: ISelectCourseViewModel = SelectCourseViewModelImpl(this)
    override val selectRidersViewModel: ISelectRidersViewModel = SelectRidersViewModelImpl(this)
    override val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel = TimeTrialPropertiesViewModelImpl(this)
    override val setupConformationViewModel: ISetupConformationViewModel = SetupConfirmationViewModel(this)

    fun insertTt(){
        viewModelScope.launch(Dispatchers.IO) {
            timeTrial.value?.let { timeTrialRepository.insertOrUpdate(it) }
        }
    }

    fun initialise(timeTrialId: Long){
        if(timeTrial.value == null){

//                timeTrial.addSource(timeTrialRepository.getSetupTimeTrial()){ tt->
//                    if(tt == null){
//                        timeTrial.value = TimeTrial.createBlank()
//                    }else{
//                        timeTrial.value = tt
//                    }
//                }
            }

    }


    init {

        /**
         * Need to remember which ids were selected when a rider is added/removed
         * Also need to update selected riders if they are modfied in the DB
         */
        timeTrial.addSource(riderRepository.allRidersLight) { result: List<RiderLight>? ->
            result?.let {newRiders->
                timeTrial.value?.let {ttdef->
                    val currentSelected = ttdef.riderList.map { r -> r.rider }
                    if(currentSelected.count() > 0){

                        val oldSelected: LinkedHashMap<Long, RiderLight> =  LinkedHashMap(currentSelected.associateBy { r -> r.id ?: 0 })
                        val retainedIds: MutableSet<Long> = mutableSetOf()
                        newRiders.forEach{rider ->
                            rider.id?.let {id ->
                                if (oldSelected.containsKey(id)) {
                                    //Update selected rider details
                                    oldSelected[id] = rider
                                    retainedIds.add(id)
                                }
                            }
                        }
                        //Only keep riders which are still in the DB
                        val newList = oldSelected.filter { i -> (retainedIds.contains(i.key))}.values.toList()
                        ttdef.let {
                           // it.riderList = newList.mapIndexed { index, r-> TimeTrialRider(r, it.timeTrialHeader.id, index+1,(60 + index * it.timeTrialHeader.interval).toLong()) }
                            val ml = newList.mapIndexed { index, r-> TimeTrialRider(r, it.timeTrialHeader.id?:0L, index+1) }
                            updateTimeTrial(it.copy(riderList = ml))
                        }
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}