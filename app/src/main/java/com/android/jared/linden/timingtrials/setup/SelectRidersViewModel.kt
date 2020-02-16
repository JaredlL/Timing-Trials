package com.android.jared.linden.timingtrials.setup


import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider


interface ISelectRidersViewModel{
    val selectedRidersInformation: LiveData<SelectedRidersInformation>
    fun addRiderToTt(newSelectedRider: Rider)
    fun removeRiderFromTt(riderToRemove: Rider)
    fun setRiderFilter(filter: String)
    val riderFilter:MutableLiveData<String>
}

class SelectRidersViewModelImpl(private val ttSetup: SetupViewModel):ISelectRidersViewModel {



    private val selectedRidersMediator: MediatorLiveData<SelectedRidersInformation> = MediatorLiveData()

    override val selectedRidersInformation: LiveData<SelectedRidersInformation> = selectedRidersMediator


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

        selectedRidersMediator.addSource(riderFilter){filter->
            updateselectedRiderInfo(ttSetup.riderRepository.allRidersLight.value, filter, ttSetup.timeTrial.value)
        }

        selectedRidersMediator.addSource(ttSetup.riderRepository.allRidersLight){allRiders->
            updateselectedRiderInfo(allRiders, riderFilter.value, ttSetup.timeTrial.value)
        }
        selectedRidersMediator.addSource(ttSetup.timeTrial){ tt->
            updateselectedRiderInfo(ttSetup.riderRepository.allRidersLight.value, riderFilter.value, tt)
        }
    }

    fun updateselectedRiderInfo(allRiders: List<Rider>?, filterString: String?, timeTrial: TimeTrial?){
        if(allRiders != null && timeTrial != null){
           val filteredRiders = if(filterString.isNullOrBlank()){
               allRiders
           }else{
               allRiders.filter { it.fullName().contains(filterString, ignoreCase = true) }
           }
            selectedRidersMediator.value = SelectedRidersInformation(filteredRiders, timeTrial)
        }
    }


}

data class SelectedRidersInformation(val allRiderList: List<Rider>, val timeTrial: TimeTrial)


