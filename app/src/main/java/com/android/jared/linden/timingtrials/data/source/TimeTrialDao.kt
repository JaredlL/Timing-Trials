package com.android.jared.linden.timingtrials.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.room.*
import com.android.jared.linden.timingtrials.data.*
import kotlinx.coroutines.Dispatchers

@Dao
abstract class TimeTrialDao(db: RoomDatabase) {

    init{
        System.out.println("JAREDMSG -> Init DAO ${db.javaClass.canonicalName} ")
    }

    @Insert
    abstract fun insert(timeTrialHeader: TimeTrialHeader): Long

    @Update
    abstract fun update(timeTrialHeader: TimeTrialHeader)

    @Delete
    abstract fun delete(timeTrialHeader: TimeTrialHeader)

    @Transaction
    open suspend fun insertFull(timeTrial: TimeTrial): Long
    {
        val id = insert(timeTrial.timeTrialHeader)
        System.out.println("JAREDMSG -> Insert New TT $id + ${timeTrial.timeTrialHeader.ttName} FROM TRANSACTON, ${timeTrial.riderList.count()} riders into DB")

        val newRiderList = timeTrial.riderList.map { it.copy(timeTrialId = id)}
        val newEvents = timeTrial.eventList.map { it.copy(timeTrialId = id)}
        _insertAllEvents(newEvents)
        _insertAllTimeTrialRiders(newRiderList)

        printStackTrace()
        return id
    }


    fun getNonFinishedTt() : LiveData<TimeTrial?>{
        return getNonFinishedTtLive()
//        return liveData(Dispatchers.IO) {
//            val nonFinishedTt = _getNonFinishedTimeTrial()
//            if(nonFinishedTt == null){
//                val id = insert(TimeTrialHeader.createBlank())
//                System.out.println("JAREDMSG -> Insert Blank New TT $id from LIVEDATA")
//
//
//            }
//            val ntt = _getNonFinishedTimeTrial()
//            System.out.println("JAREDMSG -> EMIT TT id = ${ntt?.timeTrialHeader?.id} from LIVEDATA, rider count =  ${ntt?.riderList?.count()}")
//             emit(ntt)
//        }
    }

    fun getNonFinishedTimeTrialSuspend(): TimeTrial?{
        val nonFinishedTt = _getNonFinishedTimeTrial()

        if(nonFinishedTt == null){

            val id = insert(TimeTrialHeader.createBlank())
            System.out.println("JAREDMSG -> Insert Blank New TT $id from SUSPEND")
            printStackTrace()
            return _getNonFinishedTimeTrial()
        }else{
            System.out.println("JAREDMSG -> Found non finished tt ${nonFinishedTt.timeTrialHeader} from SUSPEND")
            return nonFinishedTt
        }
    }


    @Transaction
    open fun update(timeTrial: TimeTrial){
        timeTrial.timeTrialHeader.id?.let { ttId->

            val allTt = getAllTimeTrialsSuspend()
            System.out.println("JAREDMSG -> UPDATE(${timeTrial.timeTrialHeader.id} + ${timeTrial.timeTrialHeader.ttName}), Riders -> ${timeTrial.riderList.map { it.rider.firstName }},  All TT Ids = ${allTt.map { it.id }.toList()}")
            printStackTrace()

            _deleteTtEvents(ttId)
            _deleteTtRiders(ttId)

            val newEvents = timeTrial.eventList.map { it.copy(timeTrialId = ttId)}
             _insertAllEvents(newEvents)

            val newRiderList = timeTrial.riderList.map { it.copy(timeTrialId = ttId)}
            System.out.println("JAREDMSG -> UPDATE(${timeTrial.timeTrialHeader.id} + ${timeTrial.timeTrialHeader.ttName}), ${newRiderList.count()} riders into DB")

            update(timeTrial.timeTrialHeader)

            _insertAllTimeTrialRiders(newRiderList)



            val allTtR = _allTtRiders()

            System.out.println("JAREDMSG -> UPDATE(${timeTrial.timeTrialHeader.id} + ${timeTrial.timeTrialHeader.ttName}), ${allTtR.count()} TT Riders in DB")

        }

    }

    fun printStackTrace(){

        val mlist = Thread.currentThread().stackTrace.map { it.className }
        //System.out.println("JAREDMSG -> ${mlist}")
    }

    @Delete
    fun delete(timeTrial: TimeTrial){
        timeTrial.timeTrialHeader.id?.let {ttId ->

            delete(timeTrial.timeTrialHeader)
        }

    }


    @Query("DELETE FROM timetrial_table") abstract fun deleteAll()
    @Query("DELETE FROM timetrial_rider_table") abstract fun deleteAllR()
    @Query("DELETE FROM timetrial_event_table") abstract fun deleteAllE()

    @Query("SELECT * from timetrial_table ORDER BY startTime ASC") abstract fun getAllTimeTrialsSuspend(): List<TimeTrialHeader>

    @Query("SELECT * from timetrial_table ORDER BY startTime ASC") abstract fun getAllTimeTrials(): LiveData<List<TimeTrialHeader>>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE id = :ttId LIMIT 1") abstract fun getTimeTrialById(ttId: Long): LiveData<TimeTrial>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE ttName = :timeTrialName LIMIT 1") abstract fun getTimeTrialByName(timeTrialName: String): TimeTrial?

    @Transaction @Query("SELECT * FROM timetrial_table WHERE ttName = :timeTrialName LIMIT 1") abstract fun getLiveTimeTrialByName(timeTrialName: String): LiveData<TimeTrial>

    //SQLite does not have a boolean data type. Room maps it to an INTEGER column, mapping true to 1 and false to 0.
    @Transaction @Query("SELECT * FROM timetrial_table LIMIT 1") abstract fun _getNonFinishedTimeTrial(): TimeTrial?

    @Transaction @Query("SELECT * FROM timetrial_table LIMIT 1") abstract fun getNonFinishedTtLive(): LiveData<TimeTrial?>


    @Query("SELECT * FROM timetrial_table WHERE status != 2") abstract fun _getAllUncompleteTimeTrial(): List<TimeTrialHeader>


    //@Transaction @Query("SELECT * FROM timetrial_table WHERE status = 1 LIMIT 1") abstract fun getTimingTimeTrial(): LiveData<TimeTrial>

    @Transaction @Query("SELECT * FROM timetrial_table WHERE id = :timeTrialId LIMIT 1") abstract fun getFullTimeTrial(timeTrialId: Long): LiveData<TimeTrial>


    @Query("DELETE  FROM timetrial_event_table WHERE timeTrialId = :ttId") abstract fun _deleteTtEvents(ttId: Long)

    @Query("DELETE  FROM timetrial_rider_table WHERE timeTrialId = :ttId") abstract fun _deleteTtRiders(ttId: Long)

    @Query("SELECT * from timetrial_rider_table") abstract fun _allTtRiders(): List<TimeTrialRider>

    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract fun _insertAllEvents(events: List<RiderPassedEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract fun _insertAllTimeTrialRiders(riders: List<TimeTrialRider>)

}