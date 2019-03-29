package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.domain.TimeTrialSetup
import com.android.jared.linden.timingtrials.util.ConverterUtils
import java.util.*
import javax.inject.Inject

class SetupTimeTrialViewModel @Inject constructor(private val timeTrialSetup: TimeTrialSetup) : ViewModel() {


    val courseName: LiveData<String> = Transformations.map(timeTrialSetup.selectedCourse){
        it?.courseName
    }

    val timeTrial = timeTrialSetup.timeTrial

    val interval = MutableLiveData<String>()
    private val intervalMediator = MediatorLiveData<String>().apply {
        addSource(interval) {interval->
            value = interval
            interval.toIntOrNull()?.let{timeTrialSetup.timeTrial.value?.interval = it}
        }
        addSource(timeTrialSetup.timeTrial) {
            if (interval.value != it.interval.toString()) {
                interval.value = it.interval.toString()
            }
        }
    }.also { it.observeForever {  } }


    val laps = MutableLiveData<String>()
    private val lapsMediator = MediatorLiveData<String>().apply {
        addSource(laps) {laps->
            value = laps
            laps.toIntOrNull()?.let{timeTrialSetup.timeTrial.value?.laps = it}
        }
        addSource(timeTrialSetup.timeTrial) {
            if (laps.value != it.laps.toString()) {
                laps.value = it.laps.toString()
            }
        }
    }.also { it.observeForever {  } }

    val timeTrialName = MutableLiveData<String>()
    private val nameMediator = MediatorLiveData<String>().apply {
        addSource(timeTrialName) {
            value = it
            timeTrialSetup.timeTrial.value?.ttName = it
        }
        addSource(timeTrialSetup.timeTrial) {
           if (timeTrialName.value != it.ttName) {
               timeTrialName.value = it.ttName
           }
        }
    }.also { it.observeForever {  } }

    val availibleLaps = 1.rangeTo(99).map { i -> i.toString() }

    var selectedLapsPosition = 0
        set(value){
            field = value
            laps.value = availibleLaps[value]
        }


    val startTimeString: LiveData<String>  = Transformations.map(timeTrialSetup.timeTrial){
       ConverterUtils.dateToTimeDisplayString(it.startTime)
    }

    val startTime: LiveData<Date> = Transformations.map(timeTrialSetup.timeTrial){
        it.startTime
    }

    fun beginTt(){
        onBeginTt()
    }
    var onBeginTt = { Unit}


    fun setStartTime(startTime: Date){

        timeTrialSetup.timeTrial.value?.let {
            it.startTime = startTime
            timeTrialSetup.timeTrial.value = it
        }
    }

    }


