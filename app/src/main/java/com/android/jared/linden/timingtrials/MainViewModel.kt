package com.android.jared.linden.timingtrials

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.setup.ResumeOldConfirmationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject



class MainViewModel@Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel() {

    val setupTimeTrial = Transformations.map(timeTrialRepository.getNonFinishedTimeTrial()){tt->
        tt?.firstOrNull{it.timeTrialHeader.status == TimeTrialStatus.SETTING_UP}
    }

    val timingTimeTrial = Transformations.map(timeTrialRepository.getNonFinishedTimeTrial()){tt->
        tt?.firstOrNull{it.timeTrialHeader.status == TimeTrialStatus.IN_PROGRESS}
    }

    val resumeOldViewModel: ResumeOldConfirmationViewModel = ResumeOldConfirmationViewModel(this)

    fun deleteTimeTrial(tt: TimeTrial){
        viewModelScope.launch(Dispatchers.IO) {
            timeTrialRepository.delete(tt)
        }
    }

}