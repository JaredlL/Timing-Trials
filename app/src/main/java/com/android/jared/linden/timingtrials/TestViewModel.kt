package com.android.jared.linden.timingtrials

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.roomrepo.ICourseRepository
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.android.jared.linden.timingtrials.data.source.TimingTrialsDatabase
import com.android.jared.linden.timingtrials.util.Event
import kotlinx.coroutines.*
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

class TestViewModel@Inject constructor(
        val timeTrialRepository: ITimeTrialRepository,
        val riderRepository: IRiderRepository,
        val courseRepository: ICourseRepository,
        val timingTrialsDatabase: TimingTrialsDatabase
) : ViewModel(){



    private val inserting = AtomicBoolean()

     val insertedTest = MutableLiveData(false)

    fun insertionActedUpon(){
        insertedTest.value = false
    }

//    fun testSetup(timeTrial: TimeTrial){
//
//            if(!inserting.get() && timeTrial.timeTrialHeader.ttName != "Test Setup Timetrial"){
//                inserting.set(true)
//                viewModelScope.launch(Dispatchers.IO) {
//                    val rList = riderRepository.allRidersLightSuspend()
//                    val courses = courseRepository.getAllCoursesSuspend()
//                    val newTt = timeTrial.copy(timeTrialHeader = timeTrial.timeTrialHeader
//                            .copy(ttName = "Test Setup Timetrial",
//                                    startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
//                                    firstRiderStartOffset = 0,
//                                    interval = 2,
//                                    course = courses[3],
//                                    laps = 2,
//                                    status = TimeTrialStatus.SETTING_UP))
//                    val copy = newTt.helper.addRidersAsTimeTrialRiders(rList)
//                    timeTrialRepository.update(copy)
//                    inserting.set(false)
//                    insertedTest.postValue(true)
//                }
//            }
//    }

    fun testTiming(timeTrial: TimeTrial){
        if(!inserting.get()  && timeTrial.timeTrialHeader.ttName != "Test Timing TimeTrial"){
            inserting.set(true)
            viewModelScope.launch(Dispatchers.IO) {
                val rList = riderRepository.allRidersLightSuspend().take(6)
                val courses = courseRepository.getAllCoursesSuspend()
                val cop1 = timeTrial.copy(timeTrialHeader = timeTrial.timeTrialHeader
                        .copy(ttName = "Test Timing TimeTrial",
                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(5), ZoneId.systemDefault()),
                                firstRiderStartOffset = 0,
                                interval = 2,
                                courseId = courses[3].id,
                                laps = 2,
                                status = TimeTrialStatus.IN_PROGRESS))
                val newTt = cop1.addRiders(rList)
                timeTrialRepository.updateFull(newTt)
                inserting.set(false)
                insertedTest.postValue(true)
            }
        }
    }







    fun insertFinishedTt(){

        viewModelScope.launch(Dispatchers.IO) {

            val exist = timeTrialRepository.getTimeTrialByName("Test Result TT 2")
            if(exist == null){

                val rList = riderRepository.allRidersLightSuspend()
                val courses = courseRepository.getAllCoursesSuspend()

                val _mTimeTrial =TimeTrial.createBlank().copy(timeTrialHeader = TimeTrialHeader.createBlank()
                        .copy(ttName = "Test Result TT 2",
                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
                                firstRiderStartOffset = 0,
                                interval = 2,
                                courseId = courses[2].id,
                                laps = 4,
                                status = TimeTrialStatus.FINISHED))


                val rListWithIds = _mTimeTrial.addRiders(rList).riderList.mapIndexed { index, filledTimeTrialRider -> filledTimeTrialRider.copy(timeTrialData = filledTimeTrialRider.timeTrialData.copy(id = index.toLong())) }

                val copy = _mTimeTrial.copy(riderList = rListWithIds)
                var current = copy
                for(rider in copy.riderList){
                    val startTime = copy.helper.getRiderStartTime(rider.timeTrialData)
                    for(i in (1..copy.timeTrialHeader.laps)){
                        val timeStamp = startTime + i * 2000L + rider.timeTrialData.index * 200L
                        current = current.updateHeader(current.timeTrialHeader.copy(timeStamps = listOf(timeStamp)))
                        current = current.helper.assignRiderToEvent(rider.timeTrialData, current.timeTrialHeader.timeStamps.last()).tt
                    }
                }

                val rListWithoutIds = current.riderList.map { it.copy(timeTrialData = it.timeTrialData.copy(id = null)) }

               val id = timeTrialRepository.insert(current.copy(riderList = rListWithoutIds))
                testInsertedEvent.postValue(Event(id))
            }else{
                testInsertedEvent.postValue(Event(exist.timeTrialHeader.id))
            }
        }
    }

    fun updateHeader(timeTrial: TimeTrial, newHeader: TimeTrialHeader):TimeTrial{
        return timeTrial.copy(timeTrialHeader = newHeader)
    }

