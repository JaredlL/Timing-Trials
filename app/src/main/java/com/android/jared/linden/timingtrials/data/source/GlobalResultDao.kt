package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.GlobalResult

@Dao
interface GlobalResultDao {
    @Insert
    fun insert(globalResult: GlobalResult)

    @Update
    fun update(globalResult: GlobalResult)

    @Update
    fun updateList(globalResult: List<GlobalResult>)

    @Delete
    fun delete(globalResult: GlobalResult)

    @Query("SELECT * FROM globalresult_table ORDER BY millisTime") fun getAllResults(): LiveData<List<GlobalResult>>

    @Query("SELECT * FROM globalresult_table WHERE riderId = :riderId")fun getResultsForRiderID(riderId:Long): LiveData<List<GlobalResult>>
}