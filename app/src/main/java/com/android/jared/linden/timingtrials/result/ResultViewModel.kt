package com.android.jared.linden.timingtrials.result

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import javax.inject.Inject

class ResultViewModel @Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel() {
    // TODO: Implement the ViewModel

   val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()

    fun initialise(timeTrialId: Long){
        timeTrial.addSource(timeTrialRepository.getTimeTrialById(timeTrialId)){
            timeTrial.value = it
        }
    }

}
