package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrialRiderResult


@Dao
interface TimeTrialRiderDao {

    @Insert
    fun insert(timeTrialRider: TimeTrialRider)

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



}