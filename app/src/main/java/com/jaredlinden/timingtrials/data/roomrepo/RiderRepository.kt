package com.jaredlinden.timingtrials.data.roomrepo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.jaredlinden.timingtrials.data.Gender
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.source.RiderDao
import javax.inject.Inject
import javax.inject.Singleton


interface IRiderRepository {

    val allRiders: LiveData<List<Rider>>


    val allRidersLight: LiveData<List<Rider>>

    suspend fun allRidersLightSuspend():List<Rider>

    val allClubs: LiveData<List<String>>

    val allCategories: LiveData<List<String>>

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(rider: Rider): Long

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(rider: Rider)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateRiders(riders: List<Rider>)

    fun getRider(riderId: Long) : LiveData<Rider?>

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(rider: Rider)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertOrUpdate(rider: Rider)

    suspend fun ridersFromIds(ids: List<Long>): List<Rider>



    suspend fun ridersFromFirstLastName(firstName:String, lastName:String): List<Rider>

}
@Singleton
class RoomRiderRepository @Inject constructor(private val riderDao: RiderDao) : IRiderRepository {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    override val allRiders: LiveData<List<Rider>> = riderDao.getAllRiders()

    override val allRidersLight: LiveData<List<Rider>> = riderDao.getAllRidersLight()

    override val allClubs: LiveData<List<String>> = riderDao.getAllClubs()


    override val allCategories: LiveData<List<String>> = Transformations.map(riderDao.getAllCategories()){res-> res.filterNotNull() }

    override suspend fun allRidersLightSuspend(): List<Rider> {
        return riderDao.getAllRidersLightSuspend()
    }

    // You must call this on a non-UI thread or your app will crash. So we're making this a
    // suspend function so the caller methods know this.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(rider: Rider):Long {
        return riderDao.insert(rider)
    }

    override suspend fun ridersFromIds(ids: List<Long>): List<Rider> {
       return riderDao.getRidersByIds(ids)
    }

    override suspend fun ridersFromFirstLastName(firstName: String, lastName: String): List<Rider> {
        return riderDao.ridersFromFirstLastName(firstName, lastName)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(rider: Rider) {
        riderDao.update(rider)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun updateRiders(riders: List<Rider>) {
        riderDao.updateList(riders)
    }


    override fun getRider(riderId: Long) : LiveData<Rider?> {
        return when(riderId){
            0L ->  MutableLiveData<Rider>(Rider.createBlank().copy(gender = Gender.MALE))
            else ->  riderDao.getRiderById(riderId)
        }
    }



    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun delete(rider: Rider) {
        riderDao.delete(rider)
    }

        @Suppress("RedundantSuspendModifier")
        @WorkerThread
        override suspend fun insertOrUpdate(rider: Rider){
            val id = rider.id ?: 0
            if(id != 0L){
                riderDao.update(rider)
            }else{
                riderDao.insert(rider)
            }

        }

}