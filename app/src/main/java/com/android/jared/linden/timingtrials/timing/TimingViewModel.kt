package com.android.jared.linden.timingtrials.timing

import androidx.core.util.size
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.domain.RiderAssignmentResult
import com.android.jared.linden.timingtrials.domain.TimeTrialHelper
import com.android.jared.linden.timingtrials.util.ConverterUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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

    private var currentTt: TimeTrial? = null
    private var currentTimeString = ""
    private var currentStatusString = ""

    override var eventAwaitingSelection: Long? = null


    private var ttIntervalMilis: Long = 0
    private val timer: Timer = Timer()
    private val TIMER_PERIOD_MS = 20L

    init {
        if (timeTrial.value == null) {
            timeTrial.addSource(timeTrialRepository.getTimingTimeTrial()) { result ->

                if(timeTrial.value == null){
                    result?.let {tt->

                        currentTt = tt
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
    }

    fun onRiderPassed(){

        currentTt?.let { tte->
            val now = Instant.now().toEpochMilli() - tte.timeTrialHeader.startTime.toInstant().toEpochMilli()
            val newEvents = tte.eventList.toMutableList()
            newEvents.add(TimeTrialEvent(tte.timeTrialHeader.id?:0, null, now, EventType.RIDER_PASSED))
            currentTt = tte.copy(eventList = newEvents)
        }
    }

    fun tryAssignRider(ttRider: TimeTrialRider): RiderAssignmentResult{
        timeTrial.value?.let{tt->
            ttRider.rider.id?.let {riderId->
                eventAwaitingSelection?.let { eid->
                    val helper = TimeTrialHelper(tt)
                   val res = helper.assignRiderToEvent(riderId, eid)
                    if(res.succeeded)
                    {
                        currentTt = res.tt
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
        currentTt?.let { tt->
            val now = Instant.now()
            val millisSinceStart = now.toEpochMilli() - tt.timeTrialHeader.startTime.toInstant().toEpochMilli()


           val updatedTimeTrial = updateEvents(millisSinceStart, tt)


            val newTimeString = ConverterUtils.toTenthsDisplayString(now.toEpochMilli() - tt.timeTrialHeader.startTime.toInstant().toEpochMilli())
            if(currentTimeString != newTimeString){
                currentTimeString = newTimeString
                timeString.postValue(newTimeString)
            }


            val newStatusString = getStatusString(millisSinceStart, updatedTimeTrial)
            if(newStatusString != currentStatusString){
                currentStatusString = newStatusString
                statusString.postValue(newStatusString)
            }

            currentTt = updatedTimeTrial
            if(timeTrial.value != currentTt){
                timeTrial.postValue(currentTt)
            }


        }
    }

    private fun updateEvents(millisSinceStart: Long, tt: TimeTrial): TimeTrial{

        //val ridersWhoShouldHaveStarted = tt.helper.riderStartTimes.headMap(millisSinceStart)
        val sparse = tt.helper.sparseRiderStartTimes
        val index = sparse.indexOfKey(millisSinceStart)
        val startedIndexes = if(index >= 0){ index }else{ Math.abs(index) - 2 }
        val shouldHaveStartedIds = mutableListOf<Long>()
        var i = 0
        while (i <= startedIndexes) {
            val obj = sparse.valueAt(i)
            obj.rider.id?.let { shouldHaveStartedIds.add(it) }
            i++
        }

        val started = tt.helper.departedRidersFromEvents.asSequence().map { it.rider.id }
        val newStartingIds = shouldHaveStartedIds.asSequence().filter { !started.contains(it) }
        val newEvents = newStartingIds.map { id -> tt.helper.getRiderById(id)}.mapNotNull { it?.let {ttr->  TimeTrialEvent(tt.timeTrialHeader.id?:0, ttr.rider.id, tt.helper.getRiderStartTime(ttr), EventType.RIDER_STARTED) }  }.toList()

        if(newEvents.isNotEmpty()){ return tt.copy(eventList = tt.eventList.plus(newEvents)) }
        return  tt

    }

    fun finishTt(){
        timer.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            timeTrial.value?.let {
                val headerCopy = it.timeTrialHeader.copy(status = TimeTrialStatus.FINISHED)
                timeTrialRepository.insertOrUpdate(it.copy(timeTrialHeader = headerCopy))
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }


    private fun getStatusString(millisSinceStart: Long, tte: TimeTrial): String{

        val sparse = tte.helper.sparseRiderStartTimes
        val index = sparse.indexOfKey(millisSinceStart)
        val prevIndex = if(index >= 0){ index }else{ Math.abs(index) - 2 }
        val nextIndex = prevIndex + 1

        //val ridersWhoShouldHaveStarted = tte.helper.riderStartTimes.headMap(millisSinceStart)
        //val nextRiderStart = tte.helper.riderStartTimes.tailMap(millisSinceStart)

        val ss = tte.helper.sparseRiderStartTimes.indexOfKey(millisSinceStart)

        if(nextIndex < tte.helper.sparseRiderStartTimes.size){

            //If we are more than 1 min before TT start time
            val nextStartMilli = sparse.keyAt(0)
            if((nextStartMilli - millisSinceStart) > 60000){
                return "TimeTrial starts at 0:00:00:0"
            }

                val nextStartRider = sparse.valueAt(nextIndex)
                val millisToNextRider = (nextStartMilli - millisSinceStart)

                    val riderString = "(${nextStartRider.number}) ${nextStartRider.rider.firstName} ${nextStartRider.rider.lastName}"
                    return when(millisToNextRider){
                      in ttIntervalMilis - 3000..ttIntervalMilis ->
                        {
                            if(prevIndex >= 0){
                                val prevRider = sparse.valueAt(prevIndex)
                                 "(${prevRider.rider.firstName} ${prevRider.rider.lastName}) GO GO GO!!!"
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
                return "${tte.helper.finishedRidersFromEvents.size} riders have finished, ${tte.riderList.size - tte.helper.finishedRidersFromEvents.size} riders on course"
            }

    }
}