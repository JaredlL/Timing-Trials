package com.jaredlinden.timingtrials.select

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.setup.ISelectRidersViewModel
import com.jaredlinden.timingtrials.setup.SORT_ALPHABETICAL
import com.jaredlinden.timingtrials.setup.SORT_RECENT_ACTIVITY
import com.jaredlinden.timingtrials.setup.SelectedRidersInformation
import com.jaredlinden.timingtrials.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class  SelectRiderViewModel @Inject constructor (val riderRepository: IRiderRepository, val timeTrialRiderRepository: TimeTrialRiderRepository): ISelectRidersViewModel, ViewModel()
{
    override val selectedRidersInformation: MediatorLiveData<SelectedRidersInformation> = MediatorLiveData()

    private val lastYear = OffsetDateTime.now().minusYears(1).minusMonths(6)

    private val groupedStartTimeRiders = timeTrialRiderRepository.lastTimeTrialRiders()

    private val groupedAllRiders = riderRepository.allRiders.switchMap{ riderList->
            groupedStartTimeRiders.map{ lastTimeTrialList->

                val startTimeMap = lastTimeTrialList
                    .asSequence()
                    .filter { it.startTime.isAfter(lastYear) }
                    .groupBy { it.riderId }
                    .map { Pair(it.key, it.value.count()) }
                    .toMap()
                val ordered = riderList.sortedByDescending { startTimeMap[it.id]?:0 }
                ordered
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
            updateSelectedRiderInfo(groupedAllRiders.value, riderFilter.value, selectedRidersInformation.value, sm?:0)
        }
        selectedRidersInformation.addSource(riderFilter){filter->
            updateSelectedRiderInfo(groupedAllRiders.value, filter, selectedRidersInformation.value, liveSortMode.value?:0)
        }

        selectedRidersInformation.addSource(groupedAllRiders){allRiders->
            updateSelectedRiderInfo(allRiders, riderFilter.value, selectedRidersInformation.value, liveSortMode.value?:0)
        }
    }

    fun updateSelectedRiderInfo(allRiders: List<Rider>?, filterString: String?, selectedInfo: SelectedRidersInformation?, sortMode: Int){
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