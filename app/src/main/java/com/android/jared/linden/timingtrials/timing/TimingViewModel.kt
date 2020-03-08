package com.android.jared.linden.timingtrials.timing

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.android.jared.linden.timingtrials.domain.ITimelineEvent
import com.android.jared.linden.timingtrials.domain.TimeLine
import com.android.jared.linden.timingtrials.domain.TimeTrialHelper
import com.android.jared.linden.timingtrials.util.ConverterUtils
import com.android.jared.linden.timingtrials.util.Event
import kotlinx.coroutines.*
import org.threeten.bp.Instant
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface IEventSelectionData{
    var eventAwaitingSelection: Long?
}

class TimingViewModel  @Inject constructor(val timeTrialRepository: ITimeTrialRepository, val resultRepository: TimeTrialRiderRepository) : ViewModel(), IEventSelectionData {

    val timeTrial: MediatorLiveData<TimeTrial> = MediatorLiveData()
    private val liveMilisSinceStart: MutableLiveData<Long> = MutableLiveData()

    val timeLine: MediatorLiveData<TimeLine> = MediatorLiveData()
    private var prevMilis = 0L
    private var prevString = ""
    val timeString: LiveData<String> = Transformations.map(liveMilisSinceStart){
        if(((it % 1000) / 100) != prevMilis){
            prevMilis = it
            prevString = ConverterUtils.toTenthsDisplayString(it)
        }
        prevString

    }
    val statusString: MutableLiveData<String> = MutableLiveData()
    val messageData: MutableLiveData<Event<String>> = MutableLiveData()



    private var currentStatusString = ""
    override var eventAwaitingSelection: Long? = null


