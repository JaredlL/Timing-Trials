package com.jaredlinden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.jaredlinden.timingtrials.data.FilledTimeTrialRider
import com.jaredlinden.timingtrials.data.RiderIdStartTime
import com.jaredlinden.timingtrials.data.TimeTrialRider
import com.jaredlinden.timingtrials.data.TimeTrialRiderResult


@Dao
interface TimeTrialRiderDao {

    @Insert
    fun insert(timeTrialRider: TimeTrialRider): Long

    @Insert
    fun insertMultiple(timeTrialRiderList: TimeTrialRider)

    @Update
    suspend fun update(timeTrialRider: TimeTrialRider)

    @Update
    fun updateMultiple(timeTrialRiderList: List<TimeTrialRider>)

    @Delete
    fun delete(timeTrialRider: TimeTrialRider)

    @Transaction @Query ("SELECT * FROM timetrial_rider_table WHERE id = :resultId")
    fun getResultById(resultId: Long): LiveData<TimeTrialRiderResult?>

    @Transaction @Query("SELECT * FROM timetrial_rider_table WHERE timeTrialId = :timeTrialId ORDER BY `index`")
    fun getTimeTrialRiders(timeTrialId: Long): LiveData<List<FilledTimeTrialRider>>

    @Transaction @Query("SELECT * FROM timetrial_rider_table WHERE timeTrialId = :timeTrialId ORDER BY `index`")
    suspend fun getTimeTrialRidersSuspend(timeTrialId: Long): List<FilledTimeTrialRider>

    @Transaction @Query ("SELECT * FROM timetrial_rider_table WHERE riderId = :riderId AND finishCode IS NOT NULL ORDER BY finishCode")
    fun getRiderResults(riderId: Long): LiveData<List<TimeTrialRiderResult>>

    @Transaction @Query ("SELECT * FROM timetrial_rider_table WHERE courseId = :courseId AND finishCode IS NOT NULL ORDER BY finishCode")
    fun getCourseResults(courseId: Long): LiveData<List<TimeTrialRiderResult>>

    @Transaction @Query ("SELECT * FROM timetrial_rider_table WHERE courseId = :courseId AND finishCode IS NOT NULL ORDER BY finishCode")
    fun getCourseResultsSuspend(courseId: Long): List<TimeTrialRider>

    @Transaction @Query("SELECT * FROM timetrial_rider_table WHERE timeTrialId == :timeTrialId AND riderId == :riderId")
    fun getByRiderTimeTrialIds(riderId: Long, timeTrialId: Long): List<TimeTrialRider>

    @Transaction @Query("SELECT * FROM timetrial_rider_table WHERE timetrial_rider_table.finishCode > 0 ORDER BY timetrial_rider_table.finishCode")
    fun getAllResults(): LiveData<List<TimeTrialRiderResult>>

    @Transaction @Query("SELECT riderId, timetrial_table.startTime AS startTime FROM timetrial_rider_table INNER JOIN timetrial_table ON timetrial_table.id = timeTrialId WHERE timetrial_table.status = 2 ORDER BY timetrial_table.startTime")
    fun getRiderIdTimeTrialStartTime(): LiveData<List<RiderIdStartTime>>

    @Transaction @Query("SELECT * FROM timetrial_rider_table WHERE timetrial_rider_table.finishCode > 0 ORDER BY timetrial_rider_table.finishCode")
    fun getAllResultsSuspend(): List<TimeTrialRiderResult>

}