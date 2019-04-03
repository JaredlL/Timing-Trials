package com.android.jared.linden.timingtrials.edititem


import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.IRiderRepository
import kotlinx.coroutines.*
import javax.inject.Inject


class RiderViewModel @Inject constructor(private val repository: IRiderRepository): ViewModel() {


    val clubs: LiveData<List<String>> = repository.allClubs
    val mutableRider: MediatorLiveData<Rider> = MediatorLiveData()

    fun initialise(riderId: Long){
        mutableRider.addSource(repository.getRider(riderId)){result: Rider ->
            if(mutableRider.value == null){
                result.let { mutableRider.value = result
            }
            }
        }
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

