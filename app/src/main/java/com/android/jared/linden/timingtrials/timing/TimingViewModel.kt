package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.domain.TimeLine
import com.android.jared.linden.timingtrials.domain.TimeTrialHelper
import com.android.jared.linden.timingtrials.util.ConverterUtils
import com.android.jared.linden.timingtrials.util.Event
import kotlinx.coroutines.*
import org.threeten.bp.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface IEventSelectionData{
    var eventAwaitingSelection: Long?
}

class TimingViewModel  @Inject constructor(val timeTrialRepository: ITimeTrialRepository) : ViewModel(), IEventSelectionData {

    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()
    private val liveMilisSinceStart: MutableLiveData<Long> = MutableLiveData()

    val timeLine: MediatorLiveData<TimeLine> = MediatorLiveData()
    val timeString: LiveData<String> = Transformations.map(liveMilisSinceStart){
        ConverterUtils.toTenthsDisplayString(it)
    }
    val statusString: MutableLiveData<String> = MutableLiveData()
    val messageData: MutableLiveData<Event<String>> = MutableLiveData()



    private var currentStatusString = ""

    override var eventAwaitingSelection: Long? = null


    init {
        timeTrial.addSource(timeTrialRepository.nonFinishedFullTimeTrial) {new ->
            if(new != null && !isCorotineAlive.get() && !new.equalsOtherExcludingIds(timeTrial.value)) {
                println("JAREDMSG -> TIMINGVM -> TimingTt self updating TT, ${new.timeTrialHeader.timeStamps} unassigned")
                timeTrial.value = new
            }
        }

        timeLine.addSource(timeTrial){tt->
            timeLine.value = TimeLine(tt, Instant.now().toEpochMilli() - tt.timeTrialHeader.startTimeMilis)
        }
        timeLine.addSource(liveMilisSinceStart){millis->
            if(timeLine.value?.isValidForTimeStamp(millis) != true){
                timeTrial.value?.let {tt->
                    timeLine.value = TimeLine(tt, millis)
                }
            }
        }
    }

    private fun showMessage(mesg: String){
        messageData.postValue(Event(mesg))
    }

    fun onRiderPassed(){
        timeTrial.value?.let { tte->
            val now = Instant.now().toEpochMilli() - tte.timeTrialHeader.startTimeMilis
            if (tte.helper.riderStartTimes.firstKey() > now){
                showMessage("First rider has not started yet")
            }else{
                val newHeader = tte.timeTrialHeader.copy(timeStamps = tte.timeTrialHeader.timeStamps + now)
                updateTimeTrial(tte.copy(timeTrialHeader = newHeader))
            }
        }
    }

    fun tryAssignRider(ttRider: TimeTrialRider){
        timeTrial.value?.let{tt->

                eventAwaitingSelection?.let { eid->
                    val helper = TimeTrialHelper(tt)
                   val res = helper.assignRiderToEvent(ttRider, eid)
                    if(res.succeeded)
                    {
                        updateTimeTrial(res.tt)
                        eventAwaitingSelection = null
                    }else{
                        showMessage(res.message)
                    }
                }

        }

    }

    var iters =0
    var looptime = 0L

    var queue = ConcurrentLinkedQueue<TimeTrial>()
    var isCorotineAlive = AtomicBoolean()

    private fun updateTimeTrial(newtt: TimeTrial){
        timeTrial.value = newtt
        println("JAREDMSG -> TIMINGVM -> Update TT, ${newtt.riderList.size} riders")
            if(!isCorotineAlive.get()){
                queue.add(newtt)
                viewModelScope.launch(Dispatchers.IO) {
                    isCorotineAlive.set(true)
                    while (queue.peek() != null){
                        var ttToInsert = queue.peek()
                        while (queue.peek() != null){
                            ttToInsert = queue.poll()
                        }
                       ttToInsert?.let {
                           timeTrialRepository.updateFull(it)
                       }
                    }
                    isCorotineAlive.set(false)
                }
            }else{
                queue.add(newtt)
            }
    }

    fun updateLoop(){
        timeTrial.value?.let { tt->

            val startts = System.currentTimeMillis()
            val millisSinceStart = startts - tt.timeTrialHeader.startTimeMilis

            liveMilisSinceStart.postValue(millisSinceStart)


            val newStatusString = getStatusString(millisSinceStart, tt)
            if(newStatusString != currentStatusString){
                currentStatusString = newStatusString
                statusString.postValue(newStatusString)
            }

            val endtime = System.currentTimeMillis() - startts
            looptime += endtime
            if(iters++ == 10000){
                println("JAREDMSG -> TIMINGVM -> Time for 10000 loops =  $looptime")
                looptime = 0
                iters = 0
            }

        }

    }

    fun finishTt(){
        //timer.cancel()


            timeTrial.value?.let {
                val headerCopy = it.timeTrialHeader.copy(status = TimeTrialStatus.FINISHED)
                updateTimeTrial(it.copy(timeTrialHeader = headerCopy))
            }
    }

    fun discardTt(){
        println("JAREDMSG -> TIMINGVM -> Deleting TT")
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
        timeTrial.value?.let {
            val headerCopy = it.timeTrialHeader.copy(status = TimeTrialStatus.SETTING_UP)
            updateTimeTrial(it.copy(timeTrialHeader = headerCopy))
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
        val ttIntervalMilis = (tte.timeTrialHeader.interval * 1000L)

        if(nextIndex < tte.helper.sparseRiderStartTimes.size()){

            //If we are more than 1 min before TT start time
            val nextStartMilli = sparse.keyAt(nextIndex)
            if((nextStartMilli - millisSinceStart) > 60000){
                return "${tte.timeTrialHeader.ttName} starts at 0:00:00:0"
            }

                val nextStartRider = sparse.valueAt(nextIndex)
                val millisToNextRider = (nextStartMilli - millisSinceStart)

                    val riderString = "(${nextStartRider.timeTrialData.number}) ${nextStartRider.riderData.firstName} ${nextStartRider.riderData.lastName}"
                    return when(millisToNextRider){

                        in 0L..10000 -> {
                            var x = millisToNextRider
                            if(x > 1000){
                                do{x /= 10} while (x > 9)
                            }else{
                                x = 0
                            }
                            "${nextStartRider.riderData.firstName} ${nextStartRider.riderData.lastName} - ${x+1}!"
                            }
                        in 5..ttIntervalMilis/4 ->
                            "$riderString starts in ${ttIntervalMilis/4000} seconds!"
                        in ttIntervalMilis/4.. ttIntervalMilis/2 ->
                            "$riderString starts in ${ttIntervalMilis/2000} seconds"
                        in (ttIntervalMilis - 3000)..ttIntervalMilis ->
                        {
                            if(prevIndex >= 0){
                                val prevRider = sparse.valueAt(prevIndex)
                                "(${prevRider.riderData.firstName} ${prevRider.riderData.lastName}) GO GO GO!!!"
                            }else{
                                "Next rider is $riderString"
                            }

                        }
                        else ->
                            "Next rider is $riderString"
                    }

                //return "NULL"
            }else{
                return "${tte.helper.finishedRiders.size} riders have finished, ${tte.riderList.size - tte.helper.finishedRiders.size} riders on course"
            }

    }
}