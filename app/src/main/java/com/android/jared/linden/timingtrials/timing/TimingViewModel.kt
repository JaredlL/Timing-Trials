package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.util.ConverterUtils
import org.threeten.bp.Instant
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

interface IEventSelectionData{
    var eventAwaitingSelection: Long?
}

class TimingViewModel  @Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel(), IEventSelectionData {

    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()
    val timeString: MutableLiveData<String> = MutableLiveData()
    val statusString: MutableLiveData<String> = MutableLiveData()

    override var eventAwaitingSelection: Long? = null

    private var departedRidersIdsCached = ArrayList<Long>()
    private var finishedRidersIdsCached = ArrayList<Long>()


    private fun getFinishedRiders(): List<TimeTrialRider> {
        return timeTrial.value?.let { tt->
            tt.eventList.filter { it.eventType == EventType.RIDER_FINISHED }.mapNotNull {event-> tt.riderList.firstOrNull { rn -> rn.rider.id == event.riderId }  }
        }?: listOf()
    }

    private fun getDepartedRiders(): List<TimeTrialRider> {
        return timeTrial.value?.let { tt->
            tt.eventList.filter { it.eventType == EventType.RIDER_STARTED}.mapNotNull { event-> tt.riderList.firstOrNull { rn -> rn.rider.id == event.riderId }  }
        }?: listOf()
    }


    private var ttIntervalMilis: Long = 0
    private var timer: Timer = Timer()
    private val TIMER_PERIOD_MS = 50L

    init {
        if (timeTrial.value == null) {
            timeTrial.addSource(timeTrialRepository.getTimingTimeTrial()) { result ->
                result?.let {tt->
                    tt.riderList.forEachIndexed{ index, rider ->
                        rider.number = index + 1
                        rider.startTime = (tt.timeTrialHeader.interval * 1000 * (index + 1)).toLong()
                    }

                    ttIntervalMilis = (tt.timeTrialHeader.interval * 1000).toLong()

                    departedRidersIdsCached.clear()
                    finishedRidersIdsCached.clear()

                    departedRidersIdsCached.addAll(getDepartedRiders().mapNotNull { it.rider.id })
                    finishedRidersIdsCached.addAll(getFinishedRiders().mapNotNull { it.rider.id })



                    timer = Timer()
                    val task = object : TimerTask(){
                        override fun run() {
                            updateLoop()
                        }
                    }
                    timer.scheduleAtFixedRate(task, 0L, TIMER_PERIOD_MS)

                    timeTrial.value = tt
                }
            }
        }
    }

    fun onRiderPassed(){

        timeTrial.value?.let { tte->
            val now = Instant.now().toEpochMilli() - tte.timeTrialHeader.startTime.toEpochMilli()
            val newEvents = tte.eventList.toMutableList()
            newEvents.add(TimeTrialEvent(tte.timeTrialHeader.id?:0, null, now, EventType.RIDER_PASSED))
            tte.eventList = newEvents
            timeTrial.postValue(tte)
        }
    }


    private fun updateLoop(){
        timeTrial.value?.let { tt->
            val now = Instant.now()
            val millisSinceStart = now.toEpochMilli() - tt.timeTrialHeader.startTime.toEpochMilli()

            //val dr: ArrayList<Long> = departedRidersIdsCached
            //val fr: ArrayList<Long> = finishedRidersIdsCached

            val bSearchResult = tt.riderList.map { r -> r.startTime }.binarySearch(millisSinceStart)
            val nextRiderIndex = if(bSearchResult < 0) bSearchResult.unaryMinus() - 1 else bSearchResult + 1

            updateEvents(millisSinceStart, nextRiderIndex)

            timeString.postValue(ConverterUtils.toTenthsDisplayString(now.toEpochMilli() - tt.timeTrialHeader.startTime.toEpochMilli()))
            statusString.postValue(getStatusString(millisSinceStart, nextRiderIndex))

        }
    }

    private fun updateEvents(millisSinceStart: Long, nextRiderIndex: Int){
        timeTrial.value?.let {tte->

            val ridersWhoShouldHaveStarted = tte.riderList.take(nextRiderIndex)
            val newStartingIds = ridersWhoShouldHaveStarted.mapNotNull { it.rider.id }.subtract(departedRidersIdsCached)
            if(newStartingIds.isNotEmpty()){

                departedRidersIdsCached.addAll(newStartingIds)
                val newEvents = tte.eventList.toMutableList()
                newEvents.addAll(newStartingIds.map { TimeTrialEvent(tte.timeTrialHeader.id?:0, it, millisSinceStart, EventType.RIDER_STARTED) })
                tte.eventList = newEvents
                timeTrial.postValue(tte)

            }
        }
    }


    private fun getStatusString(millisSinceStart: Long, nextRiderIndex: Int): String{

        timeTrial.value?.let { tte ->

            val stillToGo = tte.riderList.subList(nextRiderIndex, tte.riderList.count())


            if(stillToGo.isNotEmpty()){

                //If we are more than 1 min before TT start time
                if((millisSinceStart + 60000) < (stillToGo.first().startTime ?: 0)){
                    //val dur = Duration.between(currentTime, ridersToStart.firstKey())
                    return "TimeTrialHeader starts at 00:00:0, First rider off 1 minute later"
                }


                val nextStartMilli = stillToGo.first().startTime?:0
                val nextStartRider = stillToGo.first()
                val millisToNextRider = (nextStartMilli - millisSinceStart)

                    val riderString = "(${nextStartRider.number}) ${nextStartRider.rider.firstName} ${nextStartRider.rider.lastName}"
                    return when(millisToNextRider){
                      in ttIntervalMilis - 3000..ttIntervalMilis ->
                        {
                            val lastRiderToStart = tte.riderList.firstOrNull { it.rider.id == departedRidersIdsCached.lastOrNull() }
                            if(lastRiderToStart != null){
                                 "(${lastRiderToStart.rider.firstName} ${lastRiderToStart.rider.lastName}) GO GO GO!!!"
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
                            "${nextStartRider.rider.firstName} ${nextStartRider.rider.lastName} - ${x+1}!"
                            }
                        in 5..ttIntervalMilis/4 ->
                            "$riderString starts in ${ttIntervalMilis/4000} seconds!"
                        in ttIntervalMilis/4.. ttIntervalMilis/2 ->
                            "$riderString starts in ${ttIntervalMilis/2000} seconds"
                        else ->
                            "Next rider is $riderString"
                    }

                //return "NULL"
            }else{
                return "${finishedRidersIdsCached.count()} riders have finished, ${tte.riderList.count() - finishedRidersIdsCached.count()} riders on course"
            }
        }
        return "No TT Loaded"
    }
}