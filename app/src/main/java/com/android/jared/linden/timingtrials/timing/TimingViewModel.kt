package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.util.ConverterUtils
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class TimingViewModel  @Inject constructor(val timeTrialEventRepository: ITimeTrialEventRepository) : ViewModel() {

    val timeTrialWithEvents: MediatorLiveData<TimeTrialWithEvents> = MediatorLiveData()
    val riderStatus = Transformations.map(timeTrialWithEvents){
        it?.let {

        }
    }
    val timeString: MutableLiveData<String> = MutableLiveData()
    val statusString: MutableLiveData<String> = MutableLiveData()
    val allTtWithEvent = timeTrialEventRepository.getAllTimeTrialEvents()


    private var riderStartTimes: SortedMap<Instant, RiderWithNumber> = sortedMapOf()
    private var departedRidersCached = ArrayList<RiderWithNumber>()
    private var finishedRidersCached = ArrayList<RiderWithNumber>()
    private var numberOfRiders = 0

    private var ridersWithNumbers: List<RiderWithNumber> = listOf()

    private fun getFinishedRiders(): List<RiderWithNumber> {
        return timeTrialWithEvents.value?.let {tt->
            tt.eventList.filter { it.eventType == EventType.RIDER_FINISHED }.mapNotNull {event-> ridersWithNumbers.firstOrNull { rn -> rn.rider.id == event.riderId }  }
        }?: listOf()
    }

    private fun getDepartedRiders(): List<RiderWithNumber> {
        return timeTrialWithEvents.value?.let {tt->
            tt.eventList.filter { it.eventType == EventType.RIDER_STARTED}.mapNotNull { event-> ridersWithNumbers.firstOrNull { rn -> rn.rider.id == event.riderId }  }
        }?: listOf()
    }


    private var ttIntervalMilis: Long = 0
    private var timer: Timer = Timer()
    private val TIMER_PERIOD_MS = 50L

    fun initialise(timeTrialId: Long) {
        if (timeTrialWithEvents.value == null && timeTrialId != 0L) {
            timeTrialWithEvents.addSource(timeTrialEventRepository.getTimeTrialWithEvents(timeTrialId)) { result ->
                result?.let {tt->
                    timeTrialWithEvents.value = tt
                    ridersWithNumbers = tt.timeTrial.riders.mapIndexed { index, rider -> RiderWithNumber(rider, index + 1) }

                    ttIntervalMilis = tt.timeTrial.interval.toMillis()
                    numberOfRiders = tt.timeTrial.riders.count()

                    departedRidersCached.clear()
                    finishedRidersCached.clear()

                    departedRidersCached.addAll(getDepartedRiders())
                    finishedRidersCached.addAll(getFinishedRiders())

                    riderStartTimes = tt.timeTrial.riders.withIndex()
                            .associate { r -> Pair(
                                    tt.timeTrial.startTime.plusSeconds(0).plusSeconds(tt.timeTrial.interval.seconds * r.index).truncatedTo(ChronoUnit.SECONDS),
                                    RiderWithNumber(r.value, r.index)) }.toSortedMap()

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

    fun onRiderPassed(){
        val now = Instant.now()
        timeTrialWithEvents.value?.let {tte->
            val newEvents = tte.eventList.toMutableList()
            newEvents.add(TimeTrialEvent(tte.timeTrial.id?:0, null, now, EventType.RIDER_PASSED))
            tte.eventList = newEvents
            timeTrialWithEvents.postValue(tte)
        }
    }


    private fun updateLoop(){
        timeTrialWithEvents.value?.let {tt->
            val now = Instant.now()

            val dr: ArrayList<RiderWithNumber> = departedRidersCached
            val fr: ArrayList<RiderWithNumber> = finishedRidersCached

            updateEvents(now, dr)

            timeString.postValue(ConverterUtils.toTenthsDisplayString(now.toEpochMilli() - tt.timeTrial.startTime.toEpochMilli()))
            statusString.postValue(getStatusString(now, dr, fr))

        }
    }

    private fun updateEvents(currentTime: Instant, departed: List<RiderWithNumber>){
        val startedRiders = riderStartTimes.headMap(currentTime)
        val newStartingRiders = startedRiders.values.subtract(departed).toList()
        if(newStartingRiders.isNotEmpty()){

            timeTrialWithEvents.value?.let { tte->
                departedRidersCached.addAll(newStartingRiders)
                val newEvents = tte.eventList.toMutableList()
                newEvents.addAll(newStartingRiders.map { TimeTrialEvent(tte.timeTrial.id?:0, it.rider.id, currentTime, EventType.RIDER_STARTED) })
                tte.eventList = newEvents
                timeTrialWithEvents.postValue(tte)
            }
        }
    }


    private fun getStatusString(currentTime: Instant, departed: List<RiderWithNumber>, finished: List<RiderWithNumber>): String{

        timeTrialWithEvents.value?.let { tte ->



            val ridersToStart = riderStartTimes.tailMap(currentTime)


            if(ridersToStart.isNotEmpty()){

                //If we are more than 1 min before TT start time
                if(currentTime.plusSeconds(60).isBefore(ridersToStart.firstKey())){
                    //val dur = Duration.between(currentTime, ridersToStart.firstKey())
                    return "TimeTrial starts at 00:00:0, First rider off 1 minute later"
                }


                val nextStartInstant = ridersToStart.firstKey()
                val nextStartRider = ridersToStart[nextStartInstant]
                val number = riderStartTimes.map { r -> r.key }.indexOf(nextStartInstant) + 1

                val millisToNextRider = (nextStartInstant.toEpochMilli() - currentTime.toEpochMilli())
                nextStartRider?.let { rn->
                    val riderString = "(${rn.number}) ${rn.rider.firstName} ${rn.rider.lastName}"
                    return when(millisToNextRider){
                      in ttIntervalMilis - 3000..ttIntervalMilis ->
                        {
                            val lastRiderToStart = tte.timeTrial.riders.firstOrNull { it.id == departed.lastOrNull()?.rider?.id }
                            if(lastRiderToStart != null){
                                 "(${lastRiderToStart.firstName} ${lastRiderToStart.lastName}) GO GO GO!!!"
                            }else{
                                "Next rider is $riderString"
                            }

                        }
                        in 0L..10000 -> {
                            var x = millisToNextRider
                            if(x > 1000){
                                do{x /= 10} while (x > 9)
                            }else{
                                x = 0
                            }
                            "${rn.rider.firstName} ${rn.rider.lastName} - ${x+1}!"
                            }
                        in 5..ttIntervalMilis/4 ->
                            "$riderString starts in ${ttIntervalMilis/4000} seconds!"
                        in ttIntervalMilis/4.. ttIntervalMilis/2 ->
                            "$riderString starts in ${ttIntervalMilis/2000} seconds"
                        else ->
                            "Next rider is $riderString"
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