package com.android.jared.linden.timingtrials.data.roomrepo

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.android.jared.linden.timingtrials.data.FilledResult
import com.android.jared.linden.timingtrials.data.GlobalResult
import com.android.jared.linden.timingtrials.data.IResult
import com.android.jared.linden.timingtrials.data.source.GlobalResultDao
import javax.inject.Inject
import javax.inject.Singleton

interface IGlobalResultRepository{

    fun getAllResults(): LiveData<List<GlobalResult>>

    fun getRiderResults(riderId: Long): LiveData<List<FilledResult>>

    fun getCourseResults(courseId: Long): LiveData<List<FilledResult>>

    suspend fun insertNewResults(newResults: List<IResult>)

    suspend fun getResultsForTimeTrial(timeTrialId: Long): List<GlobalResult>

}

@Singleton
class GlobalResultRepository @Inject constructor(private val  globalResultDao: GlobalResultDao) : IGlobalResultRepository{

    override fun getAllResults(): LiveData<List<GlobalResult>> {
        return globalResultDao.getAllResults()
    }

    override fun getRiderResults(riderId: Long): LiveData<List<FilledResult>> {
        return globalResultDao.getResultsForRider(riderId)
    }

    override fun getCourseResults(courseId: Long): LiveData<List<FilledResult>> {
        return globalResultDao.getResultsForCourseID(courseId)
    }

    override suspend fun insertNewResults(newResults: List<IResult>) {
        globalResultDao.insertMultiple(newResults.map { GlobalResult.fromiResult(it) })
    }

    override suspend fun getResultsForTimeTrial(timeTrialId: Long): List<GlobalResult> {
        return globalResultDao.getResultsForTimeTrialIDSuspend(timeTrialId)
    }

}