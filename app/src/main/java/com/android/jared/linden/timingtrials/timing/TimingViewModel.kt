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
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.math.abs

interface IEventSelectionData{
    var eventAwaitingSelection: Long?
}

class TimingViewModel  @Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel(), IEventSelectionData {

    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()
    val timeLine: MutableLiveData<TimeLine> = MutableLiveData()
    val timeString: MutableLiveData<String> = MutableLiveData()
    val statusString: MutableLiveData<String> = MutableLiveData()

    private var currentTt: TimeTrial? = null
    private var currentTimeLine: TimeLine? = null
    private var currentTimeString = ""
    private var currentStatusString = ""

    override var eventAwaitingSelection: Long? = null


    init {
        timeTrial.addSource(timeTrialRepository.nonFinishedTimeTrial) { timing ->

            if(timing != null && timing.timeTrialHeader.status == TimeTrialStatus.IN_PROGRESS && !timing.equalsOtherExcludingIds(timeTrial.value)) {
                currentTt = timing
                timeTrial.value = timing
                currentTimeLine = TimeLine(timing, Instant.now().toEpochMilli() - timing.timeTrialHeader.startTime.toInstant().toEpochMilli())
                timeLine.value = currentTimeLine
            }else{
                timeTrial.value = timing
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
                }
            }
        }

    }

    var iters =0
    var looptime = 0L

    val saveInterval = 5000L
    var lastSave = 0L

    var queue = ConcurrentLinkedQueue<TimeTrial>()
    var isCorotineAlive = AtomicBoolean()

    private fun updateTimeTrial(newtt: TimeTrial){
        //_mTimeTrial.value = newtt
            if(!isCorotineAlive.get()){
                queue.add(newtt)
                viewModelScope.launch(Dispatchers.IO) {
                    isCorotineAlive.set(true)
                    while (queue.peek() != null){
                        var ttToInsert = queue.peek()
                        while (queue.peek() != null){
                            ttToInsert = queue.poll()
                        }
                        timeTrialRepository.update(ttToInsert)
                    }
                    isCorotineAlive.set(false)
                }
            }else{
                queue.add(newtt)
            }
    }

    fun updateLoop(millisSinceStart: Long){
        currentTt?.let { tt->

            val startts = System.currentTimeMillis()

            val newTimeString = ConverterUtils.toTenthsDisplayString(millisSinceStart)
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
                    System.out.println("JAREDMSG -> Update TT & TimeLine")
                    timeTrial.postValue(ctt)
                    currentTimeLine = TimeLine(ctt, millisSinceStart )
                    timeLine.postValue(currentTimeLine)

                    if(abs(millisSinceStart - lastSave) > saveInterval){
                        updateTimeTrial(ctt)
                        lastSave = millisSinceStart
                    }

                }

            }

            timeLine.value?.let {tl->
               if(!tl.isValidForTimeStamp(millisSinceStart)){
                   currentTt?.let {
                       System.out.println("JAREDMSG -> Update TimeLine")
                       currentTimeLine = TimeLine(it, millisSinceStart )
                       timeLine.postValue(currentTimeLine)
                   }

               }
            }




            val endtime = System.currentTimeMillis() - startts
            looptime += endtime
            iters ++
            if(iters == 100){
                System.out.println("JAREDMSG -> Time for 100 loops =  $looptime")
                looptime = 0
                iters = 0
            }

        }

    }

    fun finishTt(){
        //timer.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            timeTrial.value?.let {
                val headerCopy = it.timeTrialHeader.copy(status = TimeTrialStatus.FINISHED)
                timeTrialRepository.update(it.copy(timeTrialHeader = headerCopy))
            }
        }
    }

    fun discardTt(){
        //timer.cancel()
        System.out.println("JAREDMSG -> TIMINGVM Deleting TT")
        viewModelScope.launch(Dispatchers.IO) {
            var deleted = false
            while (!deleted){
                if(!isCorotineAlive.get()){
                    isCorotineAlive.set(true)
                    timeTrial.value?.let {
                        timeTrialRepository.delete(it)
                    }
                    isCorotineAlive.set(false)
                    deleted = true
                }else{
                    delay(5L)
                }
            }
        }
    }

    fun backToSetup(){
        currentTt?.let {
            val headerCopy = it.timeTrialHeader.copy(status = TimeTrialStatus.SETTING_UP)
            updateTimeTrial(it.copy(timeTrialHeader = headerCopy, eventList = listOf()))
        }

    }

//    @ExperimentalCoroutinesApi
//    override fun onCleared() {
//        super.onCleared()
//        viewModelScope.cancel()
//    }



    private fun getStatusString(millisSinceStart: Long, tte: TimeTrial): String{

        val sparse = tte.helper.sparseRiderStartTimes
        val index = sparse.indexOfKey(millisSinceStart)
        val prevIndex = if(index >= 0){ index }else{ Math.abs(index) - 2 }
        val nextIndex = prevIndex + 1
        val ttIntervalMilis = (tte.timeTrialHeader.interval * 1000L)

        val ss = tte.helper.sparseRiderStartTimes.indexOfKey(millisSinceStart)

        if(nextIndex < tte.helper.sparseRiderStartTimes.size()){

            //If we are more than 1 min before TT start time
            val nextStartMilli = sparse.keyAt(nextIndex)
            if((nextStartMilli - millisSinceStart) > 60000){
                return "${tte.timeTrialHeader.ttName} starts at 0:00:00:0"
            }

                val nextStartRider = sparse.valueAt(nextIndex)
                val millisToNextRider = (nextStartMilli - millisSinceStart)

                    val riderString = "(${nextStartRider.number}) ${nextStartRider.rider.firstName} ${nextStartRider.rider.lastName}"
                    return when(millisToNextRider){
                      in (ttIntervalMilis - 3000)..ttIntervalMilis ->
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