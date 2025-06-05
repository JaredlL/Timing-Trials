package com.jaredlinden.timingtrials.timing

import androidx.core.util.size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.jaredlinden.timingtrials.data.Gender
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialRider
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.roomrepo.RoomRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.domain.ITimelineEvent
import com.jaredlinden.timingtrials.domain.TimeLine
import com.jaredlinden.timingtrials.domain.TimeTrialHelper
import com.jaredlinden.timingtrials.util.ConverterUtils
import com.jaredlinden.timingtrials.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import timber.log.Timber
import javax.inject.Inject

interface IEventSelectionData{
    var eventAwaitingSelection: Long?
}

@HiltViewModel
class TimingViewModel  @Inject constructor(
    val timeTrialRepository: ITimeTrialRepository,
    val resultRepository: TimeTrialRiderRepository,
    val riderRepository: RoomRiderRepository) : ViewModel(), IEventSelectionData {

    private var currentStatusString = ""
    private val liveMillisSinceStart: MutableLiveData<Long> = MutableLiveData()
    private val timeTrialUpdateChannel = Channel<TimeTrial>(Channel.CONFLATED)
    private var prevMilis = 0L
    private var prevString = ""

    // Job to keep track of the ongoing PBS calculation
    private var pbsCalculationJob: Job? = null

    override var eventAwaitingSelection: Long? = null
    val timeTrial: MediatorLiveData<TimeTrial?> = MediatorLiveData()
    val timeLine: MediatorLiveData<TimeLine> = MediatorLiveData()
    val timeString: LiveData<String> = liveMillisSinceStart.map{
        if((it / 100L) != prevMilis){
            prevMilis = it
            prevString = ConverterUtils.toTenthsDisplayString(it)
        }
        prevString
    }
    val statusString: MutableLiveData<String> = MutableLiveData()
    val messageData: MutableLiveData<Event<String>> = MutableLiveData()


    init {
        timeTrial.addSource(timeTrialRepository.getTimingTimeTrial()) {new ->
            if(new != null && !new.equalsOtherExcludingIds(timeTrial.value)) {
                Timber.d("TimingTt self updating TT, ${new.timeTrialHeader.timeStamps} unassigned")
                timeTrial.value = new
            }else if(new == null){
                timeTrial.value = new
            }
        }

        timeLine.addSource(timeTrial){tt->
            tt?.let {
                timeLine.value = TimeLine(tt, Instant.now().toEpochMilli() - tt.timeTrialHeader.startTimeMillis)
            }

        }
        timeLine.addSource(liveMillisSinceStart){millis->
            if(timeLine.value?.isValidForTimeStamp(millis) != true){
                timeTrial.value?.let {tt->
                    timeLine.value = TimeLine(tt, millis)
                }
            }
        }

        timeTrialUpdateChannel.receiveAsFlow()
            .onEach { ttToUpdate ->
                // Check if the TT we are about to update is the one being/has been discarded.
                // This is a safety check. The primary mechanism is to update LiveData to null.
                if (timeTrial.value?.timeTrialHeader?.id == ttToUpdate.timeTrialHeader.id) {
                    Timber.d("Processing TT update from channel for TT ID: ${ttToUpdate.timeTrialHeader.id}")
                    try {
                        timeTrialRepository.updateFull(ttToUpdate)
                    } catch (e: Exception) {
                        Timber.e(e, "Error updating TimeTrial from channel")
                        showMessage("Error saving time trial update: ${e.localizedMessage}")
                    }
                } else {
                    Timber.d("Skipping update for TT ID ${ttToUpdate.timeTrialHeader.id} as it might have been discarded or changed.")
                }
            }
            .launchIn(viewModelScope) // Collects as long as the ViewModel is alive
    }

    private fun showMessage(mesg: String){
        messageData.postValue(Event(mesg))
    }

    fun onRiderPassed(){
        timeTrial.value?.let { tte->
            val now = Instant.now().toEpochMilli() - tte.timeTrialHeader.startTimeMillis
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

    var iterations = 0
    var looptime = 0L

    private fun updateTimeTrial(newtt: TimeTrial){
        // Update the LiveData immediately for UI responsiveness
        timeTrial.postValue(newtt)
        Timber.d("Queuing TT update, ${newtt.riderList.size} riders, ID: ${newtt.timeTrialHeader.id}")

        // Send to the channel for background processing.
        // trySend will not suspend and will succeed if the channel is not full (CONFLATED always accepts).
        val offerResult = timeTrialUpdateChannel.trySend(newtt)
        if (!offerResult.isSuccess) {
            Timber.w("Failed to send TT update to channel. Closed: ${offerResult.isClosed}, Failed: ${offerResult.isFailure}")
        }
    }

    fun updateLoop(currentTimeMillis: Long){
        timeTrial.value?.let { tt->

            val millisSinceStart = currentTimeMillis - tt.timeTrialHeader.startTimeMillis

            liveMillisSinceStart.value = millisSinceStart

            val newStatusString = getStatusString(millisSinceStart, tt)
            if(newStatusString != currentStatusString){
                currentStatusString = newStatusString
                statusString.value = newStatusString
            }

            val endtime = System.currentTimeMillis() - currentTimeMillis
            looptime += endtime
            if(iterations++ == 100){
                Timber.d("Time for 100 loops =  $looptime")
                looptime = 0
                iterations = 0
            }
        }
    }

    fun moveRiderToBack(rider: TimeTrialRider){
        timeTrial.value?.let {tt->
            tt.riderList.firstOrNull { rider.riderId == it.riderId() }?.let {
                if(it.timeTrialData.splits.isEmpty()){
                    updateTimeTrial(tt.helper.moveRiderToBack(rider))
                }else{
                    showMessage("This rider has already passed. You must unassign them from any pass events first.")
                }
            }

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
        timeTrial.value?.let {tt->
            tt.riderList.firstOrNull { it.riderId() == riderId }?.let {
                if(it.timeTrialData.splits.isEmpty()){
                    updateTimeTrial(tt.helper.setRiderStartTime(riderId, startTime))
                }else{
                    showMessage("This rider has already passed. You must unassign them from any pass events first.")
                }
            }
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

    fun calculatePbs(){
        val currentTt = timeTrial.value ?: run {
            Timber.w("calculatePbs called but timeTrial.value is null.")
            return
        }

        if (pbsCalculationJob?.isActive == true) {
            Timber.d("PBS calculation is already in progress. Ignoring new request.")
            return
        }

        pbsCalculationJob = viewModelScope.launch {
            Timber.d("Starting PBS calculation for TT ID: ${currentTt.timeTrialHeader.id}")
            try {
                val ttWithRecords = currentTt.course?.id?.let { courseId ->
                    val ttLaps = currentTt.timeTrialHeader.laps
                    val ttId = currentTt.timeTrialHeader.id

                    // Fetch all relevant time trial headers on the same course and with the same number of laps
                    val otherTtHeadersOnCourse = timeTrialRepository.getAllHeaderBasicInfo()
                        .filter { header ->
                            header.courseId == courseId &&
                            header.laps == ttLaps &&
                            header.id != ttId
                        }
                        .mapNotNull { it.id } // Get a list of valid IDs

                    // Fetch results only for those identified time trials
                    val allRelevantCourseResults = if (otherTtHeadersOnCourse.isNotEmpty()) {
                        resultRepository.getCourseResultsSuspend(courseId)
                            .filter { courseResult -> otherTtHeadersOnCourse.contains(courseResult.timeTrialId) }
                    } else {
                        emptyList()
                    }
                    writeRecordsToNotes(currentTt, allRelevantCourseResults)
                } ?: currentTt // If no course ID, there are no course notes to write.

                updateTimeTrial(ttWithRecords.copy(timeTrialHeader = ttWithRecords.timeTrialHeader.copy(status = TimeTrialStatus.FINISHED)))
                Timber.d("PBS calculation finished and TT status updated for TT ID: ${currentTt.timeTrialHeader.id}")
            } catch (e: Exception) {
                Timber.e(e, "Error during PBS calculation for TT ID: ${currentTt.timeTrialHeader.id}")
                showMessage("Error calculating Personal Bests: ${e.localizedMessage}")
            }
        }
    }

    val PRString = "PR!"
    val CRString = "CR!"

    fun writeRecordsToNotes(timeTrial: TimeTrial, courseResults: List<TimeTrialRider>): TimeTrial{

        val maleCr = courseResults.firstOrNull{it.gender == Gender.MALE}?.finishTime()
        val femaleCr = courseResults.firstOrNull{it.gender == Gender.FEMALE}?.finishTime()

        val maleWithCr = maleCr?.let {
            timeTrial.helper.results.filter {
                it.riderData.gender == Gender.MALE && (it.timeTrialData.finishTime()
                    ?: Long.MAX_VALUE) < maleCr
            }.minByOrNull { it.timeTrialData.finishTime() ?: Long.MAX_VALUE }
        }?.rider

        val femaleWithCr = femaleCr?.let {
            timeTrial.helper.results.filter {
                it.riderData.gender == Gender.FEMALE && (it.timeTrialData.finishTime()
                    ?: Long.MAX_VALUE) < femaleCr
            }.minByOrNull { it.timeTrialData.finishTime()  ?: Long.MAX_VALUE }
        }?.rider

        val listWithPrsCalculated = timeTrial.riderList.asSequence().map { timeTrialRider->
            timeTrialRider.riderData.id?.let {riderId->
                when (riderId) {
                    maleWithCr?.id -> {
                        val newTime = timeTrialRider.timeTrialData.finishTime()
                        val crString = newTime?.let { "$CRString (by ${ConverterUtils.toSecondMinuteHour(maleCr - newTime)})"}?:""
                        timeTrialRider.copy(timeTrialData = timeTrialRider.timeTrialData.copy(notes = crString))
                    }
                    femaleWithCr?.id -> {
                        val newTime = timeTrialRider.timeTrialData.finishTime()
                        val crString = newTime?.let { "$CRString (by ${ConverterUtils.toSecondMinuteHour(femaleCr - newTime)})"}?:""
                        timeTrialRider.copy(timeTrialData = timeTrialRider.timeTrialData.copy(notes = crString))
                    }
                    else -> {
                        courseResults.firstOrNull{it.finishTime() != null && it.riderId == riderId}?.let { existingResult->

                            val thisResultTime = timeTrialRider.timeTrialData.finishTime()
                            val existingResultTime = existingResult.finishTime()

                            if(thisResultTime != null && existingResultTime != null && existingResultTime > thisResultTime){
                                val prString = "$PRString (by ${ConverterUtils.toSecondMinuteHour(existingResultTime - thisResultTime)})"
                                timeTrialRider.copy(timeTrialData = timeTrialRider.timeTrialData.copy(notes = prString))
                            }else{
                                timeTrialRider
                            }
                        }
                    }
                }
            } ?:timeTrialRider
        }.toList()
        return timeTrial.updateRiderList(listWithPrsCalculated)
    }

    fun discardTt(){

        val ttToDiscard = timeTrial.value ?: run {
            Timber.w("discardTt called but timeTrial.value is null.")
            ttDeleted.postValue(Event(false)) // Indicate failure or no-op
            return
        }

        Timber.d("Attempting to discard TT with ID: ${ttToDiscard.timeTrialHeader.id}")

        // 1. Immediately update LiveData to reflect deletion in UI and prevent further UI actions on it.
        // This also helps the channel collector to potentially skip an update for this TT.
        timeTrial.value = null
        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("Performing delete operation for TT ID: ${ttToDiscard.timeTrialHeader.id}")
            timeTrialRepository.delete(ttToDiscard)
            Timber.d("TT ID: ${ttToDiscard.timeTrialHeader.id} deleted successfully from repository.")
            ttDeleted.postValue(Event(true))
        }
    }

    val ttDeleted: MutableLiveData<Event<Boolean>> = MutableLiveData()

    fun backToSetup(){
        timeTrial.value?.let {
            val headerCopy = it.timeTrialHeader.copy(status = TimeTrialStatus.SETTING_UP)
            val riderListCopy = it.riderList.map { it.copy(timeTrialData = it.timeTrialData.copy(splits = listOf(), finishCode = null)) }
            updateTimeTrial(it.copy(timeTrialHeader = headerCopy, riderList = riderListCopy))
        }
    }

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
                in 0L..5000 -> {
                    var x = millisToNextRider
                    if(x > 1000){
                        do{x /= 10} while (x > 9)
                    }else{
                        x = 0
                    }
                    "${nextStartRider.riderData.firstName} ${nextStartRider.riderData.lastName} - ${x+1}!"
                }
                in 5000..10000 ->
                    "$riderString starts in 10 seconds"
                in 10000.. 15000 ->
                    "$riderString starts in 15 seconds"
                in 15000.. 30000 ->
                    "$riderString starts in 30 seconds"
                in (ttIntervalMilis - 3000)..ttIntervalMilis ->
                {
                    if(prevIndex >= 0)
                    {
                        val prevRider = sparse.valueAt(prevIndex)
                        "(${prevRider.riderData.firstName} ${prevRider.riderData.lastName}) GO GO GO!!!"
                    }
                    else
                    {
                        "Next rider is $riderString"
                    }
                }
                else ->
                    "Next rider is $riderString"
            }
        } else {
            return "${tte.helper.finishedRiders.size} riders have finished, ${tte.helper.ridersOnCourse(millisSinceStart).size} riders on course"
        }

    }

    fun addLateRider(riderId: Long, number:Int?){
        timeTrial.value?.let { oldTt->
            viewModelScope.launch(Dispatchers.IO) {
                riderRepository.ridersFromIds(listOf(riderId)).firstOrNull()?.let { rider->
                    if(oldTt.riderList.any { it.riderId() == riderId }){
                        throw Exception("Rider already in TT")
                    }
                    val new = oldTt.addRiderWithNumber(rider, number)
                    val ttr = new.riderList.first { it.riderId() == riderId }
                    val millisSinceStart = Instant.now().toEpochMilli() - new.timeTrialHeader.startTimeMillis
                    val newer = if(new.helper.getRiderStartTime(ttr.timeTrialData) < millisSinceStart){
                        new.helper.moveRiderToBack(ttr.timeTrialData)
                    }else{
                        new
                    }
                    updateTimeTrial(newer)
                }
            }
        }
    }

    fun testFinishAll(){
        timeTrial.value?.let {tt->
            val now1 = Instant.now().toEpochMilli() - tt.timeTrialHeader.startTimeMillis
            val timeLeft = (tt.helper.sparseRiderStartTimes.keyAt(tt.helper.sparseRiderStartTimes.size - 1)) - now1
            var c = if(tt.helper.sparseRiderStartTimes.keyAt(tt.helper.sparseRiderStartTimes.size - 1) > now1){
                tt.copy(timeTrialHeader = tt.timeTrialHeader.copy(startTime = tt.timeTrialHeader.startTime?.minusSeconds(timeLeft/1000)))
            }else{
                tt
            }

            tt.riderList.forEach{ttr->
                val now = Instant.now().toEpochMilli() - tt.timeTrialHeader.startTimeMillis
                val helper = c.helper
                c = helper.assignRiderToEvent(ttr.timeTrialData, now).tt
            }
            updateTimeTrial(c)
        }
    }

    fun testFinishTt(){
        timeTrial.value?.let {
            updateTimeTrial(it.copy(timeTrialHeader = it.timeTrialHeader.copy(status  = TimeTrialStatus.FINISHED)))
        }
    }
}