//    fun insertFinishedTt3(){
//
//        viewModelScope.launch(Dispatchers.IO) {
//
//            val exist = timeTrialRepository.getTimeTrialByName("Test Result TT 2")
//            if(exist == null){
//
//                val rList = riderRepository.allRidersLightSuspend()
//                val courses = courseRepository.getAllCoursesSuspend()
//
//                val _mTimeTrial =TimeTrial.createBlank().copy(timeTrialHeader = TimeTrialHeader.createBlank()
//                        .copy(ttName = "Result TT 3",
//                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
//                                firstRiderStartOffset = 0,
//                                interval = 2,
//                                course = courses[2],
//                                laps = 1,
//                                status = TimeTrialStatus.FINISHED))
//
//
//                val newTt = _mTimeTrial.helper.addRidersAsTimeTrialRiders(rList.take(4))
//                val withEvents = addFakeEvents(newTt)
//                val id = timeTrialRepository.insert(withEvents)
//                testInsertedEvent.postValue(Event(id))
//            }else{
//                testInsertedEvent.postValue(Event(exist.timeTrialHeader.id))
//            }
//
//
//        }
//    }


    val testInsertedEvent: MutableLiveData<Event<Long?>> = MutableLiveData()
    fun insertFinishedTt2(){
        viewModelScope.launch(Dispatchers.IO) {

            val exist = timeTrialRepository.getTimeTrialByName("Test Result TT 1")
            if(exist == null) {
                val rList = riderRepository.allRidersLightSuspend()
                val courses = courseRepository.getAllCoursesSuspend()

                val _mTimeTrial = TimeTrial.createBlank().copy(timeTrialHeader = TimeTrialHeader.createBlank()
                        .copy(ttName = "Test Result TT 1",
                                startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(15), ZoneId.systemDefault()),
                                firstRiderStartOffset = 0,
                                interval = 2,
                                courseId = courses[3].id,
                                laps = 1,
                                status = TimeTrialStatus.FINISHED))


                val rListWithIds = _mTimeTrial.addRiders(rList).riderList.mapIndexed { index, filledTimeTrialRider -> filledTimeTrialRider.copy(timeTrialData = filledTimeTrialRider.timeTrialData.copy(id = index.toLong())) }

                val copy = _mTimeTrial.copy(riderList = rListWithIds)
                var current = copy
                for(rider in copy.riderList){
                    val startTime = copy.helper.getRiderStartTime(rider.timeTrialData)
                    for(i in (1..copy.timeTrialHeader.laps)){
                        val timeStamp = startTime + i * 2000L + rider.timeTrialData.index * 200L
                        current = current.updateHeader(current.timeTrialHeader.copy(timeStamps = listOf(timeStamp)))
                        current = current.helper.assignRiderToEvent(rider.timeTrialData, current.timeTrialHeader.timeStamps.last()).tt
                    }
                }

                val rListWithoutIds = current.riderList.map { it.copy(timeTrialData = it.timeTrialData.copy(id = null)) }

                val id = timeTrialRepository.insert(current.copy(riderList = rListWithoutIds))
                testInsertedEvent.postValue(Event(id))
            }else{
                testInsertedEvent.postValue(Event(exist.timeTrialHeader.id))
            }
        }
    }



//    fun  addFakeEvents2(timeTrial: TimeTrial): TimeTrial{
//
//        val events = mutableListOf<RiderPassedEvent>()
//        timeTrial.helper.riderStartTimes.forEach {
//            for (i in 1..timeTrial.timeTrialHeader.laps) {
//                events.add(RiderPassedEvent(timeTrial.timeTrialHeader.id
//                        ?: 0, it.value.rider.id, it.key + 500 + i * 3000))
//            }
//        }
//
//        val newEvents = timeTrial.helper.riderStartTimes.asSequence().mapIndexed { index, entry ->
//            RiderPassedEvent(timeTrial.timeTrialHeader.id ?: 0, entry.value.rider.id, entry.key + 500 + index * 3000)
//        }
//        return timeTrial.copy(eventList = newEvents.toList())
//    }
//
//    fun addFakeEvents(timeTrial: TimeTrial): TimeTrial{
//        val events = mutableListOf<RiderPassedEvent>()
//        timeTrial.helper.riderStartTimes.forEach {
//            //events.add(RiderPassedEvent(_mTimeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key))
//
//
//            var prev = it.key
//            for(i in 1..timeTrial.timeTrialHeader.laps){
//                prev += prev + Random.nextLong(5000, 10000)
//                events.add(RiderPassedEvent(timeTrial.timeTrialHeader.id?:0, it.value.rider.id, prev))
//            }
//
//
////                var i = 0
////                while(i < setupTimeTrial.timeTrialHeader.laps){
////                    events.add(RiderPassedEvent(setupTimeTrial.timeTrialHeader.id?:0, it.value.rider.id, it.key + 500 + i * 333, EventType.RIDER_PASSED))
////                    i++
////                }
//
//        }
//        return timeTrial.copy(eventList = events)
//    }

    // endregion

}