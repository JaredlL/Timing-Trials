package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.util.ConverterUtils
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class TimingViewModel  @Inject constructor(val timeTrialEventRepository: ITimeTrialEventRepository) : ViewModel() {

    val timeTrialWithEvents: MediatorLiveData<TimeTrialWithEvents> = MediatorLiveData()
    val timeTrial: LiveData<TimeTrial> = Transformations.map(timeTrialWithEvents){
        it.timeTrial
    }
    val events: LiveData<List<TimeTrialEvent>> =  Transformations.map(timeTrialWithEvents) {
        it.eventList
    }
    val timeString: MutableLiveData<String> = MutableLiveData()
    val statusString: MutableLiveData<String> = MutableLiveData()
    val allTtWithEvent = timeTrialEventRepository.getAllTimeTrialEvents()


    private var riderStartTimes: SortedMap<Instant, Rider> = sortedMapOf()
    private var numberOfRiders = 0

    private fun getFinishedRiders(): List<Rider> {
        return timeTrialWithEvents.value?.let {tt->
            tt.eventList.filter { it.eventType == EventType.RIDER_FINISHED }.mapNotNull {event-> tt.timeTrial.riders.firstOrNull { rider -> rider.id == event.riderId }  }
        }?: listOf()
    }

    private fun getDepartedRiders(): List<Rider> {
        return timeTrialWithEvents.value?.let {tt->
            tt.eventList.filter { it.eventType == EventType.RIDER_STARTED}.mapNotNull { event-> tt.timeTrial.riders.firstOrNull { rider -> rider.id == event.riderId }  }
        }?: listOf()
    }


    private var ttIntervalSeconds: Int = 0
    private var timer: Timer = Timer()
    private val TIMER_PERIOD_MS = 50L

    fun initialise(timeTrialId: Long) {
        if (timeTrial.value == null && timeTrialId != 0L) {
            timeTrialWithEvents.addSource(timeTrialEventRepository.getTimeTrialWithEvents(timeTrialId)) { result ->
                result?.let {tt->
                    timeTrialWithEvents.value = tt

                    ttIntervalSeconds = tt.timeTrial.interval.seconds.toInt()
                    numberOfRiders = tt.timeTrial.riders.count()

                    riderStartTimes = tt.timeTrial.riders.withIndex()
                            .associate { r -> Pair(
                                    tt.timeTrial.startTime.plusSeconds(tt.timeTrial.interval.seconds * r.index),
                                    r.value) }.toSortedMap()

                    timer = Timer()
                    val task = object : TimerTask(){
                        override fun run() {
                            updateLoop()
                        }
                    }
                    timer.scheduleAtFixedRate(task, 0L, TIMER_PERIOD_MS)
                }
            }
        }
    }


    fun updateLoop(){
        timeTrialWithEvents.value?.let {tt->
            val now = Instant.now()
            val timeDur: Duration = Duration.between(now, tt.timeTrial.startTime)

            updateEvents(now)

            val currentDeparted = getDepartedRiders()
            val currentFinished = getFinishedRiders()



            statusString.postValue(getStatusString(now, timeDur, currentDeparted, currentFinished))
            timeString.postValue(ConverterUtils.toTenthsDisplayString(timeDur))

        }
    }

    private fun updateEvents(currentTime: Instant){
        val startedRiders = riderStartTimes.headMap(currentTime)
        val departed = getDepartedRiders()
        val newStartingRiders = startedRiders.values.subtract(departed).toList()
        if(newStartingRiders.isNotEmpty()){

            timeTrialWithEvents.value?.let { tte->
                val newEvents = tte.eventList.toMutableList()
                newEvents.addAll(newStartingRiders.map { TimeTrialEvent(timeTrial.value?.id?:0, it.id, currentTime, EventType.RIDER_STARTED) })
                tte.eventList = newEvents
                timeTrialWithEvents.postValue(tte)
            }
        }
    }

    private fun performDepartEvent(){

    }

    private fun getStatusString(currentTime: Instant, duration: Duration, departed: List<Rider>, finished: List<Rider>): String{

        timeTrialWithEvents.value?.let { tte ->
            val ridersToStart = riderStartTimes.tailMap(currentTime)

            //If we are more than 1 min before TT start time
            if(currentTime.plusSeconds(60).isBefore(tte.timeTrial.startTime)){
                return "First rider off in ${duration.toMinutes()} minutes"
            }

            if(ridersToStart.isNotEmpty()){

                val nextStartInstant = ridersToStart.firstKey()
                val nextStartRider = ridersToStart[nextStartInstant]
                val number = riderStartTimes.map { r -> r.key }.indexOf(nextStartInstant) + 1

                val durationToNextRider = Duration.ofSeconds(nextStartInstant.epochSecond - currentTime.epochSecond)

                nextStartRider?.let { rider->
                    val riderstring = "($number) ${rider.firstName} ${rider.lastName}"
                    return when(durationToNextRider.seconds){
                      0L, in ttIntervalSeconds - 3..ttIntervalSeconds ->
                        {
                            val lastRiderToStart = tte.timeTrial.riders.firstOrNull { it.id == departed.lastOrNull()?.id }
                            if(lastRiderToStart != null){
                                 "(${lastRiderToStart.firstName} ${lastRiderToStart.lastName}) GO GO GO!!!"
                            }else{
                                "(${rider.firstName} ${rider.lastName}) GO GO GO!!!"
                            }

                        }
                        in 1..10 ->
                            "${rider.firstName} ${rider.lastName} - ${durationToNextRider.seconds}!"
                        in 5..ttIntervalSeconds/4 ->
                            "$riderstring starts in ${ttIntervalSeconds/4} seconds!"
                        in ttIntervalSeconds/4.. ttIntervalSeconds/2 ->
                            "$riderstring starts in ${ttIntervalSeconds/2} seconds!"
                        else ->
                            "Next rider is $riderstring"
                    }
                }
                return "NULL"
            }else{
                return "${finished.count()} riders have finished, ${numberOfRiders - finished.count()} riders on course"
            }
        }
        return "No TT Loaded"
    }
}