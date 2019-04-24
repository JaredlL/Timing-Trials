package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.util.ConverterUtils
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.*
import javax.inject.Inject

class TimingViewModel  @Inject constructor(val timeTrialEventRepository: ITimeTrialEventRepository) : ViewModel() {

    val timeTrialWithEvents: MediatorLiveData<TimeTrialWithEvents> = MediatorLiveData()
    val timeTrial: MutableLiveData<TimeTrial> = MutableLiveData()
    val events: MutableLiveData<List<TimeTrialEvent>>  = MutableLiveData(listOf())
    val timeString: MutableLiveData<String> = MutableLiveData()
    val statusString: MutableLiveData<String> = MutableLiveData()

    val allTtWithEvent = timeTrialEventRepository.getAllTimeTrialEvents()

    private var riderStartTimes: SortedMap<Instant, Rider> = sortedMapOf()
    private var numberOfRiders = 0

    private fun getFinishedRiders(): List<Rider> {
        return timeTrialWithEvents.value?.let {tt->
            tt.eventList.filter { it.eventType == EventType.RIDER_FINISHED }.mapNotNull { tt.timeTrial.riders.find { r -> r.id == it.id }  }
        }?: listOf()
    }

    private fun getDepartedRiders(): List<Rider> {
        return timeTrialWithEvents.value?.let {tt->
            tt.eventList.filter { it.eventType == EventType.RIDER_STARTED}.mapNotNull { tt.timeTrial.riders.find { r -> r.id == it.id }  }
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
                    timeTrial.value =  tt.timeTrial
                    events.value = tt.eventList

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
            val timeSinceTtStart = now.minusMillis(tt.timeTrial.startTime.toEpochMilli())

            val timeDur: Duration = Duration.between(now, tt.timeTrial.startTime)

            val currentDeparted = getDepartedRiders()
            val currentFinished = getFinishedRiders()

            updateEvents(timeSinceTtStart, currentDeparted)
            statusString.postValue(getStatusString(timeSinceTtStart, currentDeparted, currentFinished))
            timeString.postValue(ConverterUtils.toTenthsDisplayString(timeDur))

        }
    }

    private fun updateEvents(currentTtTime: Instant, departedRiders: List<Rider>){
        val newStartedRiders = riderStartTimes.headMap(currentTtTime)
        val ridersToStart = newStartedRiders.values.subtract(departedRiders).toList()
        if(ridersToStart.isNotEmpty()){
            events.value?.let { tte->
                val newEvents = tte.toMutableList()
                newEvents.addAll(ridersToStart.map { TimeTrialEvent(timeTrial.value?.id?:0, it.id, currentTtTime, EventType.RIDER_STARTED) })
                events.postValue(newEvents)
            }
        }
    }

    private fun performDepartEvent(){

    }

    private fun getStatusString(currentTtTime: Instant, departed: List<Rider>, finished: List<Rider>): String{
        val ridersToStart = riderStartTimes.tailMap(currentTtTime)

        if(ridersToStart.isNotEmpty()){

            val nextStartInstant = ridersToStart.firstKey()
            val nextStartRider = ridersToStart[nextStartInstant]
            val number = ridersToStart.map { r -> r.key }.indexOf(nextStartInstant)
            val durationToNextRider = Duration.ofSeconds(nextStartInstant.epochSecond - currentTtTime.epochSecond)

            nextStartRider?.let { rider->
                when(durationToNextRider.seconds){
                    0L -> "[$number] (${rider.firstName} ${rider.lastName}) GO GO GO!!!"
                    in ttIntervalSeconds - 3..ttIntervalSeconds -> {
                        val lastRiderToStart = timeTrial.value?.riders?.first { it.id == departed.last().id }
                        "(${lastRiderToStart?.firstName} ${lastRiderToStart?.lastName}) GO GO GO!!!"
                    }
                    in 1..10 -> return "${rider.firstName} ${rider.lastName} - ${durationToNextRider.seconds}!"
                    in 5..ttIntervalSeconds/4 -> "[$number] (${rider.firstName} ${rider.lastName}) starts in ${ttIntervalSeconds/4} seconds!"
                    in ttIntervalSeconds/4.. ttIntervalSeconds/2 -> "[$number] (${rider.firstName}) ${rider.lastName} starts in ${ttIntervalSeconds/2} seconds!"
                    else -> "Next rider is number [$number] - ${rider.firstName} ${rider.lastName}"
                }
            }
            return "NULL"
        }else{
            return "${finished.count()} riders have finished, ${numberOfRiders - finished.count()} riders on course"
        }
    }
}