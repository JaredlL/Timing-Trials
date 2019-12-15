package com.android.jared.linden.timingtrials

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.data.source.TimingTrialsDatabase
import com.android.jared.linden.timingtrials.setup.ResumeOldConfirmationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject



class TitleViewModel@Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel() {

    val nonFinishedTimeTrial = Transformations.map(timeTrialRepository.nonFinishedFullTimeTrial){tt->
        tt
    }

    fun clearTimeTrial(timeTrial: TimeTrial){
        viewModelScope.launch(Dispatchers.IO) {
            val cleared = TimeTrial.createBlank().copy(timeTrialHeader = TimeTrialHeader.createBlank().copy(id = timeTrial.timeTrialHeader.id))
            timeTrialRepository.updateFull(cleared)
        }
    }

    val resumeOldViewModel: ResumeOldConfirmationViewModel = ResumeOldConfirmationViewModel(this)

    fun deleteTimeTrial(tt: TimeTrial){
        viewModelScope.launch(Dispatchers.IO) {
            timeTrialRepository.delete(tt)
        }
    }

}