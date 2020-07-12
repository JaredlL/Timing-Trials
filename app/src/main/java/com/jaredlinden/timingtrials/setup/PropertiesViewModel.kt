package com.jaredlinden.timingtrials.setup

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.util.ConverterUtils
import org.threeten.bp.OffsetDateTime


interface ITimeTrialPropertiesViewModel{

    val timeTrial: LiveData<TimeTrial?>
    val timeTrialHeader: LiveData<TimeTrialHeader?>
    val timeTrialName: MutableLiveData<String>
    val startTimeString: LiveData<String>
    val courseName: LiveData<String>
    val startTime: MutableLiveData<OffsetDateTime>
    val laps: MutableLiveData<String>
    val interval: MutableLiveData<String>
    val firstRiderOffset: MutableLiveData<String>
    val offsetDescription: LiveData<String>
    val availableLaps: List<String>
    val setupMediator: MediatorLiveData<Any>
    var selectedLapsPosition: Int


}

class TimeTrialPropertiesViewModelImpl(private val ttSetup: SetupViewModel): ITimeTrialPropertiesViewModel{

    override val timeTrial = ttSetup.timeTrial
   override val timeTrialHeader = Transformations.map(ttSetup.timeTrial){it?.timeTrialHeader}


    override val courseName: LiveData<String> = Transformations.map(ttSetup.timeTrial){ tt->
        tt?.let{

            tt.course?.courseName
        }
    }
    override val startTime = MutableLiveData<OffsetDateTime>()
    override val firstRiderOffset = MutableLiveData<String>()
    override val laps = MutableLiveData<String>()
    override val interval = MutableLiveData<String>()

    override val offsetDescription: LiveData<String> = Transformations.map(timeTrialHeader){tt->
        tt?.startTime?.let {
            val tString = ConverterUtils.instantToSecondsDisplayString(it.toInstant().plusSeconds(tt.firstRiderStartOffset.toLong()))
            "(ie first rider starts at $tString)"
        }
    }

    override val startTimeString: LiveData<String>  = Transformations.map(timeTrialHeader){ tt->
        tt?.startTime?.let { ConverterUtils.instantToSecondsDisplayString(it.toInstant())}
    }

    override val availableLaps = 1.rangeTo(99).map { i -> i.toString() }

    override var selectedLapsPosition = 0
        set(value){
            field = value
            laps.value = availableLaps[value]
        }

    /**
     * To perform a custom action on livedata set
     */
    override val timeTrialName: MutableLiveData<String> = MutableLiveData("")
    override val setupMediator = MediatorLiveData<Any>().apply {
        addSource(timeTrialHeader) { tt->
            tt?.let {
                val curentName = timeTrialName.value
                val ttName = it.ttName
                if(curentName!= ttName){
                    System.out.println("JAREDMSG -> Old Name = ${timeTrialName.value}, New Name = ${it.ttName}")
                    timeTrialName.value = it.ttName
                }
                if (firstRiderOffset.value != it.firstRiderStartOffset.toString()) {
                    firstRiderOffset.value = it.firstRiderStartOffset.toString()
                }
                if (startTime.value != it.startTime) {
                    startTime.value = it.startTime
                }
                if (interval.value != it.interval.toString()) {
                    interval.value = it.interval.toString()
                }
                if (laps.value != it.laps.toString()) {
                    laps.value = it.laps.toString()
                }
            }

        }
        addSource(timeTrialName) { newName ->
            newName?.trim()?.let{trimmed->
                timeTrialHeader.value?.let {
                    if(it.ttName != trimmed) { updateDefinition( it.copy(ttName = trimmed))
                    }
                }
            }

        }
        addSource(firstRiderOffset) {os->
            os.toIntOrNull()?.let{newos ->
                timeTrialHeader.value?.let { tt->
                    if(tt.firstRiderStartOffset != newos) {
                        updateDefinition(tt.copy(firstRiderStartOffset = newos))
                    }
                }}
        }
        addSource(startTime) {newStartTime->
            timeTrialHeader.value?.let { tt->
                if(tt.startTime != newStartTime) {
                    updateDefinition(tt.copy(startTime = newStartTime))
                }
            }
        }
        addSource(interval) {interval->
            timeTrialHeader.value?.let { tt ->
                interval.toIntOrNull()?.let{
                    if(tt.interval != it){
                        updateDefinition(tt.copy(interval = it))
                    }
                }
            }
        }

        addSource(laps) {laps->
            laps.toIntOrNull()?.let{newLaps ->
                timeTrialHeader.value?.let { tt->
                    if(tt.laps != newLaps) {
                        updateDefinition(tt.copy(laps = newLaps))
                    }
                }}
        }

    }
    fun updateDefinition(newTimeTrialHeader: TimeTrialHeader){
        ttSetup.timeTrial.value?.let {current->
            val new = current.updateHeader(newTimeTrialHeader)
            ttSetup.updateTimeTrial(new)
        }
    }



}



