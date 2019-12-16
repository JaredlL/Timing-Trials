package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import kotlinx.coroutines.*
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


    private val _mTimeTrial = MediatorLiveData<TimeTrial?>()
    val timeTrial: LiveData<TimeTrial?> = Transformations.map(_mTimeTrial){tt->
       tt
    }
    init {
        _mTimeTrial.addSource(timeTrialRepository.nonFinishedFullTimeTrial) { res ->
            res?.let {tt->
                val current = _mTimeTrial.value
                if(!isCarolineAlive.get() && tt != current){
                    System.out.println("JAREDMSG -> SETUPVIEWMODEL -> current data = ${_mTimeTrial.value?.timeTrialHeader?.id} new = ${tt.timeTrialHeader.id}")
                    _mTimeTrial.value = tt
            }
        }
    }
    }


    var queue = ConcurrentLinkedQueue<TimeTrial>()
    private var isCarolineAlive = AtomicBoolean()

    fun updateTimeTrial(newTimeTrial: TimeTrial){

        val previousTimeTrial = _mTimeTrial.value
        _mTimeTrial.value = newTimeTrial
        if(previousTimeTrial != null){
            _mTimeTrial.value = newTimeTrial

            if(!isCarolineAlive.get()){
                queue.add(newTimeTrial)
                viewModelScope.launch(Dispatchers.IO) {
                    isCarolineAlive.set(true)
                    while (queue.peek() != null){
                        var ttToInsert = queue.peek()
                        while (queue.peek() != null){
                            ttToInsert = queue.poll()
                        }
                        ttToInsert?.let {  timeTrialRepository.updateFull(it)}

                    }
                    isCarolineAlive.set(false)
                }
            }else{
                queue.add(newTimeTrial)
            }
        }
    }


    override val orderRidersViewModel: IOrderRidersViewModel = object: IOrderRidersViewModel {
        override fun moveItem(fromPosition: Int, toPosition: Int) {
            _mTimeTrial.value?.let { currentTimeTrial->
                val mutList = currentTimeTrial.riderList.toMutableList()

                if(fromPosition > toPosition){
                    mutList.add(toPosition, mutList[fromPosition])
                    mutList.removeAt(fromPosition + 1)
                }else{
                    mutList.add(toPosition + 1, mutList[fromPosition])
                    mutList.removeAt(fromPosition)
                }
                val updateList = mutList.mapIndexed { i,r-> r.copy(timeTrialData = r.timeTrialData.copy(index = i)) }
                updateTimeTrial(currentTimeTrial.updateRiderList(updateList))
            }


        }

        override fun getOrderableRiderData(): LiveData<TimeTrial?> {
           return _mTimeTrial
        }
    }

    override val selectCourseViewModel: ISelectCourseViewModel = ISelectCourseViewModel.SelectCourseViewModelImpl(this)
    override val selectRidersViewModel: ISelectRidersViewModel = SelectRidersViewModelImpl(this)
    override val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel = TimeTrialPropertiesViewModelImpl(this)
    override val setupConformationViewModel: ISetupConformationViewModel = SetupConfirmationViewModel(this)


    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        System.out.println("JAREDMSG -> SETUPVIEWMODEL CLEARING")
        isCarolineAlive.set(false)
        viewModelScope.cancel()
    }
}