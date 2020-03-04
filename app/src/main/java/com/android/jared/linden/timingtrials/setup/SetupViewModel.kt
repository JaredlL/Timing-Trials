package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface ITimeTrialSetupViewModel {
    val orderRidersViewModel: IOrderRidersViewModel
    val selectCourseViewModel: ISelectCourseViewModel
    val selectRidersViewModel: ISelectRidersViewModel
    val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel
    val setupConformationViewModel: ISetupConformationViewModel
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


    override val orderRidersViewModel: IOrderRidersViewModel = object : IOrderRidersViewModel {
        override fun moveItem(fromPosition: Int, toPosition: Int) {
            _mTimeTrial.value?.let { currentTimeTrial ->
                val mutList = currentTimeTrial.riderList.toMutableList()

                if (fromPosition > toPosition) {
                    mutList.add(toPosition, mutList[fromPosition])
                    mutList.removeAt(fromPosition + 1)
                } else {
                    mutList.add(toPosition + 1, mutList[fromPosition])
                    mutList.removeAt(fromPosition)
                }
                val updateList = mutList.mapIndexed { i, r -> r.copy(timeTrialData = r.timeTrialData.copy(index = i)) }
                updateTimeTrial(currentTimeTrial.updateRiderList(updateList))
            }


        }

        val numberRulesMediator : MediatorLiveData<NumberRules> = MediatorLiveData()

        init {
            numberRulesMediator.addSource(_mTimeTrial){
                it?.timeTrialHeader?.numberRules?.let { nr->
                    val exString = nr.exlusionsString()
                    if(startNumber.value != nr.terminus.toString()) startNumber.value = nr.terminus.toString()
                    if(exString != exclusions.toString()) exclusions.value = exString
                }

                numberRulesMediator.addSource(startNumber) { newSs ->
                    newSs.toIntOrNull()?.let {newSn->
                        val currentTt = _mTimeTrial.value
                        val currentSn = _mTimeTrial.value?.timeTrialHeader?.numberRules?.terminus
                        if (currentSn != null && currentTt != null) {
                            if (currentSn != newSn) {
                                val newNumRules = currentTt.timeTrialHeader.numberRules.copy(terminus = newSn)
                                updateTimeTrial(currentTt.copy(timeTrialHeader = currentTt.timeTrialHeader.copy(numberRules = newNumRules)))
                            }
                        }
                    }
                }

                numberRulesMediator.addSource(numberDirection){newDir->
                    val currentTt = _mTimeTrial.value
                    val currentDir = _mTimeTrial.value?.timeTrialHeader?.numberRules?.direction
                    if(newDir != null && currentDir!= null && currentTt != null){
                        if(currentDir != newDir){
                            val newNumRules = currentTt.timeTrialHeader.numberRules.copy(direction = newDir)
                            updateTimeTrial(currentTt.copy(timeTrialHeader = currentTt.timeTrialHeader.copy(numberRules = newNumRules)))
                        }
                    }
                }

                numberRulesMediator.addSource(exclusions){newExc->
                    val currentTt = _mTimeTrial.value
                    val currentExc = _mTimeTrial.value?.timeTrialHeader?.numberRules?.exclusions
                    if(newExc != null && currentExc!= null && currentTt != null){
                       val newList = NumberRules.stringToExclusions(newExc)
                        if(currentExc != newList){
                            val newNumRules = currentTt.timeTrialHeader.numberRules.copy(exclusions = newList)
                            updateTimeTrial(currentTt.copy(timeTrialHeader = currentTt.timeTrialHeader.copy(numberRules = newNumRules)))
                        }
                    }
                }
            }
        }

        override val numberDirection: MutableLiveData<NumbersDirection> = MutableLiveData()

        override val startNumber: MutableLiveData<String> = MutableLiveData()

        override val exclusions: MutableLiveData<String> = MutableLiveData()


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
        isCarolineAlive.set(false)
        viewModelScope.cancel()
    }
}