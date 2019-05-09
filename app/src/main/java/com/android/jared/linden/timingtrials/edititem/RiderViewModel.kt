package com.android.jared.linden.timingtrials.edititem


import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import kotlinx.coroutines.*
import javax.inject.Inject


class RiderViewModel @Inject constructor(private val repository: IRiderRepository): ViewModel() {


    val clubs: LiveData<List<String>> = repository.allClubs
    val mutableRider: MediatorLiveData<Rider> = MediatorLiveData()

    val firstName = MutableLiveData<String>("")
    val lastName = MutableLiveData<String>("")
    val club = MutableLiveData<String>("")

    fun initialise(riderId: Long){
        if(mutableRider.value == null){
            mutableRider.addSource(repository.getRider(riderId)){result: Rider ->
                result.let {
                    mutableRider.value = result
                    firstName.value = result.firstName
                    lastName.value = result.lastName
                    club.value = result.club
                }
            }
        }

    }

    fun addOrUpdate(){
        viewModelScope.launch(Dispatchers.IO) {

            val fname = firstName.value?:""
            val lname = lastName.value?:""
            val club = club.value?:""

            mutableRider.value?.let { repository.insertOrUpdate(it.copy(firstName = fname, lastName = lname, club = club)) }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}

