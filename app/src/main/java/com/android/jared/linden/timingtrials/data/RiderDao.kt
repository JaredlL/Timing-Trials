package com.android.jared.linden.timingtrials.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RiderDao {

    @Insert
    fun insert(rider: Rider)

    @Update
    fun updateRider(rider:Rider)

    @Query("DELETE FROM rider_table") fun deleteAll()

    @Query("SELECT * from rider_table ORDER BY firstName ASC") fun getAllRiders(): LiveData<List<Rider>>

    @Query("SELECT * FROM rider_table WHERE Id = :riderId LIMIT 1") fun getRiderById(riderId: Long): Rider
}