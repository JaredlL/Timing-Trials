package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.FilledResult
import com.android.jared.linden.timingtrials.data.GlobalResult

@Dao
interface GlobalResultDao {
    @Insert
    fun insert(globalResult: GlobalResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(results: List<GlobalResult>)

    @Update
    fun update(globalResult: GlobalResult)

    @Update
    fun updateMultiple(globalResult: List<GlobalResult>)

    @Delete
    fun delete(globalResult: GlobalResult)

    @Query("SELECT * FROM globalresult_table ORDER BY resultTime")
    fun getAllResults(): LiveData<List<GlobalResult>>

    @Transaction
    @Query("SELECT * FROM globalresult_table WHERE riderId = :riderId ORDER BY resultTime")
    fun getResultsForRider(riderId: Long): LiveData<List<FilledResult>>

    @Transaction
    @Query("SELECT * FROM globalresult_table WHERE courseId = :courseId ORDER BY resultTime")
    fun getResultsForCourseID(courseId: Long): LiveData<List<FilledResult>>

    @Query("SELECT * FROM globalresult_table WHERE timeTrialId = :timeTrialId")
    fun getResultsForTimeTrialID(timeTrialId: Long): LiveData<List<GlobalResult>>

    @Query("SELECT * FROM globalresult_table WHERE timeTrialId = :timeTrialId")
    suspend fun getResultsForTimeTrialIDSuspend(timeTrialId: Long): List<GlobalResult>

    @Query("SELECT * FROM globalresult_table WHERE timeTrialId = :timeTrialId LIMIT 1")
    fun getAnyResultForTimeTrial(timeTrialId: Long): LiveData<GlobalResult>
}


