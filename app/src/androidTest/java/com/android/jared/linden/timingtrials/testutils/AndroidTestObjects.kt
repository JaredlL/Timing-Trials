package com.android.jared.linden.timingtrials.testutils

import androidx.core.util.size
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.source.TimingTrialsDatabase
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit

object AndroidTestObjects{
    fun createTestTimeTrial(): TimeTrial{
        val new = TimeTrial(createTestHeader(), listOf(), listOf()).helper.addRidersAsTimeTrialRiders(createRiderList())
        return createMockEvents(new)
    }

    fun createTestHeader(): TimeTrialHeader {
        return TimeTrialHeader.createBlank()
                .copy(ttName = "Testing Timetrial",
                        startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.MINUTES).minusSeconds(300), ZoneId.systemDefault()),
                        laps = 1,
                        firstRiderStartOffset = 60,
                        course = createTestCourse(),
                        interval = 30,
                        id = 20913)

    }

    fun createMockEvents(timeTrial: TimeTrial):TimeTrial{

        val mutEvents = mutableListOf<TimeTrialEvent>()
        val startTimes = timeTrial.helper.sparseRiderStartTimes
        var i = 0
        while (i < startTimes.size) {
            val riderId = startTimes.valueAt(i).rider.id
            val time = startTimes.keyAt(i)
            val startEvent = TimeTrialEvent(20913, riderId, time, EventType.RIDER_STARTED)
            mutEvents.add(startEvent)
            val finEvent = TimeTrialEvent(20913, riderId, time + 10000L, EventType.RIDER_PASSED)
            mutEvents.add(finEvent)

            i++
        }
//        for (ttRider in startTimes){
//            val startEvent = TimeTrialEvent(20913, ttRider.value.id, ttRider.key, EventType.RIDER_STARTED)
//            mutEvents.add(startEvent)
//            val finEvent = TimeTrialEvent(20913, ttRider.value.id, ttRider.key + 100L, EventType.RIDER_PASSED)
//            mutEvents.add(finEvent)
//        }
        return timeTrial.copy(eventList = mutEvents)
    }

    fun createTestCourse():Course{
        return Course.createBlank().copy(courseName = "Test Course")
    }

    fun createRiderList():List<Rider>{
        val mList = mutableListOf<Rider>()
        mList.add(TimingTrialsDatabase.createBaseRider("Jared", "Linden", "RDFCC", 1990, Gender.MALE))
        mList.add(TimingTrialsDatabase.createBaseRider("Adam", "Taylor", "RDFCC", 1976, Gender.MALE))
        mList.add(TimingTrialsDatabase.createBaseRider("John", "Linden", "RDFCC", 1955, Gender.MALE))
        mList.add(TimingTrialsDatabase.createBaseRider("Lauren", "Johnston", "Avid", 1993, Gender.FEMALE))
        mList.add(TimingTrialsDatabase.createBaseRider("Steve", "Beal", "VeloVitesse", 1976, Gender.MALE))
        mList.add(TimingTrialsDatabase.createBaseRider("Earl", "Smith", "RDFCC", 1976, Gender.MALE))
        mList.add(TimingTrialsDatabase.createBaseRider("Jo", "Jago", "Performance Cycles", 1979, Gender.FEMALE))
        mList.add(TimingTrialsDatabase.createBaseRider("Dave", "Pearce", "RDFCC", 1977, Gender.MALE))
        mList.add(TimingTrialsDatabase.createBaseRider("Craig ", "Buffry", "RDFCC", 1992, Gender.MALE))
        mList.add(TimingTrialsDatabase.createBaseRider("Collin", "Parry", "RDFCC", 1975, Gender.MALE))
        mList.add(TimingTrialsDatabase.createBaseRider("Rob", "Borek", "Forever Pedalling", 1992, Gender.MALE))
        return mList.mapIndexed { index, rider ->  rider.copy(id = index * 1L) }
    }
}

