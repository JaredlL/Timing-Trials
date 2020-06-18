package com.jaredlinden.timingtrials.setup

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.roomrepo.ICourseRepository
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface ITimeTrialSetupViewModel {
    val orderRidersViewModel: IOrderRidersViewModel
    val selectCourseViewModel: ISelectCourseViewModel
    val selectRidersViewModel: ISelectRidersViewModel
    val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel
}



class SetupViewModel @Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository,
        val timeTrialRiderRepository: TimeTrialRiderRepository
) : ViewModel(), ITimeTrialSetupViewModel {


    private val _mTimeTrial = MediatorLiveData<TimeTrial?>()
    val timeTrial: LiveData<TimeTrial?> = Transformations.map(_mTimeTrial){
        it
    }


    private val currentId: MutableLiveData<Long?> = MutableLiveData()

    private val idSwitcher = Transformations.switchMap(currentId){
        it?.let {ttId->
            timeTrialRepository.getSetupTimeTrialById(ttId)
        }
    }

    fun changeTimeTrial(timeTrialId: Long){
        if(currentId.value != timeTrialId){
            currentId.value = timeTrialId
        }
    }



    init {
        _mTimeTrial.addSource(idSwitcher) { res ->
            res?.let { tt ->
                val current = _mTimeTrial.value
                val ordered = tt.copy(riderList = tt.riderList.sortedBy { it.timeTrialData.index })
                if (!isCarolineAlive.get() && ordered != current) {
                    _mTimeTrial.value = ordered
                }
            }
        }



    }


    private val queue = ConcurrentLinkedQueue<TimeTrial>()
    private var isCarolineAlive = AtomicBoolean()

    fun updateTimeTrial(newTimeTrial: TimeTrial) {

        val previousTimeTrial = _mTimeTrial.value
        _mTimeTrial.value = newTimeTrial
        if (previousTimeTrial != null) {
            _mTimeTrial.value = newTimeTrial

            if (!isCarolineAlive.get()) {
                queue.add(newTimeTrial)
                viewModelScope.launch(Dispatchers.IO) {
                    isCarolineAlive.set(true)
                    while (queue.peek() != null) {
                        var ttToInsert = queue.peek()
                        while (queue.peek() != null) {
                            ttToInsert = queue.poll()
                        }
                        ttToInsert?.let { timeTrialRepository.updateFull(it) }

                    }
                    isCarolineAlive.set(false)
                }
            } else {
                queue.add(newTimeTrial)
            }
        }
    }


    override val orderRidersViewModel: IOrderRidersViewModel = OrderRidersViewModel(this)
    override val selectCourseViewModel: ISelectCourseViewModel = ISelectCourseViewModel.SelectCourseViewModelImpl(this)
    override val selectRidersViewModel: ISelectRidersViewModel = SelectRidersViewModelImpl(this)
    override val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel = TimeTrialPropertiesViewModelImpl(this)


    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        isCarolineAlive.set(false)
        viewModelScope.cancel()
    }
}