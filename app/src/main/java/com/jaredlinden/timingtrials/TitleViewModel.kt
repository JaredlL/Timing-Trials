package com.jaredlinden.timingtrials

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
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