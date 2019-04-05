package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.Rider

@Dao
interface RiderDao {

    @Insert
    fun insert(rider: Rider)

    @Update
    fun update(rider: Rider)

    @Delete
    fun delete(rider: Rider)

    @Query("DELETE FROM rider_table") fun deleteAll()

    @Query("SELECT * from rider_table ORDER BY firstName COLLATE NOCASE ASC") fun getAllRiders(): LiveData<List<Rider>>

    @Query("SELECT DISTINCT club from rider_table") fun getAllClubs(): LiveData<List<String>>

    @Query("SELECT * FROM rider_table WHERE Id = :riderId LIMIT 1") fun getRiderById(riderId: Long): LiveData<Rider>
}