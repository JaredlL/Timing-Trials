package com.jaredlinden.timingtrials.setup


import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.util.Event
import org.threeten.bp.OffsetDateTime


interface ISelectRidersViewModel{
    val selectedRidersInformation: LiveData<SelectedRidersInformation>
    fun riderSelected(newSelectedRider: Rider)
    fun riderUnselected(riderToRemove: Rider)
    fun setRiderFilter(filterString: String)
    fun setSortMode(sortMode: Int)
    val riderFilter:MutableLiveData<String>
    val showMessage: MutableLiveData<Event<String>>
    val close: MutableLiveData<Event<Boolean>>
}

class SelectRidersViewModelImpl(private val ttSetup: SetupViewModel):ISelectRidersViewModel {



    private val selectedRidersMediator: MediatorLiveData<SelectedRidersInformation> = MediatorLiveData()

    override val selectedRidersInformation: LiveData<SelectedRidersInformation> = selectedRidersMediator

    private val lastYear = OffsetDateTime.now().minusYears(1).minusMonths(6)

    private val groupedStartTimeRiders = ttSetup.timeTrialRiderRepository.lastTimeTrialRiders()

    private val groupedAllRiders = Transformations.switchMap(ttSetup.riderRepository.allRiders){riderList->
        riderList?.let { rList->
            Transformations.map(groupedStartTimeRiders){lastTimeTrialList->
                lastTimeTrialList?.let {
                    val startTimeMap = it.asSequence().filter { it.startTime.isAfter(lastYear) }.groupBy { it.riderId }.map { Pair(it.key, it.value.count()) }.toMap()
                    val ordered = rList.sortedByDescending { startTimeMap[it.id]?:0 }
                    ordered
                }?:riderList
            }
        }
    }


    val liveSortMode : MutableLiveData<Int> = MutableLiveData(SORT_RECENT_ACTIVITY)

    override fun setSortMode(sortMode: Int) {
        liveSortMode.value = sortMode
    }

    override val riderFilter = MutableLiveData<String>()

    override val showMessage: MutableLiveData<Event<String>> = MutableLiveData()

    override val close: MutableLiveData<Event<Boolean>> = MutableLiveData()

    override fun setRiderFilter(filterString: String){
        riderFilter.value = filterString
    }

    override fun riderSelected(newSelectedRider: Rider) {
        ttSetup.timeTrial.value?.let { tt->
            ttSetup.updateTimeTrial(tt.addRider(newSelectedRider))
        }
    }

    override fun riderUnselected(riderToRemove: Rider) {
        ttSetup.timeTrial.value?.let { tt->
            ttSetup.updateTimeTrial(tt.removeRider(riderToRemove))
        }
    }

    init {
        selectedRidersMediator.addSource(liveSortMode){sm->
            updateselectedRiderInfo(groupedAllRiders.value, riderFilter.value, ttSetup.timeTrial.value, sm?:0)
        }
        selectedRidersMediator.addSource(riderFilter){filter->
            updateselectedRiderInfo(groupedAllRiders.value, filter, ttSetup.timeTrial.value, liveSortMode.value?:0)
        }

        selectedRidersMediator.addSource(groupedAllRiders){allRiders->
            updateselectedRiderInfo(allRiders, riderFilter.value, ttSetup.timeTrial.value, liveSortMode.value?:0)
        }
        selectedRidersMediator.addSource(ttSetup.timeTrial){ tt->
            updateselectedRiderInfo(groupedAllRiders.value, riderFilter.value, tt, liveSortMode.value?:0)
        }
    }

    fun updateselectedRiderInfo(allRiders: List<Rider>?, filterString: String?, timeTrial: TimeTrial?, sortMode: Int){
        if(allRiders != null && timeTrial != null){
           val filteredRiders = if(filterString.isNullOrBlank()){
               if(sortMode == SORT_ALPHABETICAL){
                   allRiders.sortedBy { it.fullName() }
               }else{
                   allRiders
               }

           }else{
               if(sortMode == SORT_ALPHABETICAL){
                   allRiders.asSequence().filter { it.fullName().contains(filterString, ignoreCase = true) }.sortedBy { it.fullName() }.toList()
               }else{
                   allRiders.filter { it.fullName().contains(filterString, ignoreCase = true) }
               }

           }
            selectedRidersMediator.value = SelectedRidersInformation(filteredRiders, timeTrial.riderList.mapNotNull { it.riderData.id })
        }
    }


}

data class SelectedRidersInformation(val allRiderList: List<Rider>, val selectedIds: List<Long>)


