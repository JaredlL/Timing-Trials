package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.util.ConverterUtils
import java.util.*


interface ITimeTrialPropertiesViewModel{

    val timeTrial: LiveData<TimeTrial>
    val timeTrialName: MutableLiveData<String>
    val startTimeString: LiveData<String>
    val courseName: LiveData<String>
    val startTime: MutableLiveData<Date>
    val laps: MutableLiveData<String>
    val interval: MutableLiveData<String>
    val availableLaps: List<String>
    var selectedLapsPosition: Int
    var onBeginTt: () -> Unit
    fun beginTt()

}

class TimeTrialPropertiesViewModelImpl(private val ttSetup: TimeTrialSetupViewModel): ITimeTrialPropertiesViewModel{

   override val timeTrial = ttSetup.timeTrial
    private fun timeTrialValue() = timeTrial.value

    override val courseName: LiveData<String> = Transformations.map(ttSetup.timeTrial){
        it.course?.courseName
    }


    /**
     * Best way i have found to perform a custom action on livedata set
     */
    override val timeTrialName: MutableLiveData<String> = MutableLiveData()
    private val nameMediator = MediatorLiveData<String>().apply {
        addSource(ttSetup.timeTrial) {
           if(timeTrialName.value != it.ttName) timeTrialName.value = it.ttName
        }
        addSource(timeTrialName) { newName ->
            timeTrialValue()?.let {
                if(it.ttName != newName) {
                    it.ttName = newName
                    ttSetup.timeTrial.value = it
                }
            }
        }
    }.also { it.observeForever {  } }


    override val laps = MutableLiveData<String>()
    private val lapsMediator = MediatorLiveData<String>().apply {
        addSource(ttSetup.timeTrial) {
            if (laps.value != it.laps.toString()) {
                laps.value = it.laps.toString()
            }
        }
        addSource(laps) {laps->
            laps.toIntOrNull()?.let{newLaps ->
                timeTrialValue()?.let {tt->
                if(tt.laps != newLaps) {
                    tt.laps = newLaps
                    ttSetup.timeTrial.value = tt
                }
            }}
        }
    }.also { it.observeForever {  } }

    override val interval = MutableLiveData<String>()
    private val intervalMediator = MediatorLiveData<String>().apply {
        addSource(interval) {interval->
            timeTrialValue()?.let { tt ->
                interval.toIntOrNull()?.let{
                    if(tt.interval != it){
                        tt.interval = it
                        ttSetup.timeTrial.value = tt
                    }
                }
            }
        }
        addSource(ttSetup.timeTrial) {
            if (interval.value != it.interval.toString()) {
                interval.value = it.interval.toString()
            }
        }
    }.also { it.observeForever {  } }

    override val startTime = MutableLiveData<Date>()
    private val startTimeMediator = MediatorLiveData<Date>().apply {
        addSource(ttSetup.timeTrial) {
            if (startTime.value != it.startTime) {
            startTime.value = it.startTime
            }
        }
        addSource(startTime) {newStartTime->
            timeTrialValue()?.let {tt->
                if(tt.startTime != newStartTime) {
                    tt.startTime = newStartTime
                    ttSetup.timeTrial.value = tt
                }
            }
        }
    }.also { it.observeForever {  } }

    override val startTimeString: LiveData<String>  = Transformations.map(ttSetup.timeTrial){
        ConverterUtils.dateToTimeDisplayString(it.startTime)
    }

    override val availableLaps = 1.rangeTo(99).map { i -> i.toString() }

    override var selectedLapsPosition = 0
        set(value){
            field = value
            laps.value = availableLaps[value]
        }

    override fun beginTt(){
        onBeginTt()
    }
    override var onBeginTt = { Unit}

}



