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
}

class SelectRidersViewModelImpl(private val ttSetup: SetupViewModel):ISelectRidersViewModel {



    private val selectedRidersMediator: MediatorLiveData<SelectedRidersInformation> = MediatorLiveData()

    override val selectedRidersInformation: LiveData<SelectedRidersInformation> = selectedRidersMediator



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
        selectedRidersMediator.addSource(ttSetup.riderRepository.allRidersLight){result->
            val tt = ttSetup.timeTrial.value
            if(result!=null && tt!=null){
                selectedRidersMediator.value = SelectedRidersInformation(result, tt)
            }
        }
        selectedRidersMediator.addSource(ttSetup.timeTrial){ tt->
            val riders = ttSetup.riderRepository.allRidersLight.value
            if(tt!=null && riders!=null){
                selectedRidersMediator.value = SelectedRidersInformation(riders, tt)
            }
        }
    }


}

data class SelectedRidersInformation(val allRiderList: List<Rider>, val timeTrial: TimeTrial)


