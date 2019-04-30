package com.android.jared.linden.timingtrials

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialDefinition
import com.android.jared.linden.timingtrials.setup.ResumeOldConfirmationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject



class MainViewModel@Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel() {

    var timeTrial = timeTrialRepository.getSetupTimeTrial()

    val resumeOldViewModel: ResumeOldConfirmationViewModel = ResumeOldConfirmationViewModel(this)

    fun deleteTimeTrial(tt: TimeTrial){
        viewModelScope.launch(Dispatchers.IO) {
            timeTrialRepository.delete(tt)
        }
    }

}