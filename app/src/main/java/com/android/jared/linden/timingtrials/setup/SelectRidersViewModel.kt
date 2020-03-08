package com.android.jared.linden.timingtrials.setup


import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.Year
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalField


interface ISelectRidersViewModel{
    val selectedRidersInformation: LiveData<SelectedRidersInformation>
    fun addRiderToTt(newSelectedRider: Rider)
    fun removeRiderFromTt(riderToRemove: Rider)
    fun setRiderFilter(filterString: String)
    fun setSortMode(sortMode: Int)
    val riderFilter:MutableLiveData<String>
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


    val liveSortMode : MutableLiveData<Int> = MutableLiveData(0)

    override fun setSortMode(sortMode: Int) {
        liveSortMode.value = sortMode
    }

    override val riderFilter = MutableLiveData<String>()

    override fun setRiderFilter(filterString: String){
        riderFilter.value = filterString
    }

    override fun addRiderToTt(newSelectedRider: Rider) {
        ttSetup.timeTrial.value?.let { tt->
            ttSetup.updateTimeTrial(tt.addRider(newSelectedRider))
        }
    }

    override fun removeRiderFromTt(riderToRemove: Rider) {
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
               if(sortMode == 1){
                   allRiders.sortedBy { it.fullName() }
               }else{
                   allRiders
               }

           }else{
               if(sortMode == 1){
                   allRiders.asSequence().filter { it.fullName().contains(filterString, ignoreCase = true) }.sortedBy { it.firstName }.toList()
               }else{
                   allRiders.filter { it.fullName().contains(filterString, ignoreCase = true) }
               }

           }
            selectedRidersMediator.value = SelectedRidersInformation(filteredRiders, timeTrial)
        }
    }


}

data class SelectedRidersInformation(val allRiderList: List<Rider>, val timeTrial: TimeTrial)


