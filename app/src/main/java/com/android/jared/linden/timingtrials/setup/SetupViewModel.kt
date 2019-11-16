package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
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


    val timeTrial = MediatorLiveData<TimeTrial>().apply { addSource(timeTrialRepository.getNonFinishedTimeTrial()) { res ->
        res?.let {tt->
            if(!isCarolineAlive.get() && value !=tt){
                System.out.println("JAREDMSG -> SETUPVIEWMODEL setting new livedata value ${tt.timeTrialHeader}, Rider Count = ${tt.riderList.count()}")
                value = tt
            }
        }
    }
    }



    var queue = ConcurrentLinkedQueue<TimeTrial>()
    private var isCarolineAlive = AtomicBoolean()

    fun updateTimeTrial(newtt: TimeTrial){
        //timeTrial.value = newtt
        if(timeTrial.value != newtt){
            timeTrial.value = newtt

            if(!isCarolineAlive.get()){
                queue.add(newtt)
                viewModelScope.launch(Dispatchers.IO) {
                    isCarolineAlive.set(true)
                    while (queue.peek() != null){
                        var ttToInsert = queue.peek()
                        while (queue.peek() != null){
                            ttToInsert = queue.poll()
                        }
                        timeTrialRepository.update(ttToInsert)
                    }
                    isCarolineAlive.set(false)
                }
            }else{
                queue.add(newtt)
            }
        }
    }



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
                updateTimeTrial( tt.helper.addRidersAsTimeTrialRiders(mutList))
            }


        }
        override fun getOrderableRiders(): LiveData<List<Rider>> = Transformations.map(timeTrial){it.riderList.map { r -> r.rider }}
    }

    override val selectCourseViewModel: ISelectCourseViewModel = ISelectCourseViewModel.SelectCourseViewModelImpl(this)
    override val selectRidersViewModel: ISelectRidersViewModel = SelectRidersViewModelImpl(this)
    override val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel = TimeTrialPropertiesViewModelImpl(this)
    override val setupConformationViewModel: ISetupConformationViewModel = SetupConfirmationViewModel(this)




    init {

        /**
         * Need to remember which ids were selected when a rider is added/removed
         * Also need to update selected riders if they are modfied in the DB
         */
        timeTrial.addSource(riderRepository.allRidersLight) { result: List<Rider>? ->
            result?.let {newRiders->
                timeTrial.value?.let {ttdef->
                    val currentSelected = ttdef.riderList.map { r -> r.rider }
                    if(currentSelected.count() > 0){

                        val oldSelected: LinkedHashMap<Long, Rider> =  LinkedHashMap(currentSelected.associateBy { r -> r.id ?: 0 })
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