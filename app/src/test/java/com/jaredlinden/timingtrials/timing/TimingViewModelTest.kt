package com.jaredlinden.timingtrials.timing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.roomrepo.RoomRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.util.ConverterUtils
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(RobolectricTestRunner::class)
class TimingViewModelTest {

    private val nowTime = OffsetDateTime.now()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun should_updateDisplayString_when_updateCalled(){
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
        Assert.assertEquals(expectedString,  underTest.timeString.getOrAwaitValue())
    }

    @Test
    fun should_notUpdateDisplayString_when_updateCalledWithSmallTimeDifference(){

        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(timingTimeTrial)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)
        val milis1 = 1000L
        val milis2 = 1099L
        val expectedString = ConverterUtils.toTenthsDisplayString(milis1)

        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(nowTime.plus(milis1, ChronoUnit.MILLIS).toInstant().toEpochMilli())

        // Observe the first time string to ensure it is calculated
        val y = underTest.timeString.getOrAwaitValue()

        // [act]
        underTest.updateLoop(nowTime.plus(milis2, ChronoUnit.MILLIS).toInstant().toEpochMilli())

        // [assert]
        Assert.assertEquals(expectedString,  underTest.timeString.getOrAwaitValue())
    }

    @Test
    fun should_updateDisplayString_when_updateCalledWithBiggerTimeDifference(){

        // [arrange]
        val timeTrialRepository = mockk<ITimeTrialRepository>()
        every { timeTrialRepository.getTimingTimeTrial() } returns MutableLiveData(timingTimeTrial)
        val resultRepository = mockk<TimeTrialRiderRepository>()
        val riderRepository = mockk<RoomRiderRepository>()
        val underTest = TimingViewModel(timeTrialRepository, resultRepository, riderRepository)
        val milis1 = 1000L
        val milis2 = 1100L
        val expectedString = ConverterUtils.toTenthsDisplayString(milis2)
        underTest.timeTrial.getOrAwaitValue()
        underTest.updateLoop(nowTime.plus(milis1, ChronoUnit.MILLIS).toInstant().toEpochMilli())

        // Observe the first time string to ensure it is calculated
        val y = underTest.timeString.getOrAwaitValue()

        // [act]
        underTest.updateLoop(nowTime.plus(milis2, ChronoUnit.MILLIS).toInstant().toEpochMilli())

        // [assert]
        Assert.assertEquals(expectedString,  underTest.timeString.getOrAwaitValue())
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

    private val timingTimeTrial = TimeTrial(
        TimeTrialHeader(
            "TestTT",
            null,
        1,
            60,
            nowTime,
            60,
            TimeTrialStatus.IN_PROGRESS
        )
    )
}