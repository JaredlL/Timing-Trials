package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.Rider

@Dao
interface RiderDao {

    @Insert
    fun insert(rider: Rider):Long

    @Update
    fun update(rider: Rider)

    @Update
    fun updateList(riders: List<Rider>)

    @Delete
    fun delete(rider: Rider)

    @Query("DELETE FROM rider_table") fun deleteAll()

    @Query("SELECT * from rider_table ORDER BY firstName COLLATE NOCASE ASC") fun getAllRiders(): LiveData<List<Rider>>

    @Query("SELECT *  from rider_table ORDER BY firstName COLLATE NOCASE ASC") fun getAllRidersLight(): LiveData<List<Rider>>

    @Query  ("SELECT *  from rider_table ORDER BY firstName COLLATE NOCASE ASC") fun getAllRidersLightSuspend(): List<Rider>

    @Query("SELECT * FROM rider_table WHERE firstName == :firstName AND lastName == :lastName") fun ridersFromFirstLastName(firstName: String, lastName: String): List<Rider>

    @Query("SELECT DISTINCT club from rider_table") fun getAllClubs(): LiveData<List<String>>

    @Query("SELECT * FROM rider_table WHERE Id = :riderId LIMIT 1")
    fun getRiderById(riderId: Long): LiveData<Rider>

    @Query("SELECT * FROM rider_table WHERE Id IN (:ids)") fun getRidersByIds(ids: List<Long>): List<Rider>
}