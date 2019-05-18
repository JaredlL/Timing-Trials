package com.android.jared.linden.timingtrials

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.setup.ResumeOldConfirmationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject



class MainViewModel@Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel() {

    val setupTimeTrial = timeTrialRepository.getSetupTimeTrial()

    val timingTimeTrial = timeTrialRepository.getTimingTimeTrial()

    val resumeOldViewModel: ResumeOldConfirmationViewModel = ResumeOldConfirmationViewModel(this)

    fun deleteTimeTrial(tt: TimeTrial){
        viewModelScope.launch(Dispatchers.IO) {
            timeTrialRepository.delete(tt)
        }
    }

}