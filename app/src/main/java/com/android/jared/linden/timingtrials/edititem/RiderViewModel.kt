package com.android.jared.linden.timingtrials.edititem


import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.IRiderRepository
import kotlinx.coroutines.*


class RiderViewModel(private val repository: IRiderRepository, riderId: Long): ViewModel() {


    val rider: LiveData<Rider> = repository.getRider(riderId)
    val clubs: LiveData<List<String>> = repository.allClubs
    val mutableRider: MediatorLiveData<Rider> = MediatorLiveData()



    init{
        mutableRider.addSource(rider){result: Rider -> result.let { mutableRider.value = result }}
    }

    fun addOrUpdate(){
        viewModelScope.launch(Dispatchers.IO) {
            mutableRider.value?.let { repository.insertOrUpdate(it) }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}

