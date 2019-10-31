package com.android.jared.linden.timingtrials.data.roomrepo

import androidx.lifecycle.LiveData
import com.android.jared.linden.timingtrials.data.GlobalResult
import com.android.jared.linden.timingtrials.data.source.GlobalResultDao
import javax.inject.Inject
import javax.inject.Singleton

interface IGlobalResultRepository{

  fun getAllResults(): LiveData<List<GlobalResult>>

    fun getRiderResults(riderId: Long): LiveData<List<GlobalResult>>

}

@Singleton
class GlobalResultRepository @Inject constructor(private val  globalResultDao: GlobalResultDao) : IGlobalResultRepository{

    override fun getAllResults(): LiveData<List<GlobalResult>> {
        return globalResultDao.getAllResults()
    }

    override fun getRiderResults(riderId: Long): LiveData<List<GlobalResult>> {
        return globalResultDao.getResultsForRiderID(riderId)
    }

}