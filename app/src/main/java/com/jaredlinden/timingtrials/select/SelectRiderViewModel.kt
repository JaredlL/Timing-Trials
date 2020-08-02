package com.jaredlinden.timingtrials.select

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.setup.ISelectRidersViewModel
import com.jaredlinden.timingtrials.setup.SORT_ALPHABETICAL
import com.jaredlinden.timingtrials.setup.SORT_RECENT_ACTIVITY
import com.jaredlinden.timingtrials.setup.SelectedRidersInformation
import com.jaredlinden.timingtrials.util.Event
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

class  SelectRiderViewModel @Inject constructor (val riderRepository: IRiderRepository, val timeTrialRiderRepository: TimeTrialRiderRepository): ISelectRidersViewModel, ViewModel()
{

    //rivate val selectedRidersMediator: MediatorLiveData<SelectedRidersInformation> = MediatorLiveData()

    override val selectedRidersInformation: MediatorLiveData<SelectedRidersInformation> = MediatorLiveData()

    private val lastYear = OffsetDateTime.now().minusYears(1).minusMonths(6)

    private val groupedStartTimeRiders = timeTrialRiderRepository.lastTimeTrialRiders()

    private val groupedAllRiders = Transformations.switchMap(riderRepository.allRiders){ riderList->
        riderList?.let { rList->
            Transformations.map(groupedStartTimeRiders){ lastTimeTrialList->
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
        selectedRidersInformation.value = SelectedRidersInformation(riderRepository.allRiders.value?: listOf(), listOf(newSelectedRider.id?:0L))
    }

    override fun riderUnselected(riderToRemove: Rider) {
        selectedRidersInformation.value = SelectedRidersInformation(riderRepository.allRiders.value?: listOf(), listOf())
    }

    init {
        selectedRidersInformation.value = SelectedRidersInformation(listOf(), listOf())
        selectedRidersInformation.addSource(liveSortMode){sm->
            updateselectedRiderInfo(groupedAllRiders.value, riderFilter.value, selectedRidersInformation.value, sm?:0)
        }
        selectedRidersInformation.addSource(riderFilter){filter->
            updateselectedRiderInfo(groupedAllRiders.value, filter, selectedRidersInformation.value, liveSortMode.value?:0)
        }

        selectedRidersInformation.addSource(groupedAllRiders){allRiders->
            updateselectedRiderInfo(allRiders, riderFilter.value, selectedRidersInformation.value, liveSortMode.value?:0)
        }
    }

    fun updateselectedRiderInfo(allRiders: List<Rider>?, filterString: String?, selectedInfo: SelectedRidersInformation?, sortMode: Int){
        if(allRiders != null && selectedInfo != null){
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
            selectedRidersInformation.value = SelectedRidersInformation(filteredRiders, selectedInfo.selectedIds)
        }
    }


}