package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.jared.linden.timingtrials.domain.TimeTrialSetup
import com.android.jared.linden.timingtrials.util.ConverterUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*

class SetupConfirmationViewModel(private val timeTrialSetup: TimeTrialSetup) : ViewModel(){

    val title = Transformations.map(timeTrialSetup.timeTrial){tt ->
        "Starting ${tt.ttName}"
    }

    val lapsCourse = Transformations.map(timeTrialSetup.timeTrial){tt->
        "${tt.laps} laps of ${tt.course?.courseName}"
    }

    val ridersInterval = Transformations.map(timeTrialSetup.timeTrial){tt->
        if(tt.interval == 0){
            "${tt.riders.count()} riders starting at 0 second intervals, mass start!"
        }else{
            "${tt.riders.count()} riders starting at ${tt.interval} second intervals"
        }

    }

    val startTime = Transformations.map(timeTrialSetup.timeTrial){tt->
        "First rider starting at ${ConverterUtils.dateToTimeDisplayString(tt.startTime)}"

    }

    var onStartTT: (Boolean) -> Unit ={}

    fun startTt(){

        timeTrialSetup.timeTrial.value?.let {
           if(it.startTime.after(Calendar.getInstance().time)){

               it.isSetup = true
               viewModelScope.launch(Dispatchers.IO) {
                   timeTrialSetup.insert(it)
               }
               onStartTT(true)
           }else{
               onStartTT(false)
           }



        }


    }


    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }




}