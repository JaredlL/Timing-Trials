package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.domain.RiderAssignmentResult
import com.android.jared.linden.timingtrials.domain.TimeTrialHelper.assignRiderToEvent
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

//    private var departedRidersIdsCached = ArrayList<Long>()
//    private var finishedRidersIdsCached = ArrayList<Long>()


//    private fun getFinishedRiders(): List<TimeTrialRider> {
//        return timeTrial.value?.let { tt->
//            tt.eventList.filter { it.eventType == EventType.RIDER_PASSED }.mapNotNull {event-> tt.riderList.firstOrNull { rn -> rn.rider.id == event.riderId }  }
//        }?: listOf()
//    }


    private var ttIntervalMilis: Long = 0
    private val timer: Timer = Timer()
    private val TIMER_PERIOD_MS = 50L

    init {
        if (timeTrial.value == null) {
            timeTrial.addSource(timeTrialRepository.getTimingTimeTrial()) { result ->
                result?.let {tt->

                    ttIntervalMilis = (tt.timeTrialHeader.interval * 1000).toLong()

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
            val now = Instant.now().toEpochMilli() - tte.timeTrialHeader.startTime.toInstant().toEpochMilli()
            val newEvents = tte.eventList.toMutableList()
            newEvents.add(TimeTrialEvent(tte.timeTrialHeader.id?:0, null, now, EventType.RIDER_PASSED))
            timeTrial.postValue(tte.copy(eventList = newEvents))
        }
    }

    fun tryAssignRider(ttRider: TimeTrialRider): RiderAssignmentResult{
        timeTrial.value?.let{tt->
            ttRider.rider.id?.let {riderId->
                eventAwaitingSelection?.let { eid->
                   val res = assignRiderToEvent(riderId, eid, tt)
                    if(res.succeeded)
                    {
                        timeTrial.value = res.tt
                        eventAwaitingSelection = null
                    }
                    return res
                }
            }
            return RiderAssignmentResult(false, "Null", tt)
        }
        return RiderAssignmentResult(false, "Null", TimeTrial.createBlank())
    }


    private fun updateLoop(){
        timeTrial.value?.let { tt->
            val now = Instant.now()
            val millisSinceStart = now.toEpochMilli() - tt.timeTrialHeader.startTime.toInstant().toEpochMilli()

            //val dr: ArrayList<Long> = departedRidersIdsCached
            //val fr: ArrayList<Long> = finishedRidersIdsCached



           val updatedTimeTrial = updateEvents(millisSinceStart, tt)

            timeString.postValue(ConverterUtils.toTenthsDisplayString(now.toEpochMilli() - tt.timeTrialHeader.startTime.toInstant().toEpochMilli()))
            statusString.postValue(getStatusString(millisSinceStart, updatedTimeTrial))

            timeTrial.postValue(updatedTimeTrial)

        }
    }

    private fun updateEvents(millisSinceStart: Long, tt: TimeTrial): TimeTrial{

        val bSearchResult = tt.riderList.map { r -> r.startTime }.binarySearch(millisSinceStart)
        val nextRiderIndex = if(bSearchResult < 0) bSearchResult.unaryMinus() - 1 else bSearchResult + 1

        val ridersWhoShouldHaveStarted = tt.riderList.take(nextRiderIndex)
        val newStartingIds = ridersWhoShouldHaveStarted.mapNotNull { it.rider.id }.subtract(tt.getDepartedRiders().mapNotNull { it.rider.id })
        if(newStartingIds.isNotEmpty()){

                val newEvents = tt.eventList.toMutableList()
                newEvents.addAll(newStartingIds.map { TimeTrialEvent(tt.timeTrialHeader.id?:0, it, millisSinceStart, EventType.RIDER_STARTED) })
            return tt.copy(eventList = newEvents)
               // tt.eventList = newEvents
                //timeTrial.postValue(tt)

            }
        return  tt

    }


    private fun getStatusString(millisSinceStart: Long, tte: TimeTrial): String{

        val bSearchResult = tte.riderList.map { r -> r.startTime }.binarySearch(millisSinceStart)
        val nextRiderIndex = if(bSearchResult < 0) bSearchResult.unaryMinus() - 1 else bSearchResult + 1
        val stillToGo = tte.riderList.subList(nextRiderIndex, tte.riderList.count())


            if(stillToGo.isNotEmpty()){

                //If we are more than 1 min before TT start time
                if((millisSinceStart + 60000) < (stillToGo.first().startTime)){
                    //val dur = Duration.between(currentTime, ridersToStart.firstKey())
                    return "TimeTrialHeader starts at 00:00:0, First rider off 1 minute later"
                }


                val nextStartMilli = stillToGo.first().startTime
                val nextStartRider = stillToGo.first()
                val millisToNextRider = (nextStartMilli - millisSinceStart)

                    val riderString = "(${nextStartRider.number}) ${nextStartRider.rider.firstName} ${nextStartRider.rider.lastName}"
                    return when(millisToNextRider){
                      in ttIntervalMilis - 3000..ttIntervalMilis ->
                        {
                            val lastRiderToStart = tte.riderList.firstOrNull { it.rider.id == tte.getDepartedRiders().mapNotNull { it.rider.id }.lastOrNull() }
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
                return "${tte.getFinishedRiders().count()} riders have finished, ${tte.riderList.count() - tte.getFinishedRiders().count()} riders on course"
            }

    }
}