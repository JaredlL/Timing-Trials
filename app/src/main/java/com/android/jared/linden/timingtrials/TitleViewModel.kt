package com.android.jared.linden.timingtrials

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject



class TitleViewModel@Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel() {

    val nonFinishedTimeTrial = Transformations.map(timeTrialRepository.allTimeTrialsHeader){tt->
        tt.firstOrNull()
    }

    val timingTimeTrial = timeTrialRepository.getTimingTimeTrial()

    fun clearTimeTrial(timeTrial: TimeTrialHeader){
        viewModelScope.launch(Dispatchers.IO) {
            timeTrialRepository.deleteHeader(timeTrial)
        }
    }

    //val resumeOldViewModel: ResumeOldConfirmationViewModel = ResumeOldConfirmationViewModel(this)

    fun deleteTimeTrial(tt: TimeTrialHeader){
        viewModelScope.launch(Dispatchers.IO) {
            timeTrialRepository.deleteHeader(tt)
        }
    }

}