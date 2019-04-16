package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.jared.linden.timingtrials.data.TimeTrial

class TimeTrialState(val timingViewModel: TimingViewModel){

    val timeTrial = timingViewModel.timeTrial.value

    val elapsedTime: LiveData<String> = MutableLiveData<String>()

    fun updateState(){

    }
}
