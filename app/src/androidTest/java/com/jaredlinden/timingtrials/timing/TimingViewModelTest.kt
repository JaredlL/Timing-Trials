package com.jaredlinden.timingtrials.timing

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jaredlinden.timingtrials.data.FilledTimeTrialRider
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.TimeTrialRider
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.roomrepo.RoomRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.util.ConverterUtils
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class TimingViewModelTest {

    private val nowTime = OffsetDateTime.now()

    @Before
    fun setUp() {
        // Initialize ThreeTenBP
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        AndroidThreeTen.init(context)
    }

    // This rule swaps the background executor used by the Architecture Components with a
    // different one which executes each task synchronously.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun should_updateDisplayString_when_updateCalled() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(timingTimeTrial)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)
        val expectedMillis = 5000L
        val expectedString = ConverterUtils.toTenthsDisplayString(expectedMillis)

        // [act]
        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(nowTime.plus(expectedMillis, ChronoUnit.MILLIS).toInstant().toEpochMilli())

        // [assert]
        Assert.assertEquals(expectedString, underTest.timeString.getOrAwaitValue())
    }

    @Test
    fun should_notUpdateDisplayString_when_updateCalledWithSmallTimeDifference() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(timingTimeTrial)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)
        val milis1 = 1000L
        val milis2 = 1099L // Less than 100ms difference
        val expectedString = ConverterUtils.toTenthsDisplayString(milis1)

        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(nowTime.plus(milis1, ChronoUnit.MILLIS).toInstant().toEpochMilli())

        // Observe the first time string to ensure it is calculated
        val y = underTest.timeString.getOrAwaitValue()

        // [act]
        underTest.updateLoop(nowTime.plus(milis2, ChronoUnit.MILLIS).toInstant().toEpochMilli())

        // [assert]
        Assert.assertEquals(expectedString, underTest.timeString.getOrAwaitValue())
    }

    @Test
    fun should_updateDisplayString_when_updateCalledWithBiggerTimeDifference() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(timingTimeTrial)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)
        val milis1 = 1000L
        val milis2 = 1100L // More than 100ms difference
        val expectedString = ConverterUtils.toTenthsDisplayString(milis2)
        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(nowTime.plus(milis1, ChronoUnit.MILLIS).toInstant().toEpochMilli())

        // Observe the first time string to ensure it is calculated
        val y = underTest.timeString.getOrAwaitValue()

        // [act]
        underTest.updateLoop(nowTime.plus(milis2, ChronoUnit.MILLIS).toInstant().toEpochMilli())

        // [assert]
        Assert.assertEquals(expectedString, underTest.timeString.getOrAwaitValue())
    }

    @Test
    fun should_showTimeTrialStartsAtZero_when_moreThanOneMinuteBeforeFirstRider() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        val ttInProgress = baseTimeTrial.copy(
            timeTrialHeader = baseTimeTrial.timeTrialHeader.copy(status = TimeTrialStatus.IN_PROGRESS)
        )
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(ttInProgress)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)

        // More than 1 minute before the first rider starts (firstRiderStartOffset = 60s, interval = 60s)
        val timeBeforeStartMillis = ttInProgress.timeTrialHeader.startTime!!.toInstant().toEpochMilli() - 10000

        // [act]
        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(timeBeforeStartMillis)

        // [assert]
        Assert.assertEquals("TestTT starts at 0:00:00:0", underTest.statusString.getOrAwaitValue())
    }

    @Test
    fun should_showNextRiderStartsIn30Seconds_when_30SecondsBeforeNextRider() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        val ttInProgress = baseTimeTrial.copy(
            timeTrialHeader = baseTimeTrial.timeTrialHeader.copy(status = TimeTrialStatus.IN_PROGRESS)
        )
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(ttInProgress)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)

        // 30 seconds before the first rider starts
        val firstRiderStartTime = ttInProgress.timeTrialHeader.startTimeMillis + (ttInProgress.timeTrialHeader.firstRiderStartOffset * 1000L)
        val currentTimeMillis = firstRiderStartTime - 30000

        // [act]
        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(currentTimeMillis)

        // [assert]
        val expected = "(1) Rider Zero starts in 30 seconds"
        Assert.assertEquals(expected, underTest.statusString.getOrAwaitValue())
    }

    @Test
    fun should_showNextRiderStartsIn15Seconds_when_15SecondsBeforeNextRider() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        val ttInProgress = baseTimeTrial.copy(
            timeTrialHeader = baseTimeTrial.timeTrialHeader.copy(status = TimeTrialStatus.IN_PROGRESS)
        )
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(ttInProgress)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)

        // 15 seconds before the first rider starts
        val firstRiderStartTime = ttInProgress.timeTrialHeader.startTimeMillis + (ttInProgress.timeTrialHeader.firstRiderStartOffset * 1000L)
        val currentTimeMillis = firstRiderStartTime - 15000

        // [act]
        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(currentTimeMillis)

        // [assert]
        val expected = "(1) Rider Zero starts in 15 seconds"
        Assert.assertEquals(expected, underTest.statusString.getOrAwaitValue())
    }

    @Test
    fun should_showNextRiderStartsIn10Seconds_when_10SecondsBeforeNextRider() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        val ttInProgress = baseTimeTrial.copy(
            timeTrialHeader = baseTimeTrial.timeTrialHeader.copy(status = TimeTrialStatus.IN_PROGRESS)
        )
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(ttInProgress)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)

        // 10 seconds before the first rider starts
        val firstRiderStartTime = ttInProgress.timeTrialHeader.startTimeMillis + (ttInProgress.timeTrialHeader.firstRiderStartOffset * 1000L)
        val currentTimeMillis = firstRiderStartTime - 10000

        // [act]
        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(currentTimeMillis)

        // [assert]
        val expected = "(1) Rider Zero starts in 10 seconds"
        Assert.assertEquals(expected, underTest.statusString.getOrAwaitValue())
    }

    @Test
    fun should_showRiderNameWithCountdown_when_5SecondsBeforeNextRider() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        val ttInProgress = baseTimeTrial.copy(
            timeTrialHeader = baseTimeTrial.timeTrialHeader.copy(status = TimeTrialStatus.IN_PROGRESS)
        )
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(ttInProgress)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)

        // 3 seconds before the first rider starts
        val firstRiderStartTime = ttInProgress.timeTrialHeader.startTimeMillis + (ttInProgress.timeTrialHeader.firstRiderStartOffset * 1000L)
        val currentTimeMillis = firstRiderStartTime - 3000

        // [act]
        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(currentTimeMillis)

        // [assert]
        // Should show "Rider Zero - 4!" (millisToNextRider/1000 + 1)
        val expected = "Rider Zero - 4!"
        Assert.assertEquals(expected, underTest.statusString.getOrAwaitValue())
    }

    @Test
    fun should_showFinishedRidersAndOnCourse_when_allRidersHaveStarted() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        val ttInProgress = baseTimeTrial.copy(
            timeTrialHeader = baseTimeTrial.timeTrialHeader.copy(status = TimeTrialStatus.IN_PROGRESS)
        )
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(ttInProgress)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)

        // Time after all riders would have started (4 riders * 60s interval + 60s first offset = 300s = 5 minutes)
        val allRidersStartedTime = ttInProgress.timeTrialHeader.startTimeMillis + (ttInProgress.timeTrialHeader.firstRiderStartOffset * 1000L) + (4 * ttInProgress.timeTrialHeader.interval * 1000L)
        val currentTimeMillis = allRidersStartedTime + 30000 // 30 seconds after all riders started

        // [act]
        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(currentTimeMillis)

        // [assert]
        val expected = "0 riders have finished, 4 riders on course"
        Assert.assertEquals(expected, underTest.statusString.getOrAwaitValue())
    }

    @Test
    fun should_addTimestamp_when_onRiderPassedCalled() {

        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        val ttInProgress = baseTimeTrial.copy(
            timeTrialHeader = baseTimeTrial.timeTrialHeader.copy(
                status = TimeTrialStatus.IN_PROGRESS,
                startTime = OffsetDateTime.now().minusMinutes(3)
            ),
        )
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(ttInProgress)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)

        underTest.timeTrial.getOrAwaitValue()
        
        // Set current time to after the first rider has started
        val firstRiderStartTime = ttInProgress.timeTrialHeader.startTimeMillis + (ttInProgress.timeTrialHeader.firstRiderStartOffset * 1000L)
        val currentTimeMillis = firstRiderStartTime + 30000 // 30 seconds after first rider started
        underTest.updateLoop(currentTimeMillis)

        val initialTimestampCount = underTest.timeTrial.value?.timeTrialHeader?.timeStamps?.size ?: 0

        // [act]
        underTest.onRiderPassed()

        // [assert]
        val newTimestampCount = underTest.timeTrial.value?.timeTrialHeader?.timeStamps?.size ?: 0
        Assert.assertEquals(initialTimestampCount + 1, newTimestampCount)
    }

    @Test
    fun should_showFirstRiderNotStarted_when_onRiderPassedCalledTooEarly() {
        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        val ttInProgress = baseTimeTrial.copy(
            timeTrialHeader = baseTimeTrial.timeTrialHeader.copy(status = TimeTrialStatus.IN_PROGRESS)
        )
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(ttInProgress)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)

        underTest.timeTrial.getOrAwaitValue()
        
        // Set current time to before the first rider has started
        val firstRiderStartTime = ttInProgress.timeTrialHeader.startTimeMillis + (ttInProgress.timeTrialHeader.firstRiderStartOffset * 1000L)
        val currentTimeMillis = firstRiderStartTime - 10000 // 10 seconds before first rider starts
        underTest.updateLoop(currentTimeMillis)

        // [act]
        underTest.onRiderPassed()

        // [assert]
        val message = underTest.messageData.getOrAwaitValue()
        Assert.assertEquals("First rider has not started yet", message.peekContent())
    }

    fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(o: T) {
                data = o
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }

        this.observeForever(observer)

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }

    private val baseTimeTrialHeader = TimeTrialHeader(
        ttName = "TestTT",
        courseId = 1L,
        laps = 1,
        interval = 60, // 60 second intervals
        startTime = nowTime,
        firstRiderStartOffset = 60, // First rider starts 60 seconds after TT start
        status = TimeTrialStatus.IN_PROGRESS
    )

    private val baseRiders = listOf(
        FilledTimeTrialRider(
            TimeTrialRider(
                riderId = 1L, timeTrialId = 1L, courseId = 1L, index = 0,
            ),
            Rider(firstName = "Rider", lastName = "Zero")
        ),
        FilledTimeTrialRider(
            TimeTrialRider(riderId = 2L, timeTrialId = 1L, courseId = 1L, index = 1),
            Rider(firstName = "Rider", lastName = "One")
        ),
        FilledTimeTrialRider(
            TimeTrialRider(riderId = 3L, timeTrialId = 1L, courseId = 1L, index = 2),
            Rider(firstName = "Rider", lastName = "Two")
        ),
        FilledTimeTrialRider(
            TimeTrialRider(riderId = 4L, timeTrialId = 1L, courseId = 1L, index = 3),
            Rider(firstName = "Rider", lastName = "Three")
        )
    )

    private val baseTimeTrial = TimeTrial(
        timeTrialHeader = baseTimeTrialHeader,
        riderList = baseRiders
    )

    private val timingTimeTrial = baseTimeTrial.copy(
        timeTrialHeader = baseTimeTrialHeader.copy(startTime = nowTime)
    )
}