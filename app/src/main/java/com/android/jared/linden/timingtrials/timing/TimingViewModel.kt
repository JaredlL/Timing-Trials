package com.android.jared.linden.timingtrials.timing

import androidx.core.util.size
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.domain.TimeLine
import com.android.jared.linden.timingtrials.domain.TimeTrialHelper
import com.android.jared.linden.timingtrials.util.ConverterUtils
import kotlinx.coroutines.*
import org.threeten.bp.Instant
import java.util.*
import javax.inject.Inject

interface IEventSelectionData{
    var eventAwaitingSelection: Long?
}

class TimingViewModel  @Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel(), IEventSelectionData {

    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()
    val timeLine: MutableLiveData<TimeLine> = MutableLiveData()
    val timeString: MutableLiveData<String> = MutableLiveData()
    val statusString: MutableLiveData<String> = MutableLiveData()

    private var currentTt: TimeTrial? = null
    private var currentTimeString = ""
    private var currentStatusString = ""

    override var eventAwaitingSelection: Long? = null


    private var ttIntervalMilis: Long = 0
    private val timer: Timer = Timer()
    private val TIMER_PERIOD_MS = 50L

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

    var showMessage: (String) -> Unit = {}

    fun onRiderPassed(){

        currentTt?.let { tte->
            val now = Instant.now().toEpochMilli() - tte.timeTrialHeader.startTime.toInstant().toEpochMilli()
            if (tte.helper.riderStartTimes.firstKey() > now){
                showMessage("First rider has not started yet")
            }else{
                val newEvents = tte.eventList.toMutableList()
                newEvents.add(RiderPassedEvent(timeTrialId = tte.timeTrialHeader.id?:0, riderId = null, timeStamp =  now))
                currentTt = tte.copy(eventList = newEvents)
            }

        }
    }

    fun tryAssignRider(ttRider: TimeTrialRider){
        timeTrial.value?.let{tt->
            ttRider.rider.id?.let {riderId->
                eventAwaitingSelection?.let { eid->
                    val helper = TimeTrialHelper(tt)
                   val res = helper.assignRiderToEvent(riderId, eid)
                    if(res.succeeded)
                    {
                        currentTt = res.tt
                        eventAwaitingSelection = null
                    }else{
                        showMessage(res.message)
                    }

                    //return res
                }
            }
            //showMessage("Null")
            //return RiderAssignmentResult(false, "Null", tt)
        }
        //showMessage("Null")
        //return RiderAssignmentResult(false, "Null", TimeTrial.createBlank())
    }

    var iters =0
    var looptime = 0L

    private fun updateLoop(){
        currentTt?.let { tt->

            val startts = System.currentTimeMillis()

            val now = Instant.now()
            val millisSinceStart = now.toEpochMilli() - tt.timeTrialHeader.startTime.toInstant().toEpochMilli()




            val newTimeString = ConverterUtils.toTenthsDisplayString(now.toEpochMilli() - tt.timeTrialHeader.startTime.toInstant().toEpochMilli())
            if(currentTimeString != newTimeString){
                currentTimeString = newTimeString
                timeString.postValue(newTimeString)
            }


            val newStatusString = getStatusString(millisSinceStart, tt)
            if(newStatusString != currentStatusString){
                currentStatusString = newStatusString
                statusString.postValue(newStatusString)
            }

            if(timeTrial.value != currentTt){
                currentTt?.let { ctt->
                    timeTrial.postValue(ctt)
                    timeLine.postValue(TimeLine(ctt, millisSinceStart ))
                }

            }

            timeLine.value?.let {tl->
               if(!tl.isValidForTimeStamp(millisSinceStart)){
                   currentTt?.let {
                       timeLine.postValue(TimeLine(it, millisSinceStart))
                   }

               }
            }
            val endtime = System.currentTimeMillis() - startts
            looptime += endtime
            iters ++
            if(iters == 100){
                System.out.println("LINDENJ -> Time for 100 loops =  $looptime")
                looptime = 0
                iters = 0
            }

        }

    }



//    private fun updateEventsMap(millisSinceStart: Long, tt: TimeTrial): TimeTrial{
//
//        val ridersWhoShouldHaveStarted = tt.helper.riderStartTimes.headMap(millisSinceStart)
//        val started = tt.helper.departedRidersFromEvents.asSequence().map { it.rider.id }
//        val newStartingIds = ridersWhoShouldHaveStarted.asSequence().filter { !started.contains(it.value.rider.id) }
//        val newEvents = newStartingIds.map { RiderPassedEvent(tt.timeTrialHeader.id?:0, it.value.rider.id, it.key, EventType.RIDER_STARTED) }.toList()
//
//        if(newEvents.isNotEmpty()){ return tt.copy(eventList = tt.eventList.plus(newEvents)) }
//        return  tt
//
//    }



//    private fun updateEvents(millisSinceStart: Long, tt: TimeTrial): TimeTrial{
//
//        //val ridersWhoShouldHaveStarted = tt.helper.riderStartTimes.headMap(millisSinceStart)
//        val sparse = tt.helper.sparseRiderStartTimes
//        val index = sparse.indexOfKey(millisSinceStart)
//        val startedIndexes = if(index >= 0){ index }else{ Math.abs(index) - 2 }
//        val shouldHaveStartedIds = mutableSetOf<Long>()
//
//        val rsList = sequence {
//            var i = 0
//            while (i <= startedIndexes) {
//                val obj = sparse.valueAt(i)
//                obj.rider.id?.let { yield(it) }
//                i++
//            }
//        }
//
//        val started =  tt.eventList.asSequence().filter { it.eventType == EventType.RIDER_STARTED}.mapNotNull { it.riderId }
//        val newStartingIds = rsList.minus(started)
//        val newEvents = newStartingIds.map { id -> tt.helper.getRiderById(id)}.mapNotNull { it?.let {ttr->  RiderPassedEvent(tt.timeTrialHeader.id?:0, ttr.rider.id, tt.helper.getRiderStartTime(ttr), EventType.RIDER_STARTED) }  }.toList()
//
//        if(newEvents.isNotEmpty()){ return tt.copy(eventList = tt.eventList.plus(newEvents)) }
//        return  tt
//
//    }

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

    private fun getStatusStringMap(millisSinceStart: Long, tte: TimeTrial): String{


        val ridersWhoShouldHaveStarted = tte.helper.riderStartTimes.headMap(millisSinceStart)
        val nextRiderStart = tte.helper.riderStartTimes.tailMap(millisSinceStart)

        val ss = tte.helper.sparseRiderStartTimes.indexOfKey(millisSinceStart)

        if(nextRiderStart.isNotEmpty()){

            //If we are more than 1 min before TT start time
            val nextStartMilli = nextRiderStart.firstKey()
            if((nextStartMilli - millisSinceStart) > 60000){
                return "TimeTrial starts at 0:00:00:0"
            }

            val nextStartRider = nextRiderStart.values.asSequence().first()
            val millisToNextRider = (nextStartMilli - millisSinceStart)

            val riderString = "(${nextStartRider.number}) ${nextStartRider.rider.firstName} ${nextStartRider.rider.lastName}"
            return when(millisToNextRider){
                in ttIntervalMilis - 3000..ttIntervalMilis ->
                {
                    if(ridersWhoShouldHaveStarted.isNotEmpty()){
                        val last = ridersWhoShouldHaveStarted.values.last()
                        "(${last.rider.firstName} ${last.rider.lastName}) GO GO GO!!!"
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
            val nextStartMilli = sparse.keyAt(nextIndex)
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