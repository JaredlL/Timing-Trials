package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.TimeTrial

@Dao
interface TimeTrialDao {
    @Insert
    fun insert(timeTrial: TimeTrial)

    @Update
    fun update(timeTrial: TimeTrial)

    @Delete
    fun delete(timeTrial: TimeTrial)

    @Query("DELETE FROM timetrial_table") fun deleteAll()

    @Query("SELECT * from timetrial_table ORDER BY startTime ASC") fun gatAllTimeTrials(): LiveData<List<TimeTrial>>

    @Query("SELECT * FROM timetrial_table WHERE Id = :ttId LIMIT 1") fun getTimeTrialById(ttId: Long): LiveData<TimeTrial>
}