package com.jaredlinden.timingtrials.data.roomrepo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
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

    @WorkerThread
    suspend fun insert(rider: Rider): Long

    @WorkerThread
    suspend fun update(rider: Rider)

    @WorkerThread
    suspend fun updateRiders(riders: List<Rider>)

    fun getRider(riderId: Long) : LiveData<Rider?>

    @WorkerThread
    suspend fun delete(rider: Rider)

    @WorkerThread
    suspend fun insertOrUpdate(rider: Rider)

    suspend fun ridersFromIds(ids: List<Long>): List<Rider>

    suspend fun ridersFromFirstLastName(firstName:String, lastName:String): List<Rider>

}
@Singleton
class RoomRiderRepository @Inject constructor(private val riderDao: RiderDao) : IRiderRepository {

    override val allRiders: LiveData<List<Rider>> = riderDao.getAllRiders()

    override val allRidersLight: LiveData<List<Rider>> = riderDao.getAllRidersLight()

    override val allClubs: LiveData<List<String>> = riderDao.getAllClubs()

    override val allCategories: LiveData<List<String>> = riderDao.getAllCategories().map{res-> res.filterNotNull() }

    override suspend fun allRidersLightSuspend(): List<Rider> {
        return riderDao.getAllRidersLightSuspend()
    }

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

    @WorkerThread
    override suspend fun update(rider: Rider) {
        riderDao.update(rider)
    }

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

    @WorkerThread
    override suspend fun delete(rider: Rider) {
        riderDao.delete(rider)
    }

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