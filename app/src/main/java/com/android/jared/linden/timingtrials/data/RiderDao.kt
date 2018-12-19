package com.android.jared.linden.timingtrials.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RiderDao {

    @Insert
    fun insert(rider: Rider)

    @Query("DELETE FROM rider_table") fun deleteAll()

    @Query("SELECT * from rider_table ORDER BY firstName ASC") fun getAllRiders(): LiveData<List<Rider>>
}