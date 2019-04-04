package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
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

class TimeTrialSetupViewModel @Inject constructor(
         val timeTrialRepository: ITimeTrialRepository,
         val riderRepository: IRiderRepository,
         val courseRepository: ICourseRepository
) : ViewModel(), ITimeTrialSetupViewModel{



    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()
    private fun selectedRiders() = timeTrial.value?.riders

    override val orderRidersViewModel: IOrderRidersViewModel = object: IOrderRidersViewModel {
        override fun moveItem(fromPosition: Int, toPosition: Int) {
            val mutList = selectedRiders()?.toMutableList() ?: arrayListOf()

            if(fromPosition > toPosition){
                mutList.add(toPosition, mutList[fromPosition])
                mutList.removeAt(fromPosition + 1)
            }else{
                mutList.add(toPosition + 1, mutList[fromPosition])
                mutList.removeAt(fromPosition)
            }

            timeTrial.value?.let {
                it.riders = mutList
                timeTrial.value = it
            }

        }
        override fun getOrderableRiders(): LiveData<List<Rider>> = Transformations.map(timeTrial){it.riders}
    }

    override val selectCourseViewModel: ISelectCourseViewModel = SelectCourseViewModelImpl(this)
    override val selectRidersViewModel: ISelectRidersViewModel = SelectRidersViewModelImpl(this)
    override val timeTrialPropertiesViewModel: ITimeTrialPropertiesViewModel = TimeTrialPropertiesViewModelImpl(this)
    override val setupConformationViewModel: ISetupConformationViewModel = SetupConfirmationViewModel(this)

    fun insertTt(){
        viewModelScope.launch(Dispatchers.IO) {
            timeTrial.value?.let { timeTrialRepository.insert(it) }

        }
    }

    init {

        timeTrial.addSource(timeTrialRepository.getSetupTimeTrial()){tt->
            timeTrial.value = tt
        }

        /**
         * Need to remember which ids were selected when a rider is added/removed
         * Also need to update selected riders if they are modfied in the DB
         */
        timeTrial.addSource(riderRepository.allRiders) { result: List<Rider>? ->
            result?.let {

                val currentSelected = selectedRiders()
                if( currentSelected != null && currentSelected.count() > 0){

                    val oldSelected: LinkedHashMap<Long, Rider> =  LinkedHashMap(currentSelected.associateBy { r -> r.id ?: 0 })
                    val retainedIds: MutableSet<Long> = mutableSetOf()
                    result.forEach{rider ->
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
                    timeTrial.value?.let {
                        it.riders = newList
                        timeTrial.value = it

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