package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.util.ConverterUtils
import org.threeten.bp.Instant


interface ITimeTrialPropertiesViewModel{

    val timeTrialHeader: LiveData<TimeTrialHeader>
    val timeTrialName: MutableLiveData<String>
    val startTimeString: LiveData<String>
    val courseName: LiveData<String>
    val startTime: MutableLiveData<Instant>
    val laps: MutableLiveData<String>
    val interval: MutableLiveData<String>
    val availableLaps: List<String>
    var selectedLapsPosition: Int
    var onBeginTt: () -> Unit
    fun beginTt()

}

class TimeTrialPropertiesViewModelImpl(private val ttSetup: SetupViewModel): ITimeTrialPropertiesViewModel{

   override val timeTrialHeader = Transformations.map(ttSetup.timeTrial){it.timeTrialHeader}

    //private val timeTrialValue = ttSetup.timeTrial.value?.timeTrialHeader

    override val courseName: LiveData<String> = Transformations.map(ttSetup.timeTrial){tt->
        tt?.let{
            tt.timeTrialHeader.course?.courseName
        }
    }


    /**
     * To perform a custom action on livedata set
     */
    override val timeTrialName: MutableLiveData<String> = MutableLiveData("")
    private val nameMediator = MediatorLiveData<String>().apply {
        addSource(ttSetup.timeTrial) { tt->
            tt?.timeTrialHeader?.let { if(timeTrialName.value != it.ttName) timeTrialName.value = it.ttName }

        }
        addSource(timeTrialName) { newName ->
            ttSetup.timeTrial.value?.timeTrialHeader?.let {
                if(it.ttName != newName) {
                    it.ttName = newName
                    ttSetup.updateDefinition(it)
                }
            }
        }
    }.also { it.observeForever {  } }


    override val laps = MutableLiveData<String>()
    private val lapsMediator = MediatorLiveData<String>().apply {
        addSource(ttSetup.timeTrial) { tt->
            tt?.let {
                if (laps.value != it.timeTrialHeader.laps.toString()) {
                    laps.value = it.timeTrialHeader.laps.toString()
                }
            }

        }
        addSource(laps) {laps->
            laps.toIntOrNull()?.let{newLaps ->
                timeTrialHeader.value?.let { tt->
                if(tt.laps != newLaps) {
                    tt.laps = newLaps
                    ttSetup.updateDefinition(tt)
                }
            }}
        }
    }.also { it.observeForever {  } }

    override val interval = MutableLiveData<String>()
    private val intervalMediator = MediatorLiveData<String>().apply {
        addSource(interval) {interval->
            timeTrialHeader.value?.let { tt ->
                interval.toIntOrNull()?.let{
                    if(tt.interval != it){
                        tt.interval = it
                        ttSetup.updateDefinition(tt)
                    }
                }
            }
        }
        addSource(ttSetup.timeTrial) { tt->
            tt?.let {
                if (interval.value != it.timeTrialHeader.interval.toString()) {
                    interval.value = it.timeTrialHeader.interval.toString()
                }
            }

        }
    }.also { it.observeForever {  } }

    override val startTime = MutableLiveData<Instant>()
    private val startTimeMediator = MediatorLiveData<Instant>().apply {
        addSource(timeTrialHeader) { tt->
            tt?.let {
                if (startTime.value != it.startTime) {
                    startTime.value = it.startTime
                }
            }

        }
        addSource(startTime) {newStartTime->
            timeTrialHeader.value?.let { tt->
                if(tt.startTime != newStartTime) {
                    tt.startTime = newStartTime
                    ttSetup.updateDefinition(tt)
                }
            }
        }
    }.also { it.observeForever {  } }

    override val startTimeString: LiveData<String>  = Transformations.map(timeTrialHeader){ tt->
        tt?.let { ConverterUtils.instantToSecondsDisplayString(it.startTime)}
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