    init {
        timeTrial.addSource(timeTrialRepository.getTimingTimeTrial()) {new ->
            if(new != null && !isCorotineAlive.get() && !new.equalsOtherExcludingIds(timeTrial.value)) {
                Timber.d("TimingTt self updating TT, ${new.timeTrialHeader.timeStamps} unassigned")
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
            if (tte.helper.sortedRiderStartTimes.firstKey() > now){
                showMessage("First rider has not started yet")
            }else{
                val newHeader = tte.timeTrialHeader.copy(timeStamps = tte.timeTrialHeader.timeStamps + now)
                updateTimeTrial(tte.copy(timeTrialHeader = newHeader))
                if(eventAwaitingSelection == null){
                    eventAwaitingSelection = now
                }
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

    fun unassignRiderFromEvent(event: ITimelineEvent){
        event.rider?.let { rider->
            timeTrial.value?.let { tt->
                updateTimeTrial(tt.helper.unassignRiderFromEvent(rider.timeTrialData, event.timeStamp))
            }
        }
    }

    var iters =0
    var looptime = 0L

    var queue = ConcurrentLinkedQueue<TimeTrial>()
    private val isCorotineAlive = AtomicBoolean()

    private fun updateTimeTrial(newtt: TimeTrial){
        timeTrial.value = newtt
        Timber.d("Update TT, ${newtt.riderList.size} riders")
            if(isCorotineAlive.compareAndSet(false, true)){
                queue.add(newtt)
                viewModelScope.launch(Dispatchers.IO) {
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

    private fun backgroundUpdateTt(newtt: TimeTrial){
        timeTrial.postValue(newtt)
        Timber.d("Update TT, ${newtt.riderList.size} riders")
        if(isCorotineAlive.compareAndSet(false, true)){
            queue.add(newtt)
            viewModelScope.launch(Dispatchers.IO) {
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
            if(iters++ == 100){
                Timber.d("Time for 100 loops =  $looptime")
                looptime = 0
                iters = 0
            }

        }

    }

    fun moveRiderToBack(rider: TimeTrialRider){
        timeTrial.value?.let {
            updateTimeTrial(it.helper.moveRiderToBack(rider))
        }
    }

    fun riderDns(rider: TimeTrialRider){
        timeTrial.value?.let {
            updateTimeTrial(it.helper.riderDns(rider))
        }
    }

    fun riderDnf(rider: TimeTrialRider){
        timeTrial.value?.let {
            updateTimeTrial(it.helper.riderDnf(rider))
        }
    }

    fun setRiderStartTime(riderId: Long, startTime: Long){
        timeTrial.value?.let {
            updateTimeTrial(it.helper.setRiderStartTime(riderId, startTime))
        }
    }

    fun undoDnf(rider: TimeTrialRider){
        timeTrial.value?.let {
            updateTimeTrial(it.helper.undoDnf(rider))
        }
    }


    fun finishTt(){
        timeTrial.value?.let {
            calculatePbs()
        }
    }

    val calcPbsCorotineAlive = AtomicBoolean()
    fun calculatePbs(){
        timeTrial.value?.let {tt->
            tt.course?.id?.let {courseId->
                if (calcPbsCorotineAlive.compareAndSet(false, true)) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            //Must be ordered
                            val ttsOnTheCourse = timeTrialRepository.getAllHeaderBasicInfo().asSequence().filter { it.courseId == courseId && it.laps == tt.timeTrialHeader.laps && it.id != tt.timeTrialHeader.id }.map { it.id }.toList()
                            val allRes = resultRepository.getCourseResultsSuspend(courseId).filter { it.timeTrialId?.let { ttsOnTheCourse.contains(it)}?:false}
                            val updatedTt = writeRecordsToNotes(tt, allRes)
                            val updt = updatedTt.copy(timeTrialHeader = updatedTt.timeTrialHeader.copy(status = TimeTrialStatus.FINISHED))
                            backgroundUpdateTt(updt)

                        }finally {
                            calcPbsCorotineAlive.set(false)
                        }
                    }
                }
            }

        }
    }

    val PRString = "PR"
    val CRString = "CR"

    fun writeRecordsToNotes(timeTrial: TimeTrial, courseResults: List<TimeTrialRider>): TimeTrial{

        val maleCr = courseResults.firstOrNull{it.gender == Gender.MALE}?.finishTime
        val femaleCr = courseResults.firstOrNull{it.gender == Gender.FEMALE}?.finishTime

        val copList = timeTrial.riderList.asSequence().map { ttr->
            courseResults.firstOrNull{it.finishTime != null && it.riderId == ttr.riderData.id}?.let { existingResult->
                val rTime = ttr.timeTrialData.finishTime
                val eTime = existingResult.finishTime
                if(rTime != null && eTime != null && eTime > rTime){
                    ttr.copy(timeTrialData = ttr.timeTrialData.copy(notes = PRString))
                }else{
                    ttr
                }

            }?:ttr
        }.toList()

        val maleWithCr = timeTrial.helper.results.filter { it.riderData.gender == Gender.MALE && (it.timeTrialData.finishTime?:Long.MAX_VALUE) < (maleCr ?: 0) }.minBy {
            it.resultTime ?: Long.MAX_VALUE
        }
        val femaleWithCr = timeTrial.helper.results.filter { it.riderData.gender == Gender.FEMALE && (it.timeTrialData.finishTime?: Long.MAX_VALUE) < (femaleCr ?: 0) }.minBy {
            it.resultTime ?: Long.MAX_VALUE
        }

        val nCList = copList.map {
           when (it.riderData.id) {
               maleWithCr?.riderData?.id -> it.copy(timeTrialData = it.timeTrialData.copy(notes = CRString))
               femaleWithCr?.riderData?.id -> it.copy(timeTrialData = it.timeTrialData.copy(notes = CRString))
               else -> it
           }
       }.toList()

//        timeTrial.riderList.forEach { res->
//            courseResults.firstOrNull{it.finishTime != null && it.riderId == res.riderData.id}?.let { existingResult->
//                val rTime = res.timeTrialData.finishTime
//                val eTime = existingResult.finishTime
//                if(rTime != null && eTime != null && eTime > rTime){
//                    newRiderList.add(res.copy(timeTrialData = res.timeTrialData.copy(notes = "PR")))
//                }
//            }
//        }
        return timeTrial.updateRiderList(nCList)
    }


    fun discardTt(){
        Timber.d("JAREDMSG -> TIMINGVM -> Deleting TT")
        viewModelScope.launch(Dispatchers.IO) {
            var deleted = false
            while (!deleted){
                if(isCorotineAlive.compareAndSet(false, true)){
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


                    val riderString = if(tte.timeTrialHeader.interval != 0){
                        "(${tte.getRiderNumber(nextStartRider.timeTrialData.index)}) ${nextStartRider.riderData.firstName} ${nextStartRider.riderData.lastName}"
                    }else{
                        "All Riders"
                    }
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