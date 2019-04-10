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

    @Query("SELECT * from timetrial_table ORDER BY startTime ASC") fun getAllTimeTrials(): LiveData<List<TimeTrial>>

    @Query("SELECT * FROM timetrial_table WHERE Id = :ttId LIMIT 1") fun getTimeTrialById(ttId: Long): LiveData<TimeTrial>

    @Query("SELECT * FROM timetrial_table WHERE ttName = :timeTrialName LIMIT 1") fun getTimeTrialByName(timeTrialName: String): TimeTrial?


    //SQLite does not have a boolean data type. Room maps it to an INTEGER column, mapping true to 1 and false to 0.
    @Query("SELECT * FROM timetrial_table WHERE isSetup = 0 LIMIT 1") fun getSetupTimeTrial(): LiveData<TimeTrial>
}