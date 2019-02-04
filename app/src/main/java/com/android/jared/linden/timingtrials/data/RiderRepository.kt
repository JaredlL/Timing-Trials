package com.android.jared.linden.timingtrials.data

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class RiderRepository(private  val riderDao: RiderDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allRiders: LiveData<List<Rider>> = riderDao.getAllRiders()

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(rider: Rider) {
        riderDao.insert(rider)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(rider: Rider) {
        riderDao.updateRider(rider)
    }

        @Suppress("RedundantSuspendModifier")
        @WorkerThread
        suspend fun insertOrUpdate(rider: Rider){
            val id = rider.Id ?: 0
            if(id != 0L){
                riderDao.updateRider(rider)
            }else{
                riderDao.insert(rider)
            }

        }

}