package com.android.jared.linden.timingtrials.testutils

import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.source.TimingTrialsDatabase
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit

fun createTestTimeTrial(): TimeTrial {
    val header = TimeTrialHeader.createBlank()
                .copy(ttName = "Testing Timetrial",
                        startTime = OffsetDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.MINUTES).minusSeconds(300), ZoneId.systemDefault()),
                        laps = 1,
                        course = createTestCourse(),
                        interval = 30,
                        id = 20913)

    val timeTrial1 = TimeTrial.createBlank().copy(timeTrialHeader = header)
    val timeTrial = timeTrial1.helper.addRidersAsTimeTrialRiders(createRiderList())


}

fun createMockEvents(timeTrial: TimeTrial):List<TimeTrialEvent>{

    val mutEvents = mutableListOf<TimeTrialEvent>()
    val startTimes = timeTrial.helper.sparseRiderStartTimes
    for (ttRider in timeTrial.riderList){

    }
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
    return mList.mapIndexed { index, rider ->  rider.copy(id = index * 13L) }
}