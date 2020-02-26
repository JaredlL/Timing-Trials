package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.RiderIdStartTime
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrialRiderResult
import org.threeten.bp.*


@Dao
interface TimeTrialRiderDao {

    @Insert
    fun insert(timeTrialRider: TimeTrialRider): Long

    @Insert
    fun insertMultiple(timeTrialRiderList: TimeTrialRider)

    @Update
    fun update(timeTrialRider: TimeTrialRider)

    @Update
    fun updateMultiple(timeTrialRiderList: List<TimeTrialRider>)

    @Delete
    fun delete(timeTrialRider: TimeTrialRider)

    @Transaction @Query("SELECT * FROM timetrial_rider_table WHERE timeTrialId = :timeTrialId ORDER BY `index`")
    fun getTimeTrialRiders(timeTrialId: Long): LiveData<List<FilledTimeTrialRider>>

    @Transaction @Query ("SELECT * FROM timetrial_rider_table WHERE riderId = :riderId AND finishTime IS NOT NULL ORDER BY `finishTime`")
    fun getRiderResults(riderId: Long): LiveData<List<TimeTrialRiderResult>>

    @Transaction @Query ("SELECT * FROM timetrial_rider_table WHERE courseId = :courseId AND finishTime IS NOT NULL ORDER BY `finishTime`")
    fun getCourseResults(courseId: Long): LiveData<List<TimeTrialRiderResult>>

    @Query("SELECT * FROM timetrial_rider_table WHERE courseId == :courseId AND timeTrialId == :timeTrialId AND riderId == :riderId")
    fun getByRiderCourseTimeTrialIds(riderId: Long, courseId:Long, timeTrialId: Long): List<TimeTrialRider>

    @Query("SELECT riderId, timetrial_table.startTime AS startTime FROM timetrial_rider_table INNER JOIN timetrial_table ON timetrial_table.id = timeTrialId WHERE timetrial_table.status = 2 ORDER BY timetrial_table.startTime")
    fun getRiderIdTimeTrialStartTime(): LiveData<List<RiderIdStartTime>>


}