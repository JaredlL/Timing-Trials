package com.android.jared.linden.timingtrials.edititem


import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Gender
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import kotlinx.coroutines.*
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject


class EditRiderViewModel @Inject constructor(private val repository: IRiderRepository): ViewModel() {


    val clubs: LiveData<List<String>> = repository.allClubs
    val mutableRider: MediatorLiveData<Rider> = MediatorLiveData()

    val firstName = MutableLiveData<String>("")
    val lastName: MutableLiveData<String> = MutableLiveData("")
    val yearOfBirth = MutableLiveData("")
    val selectedGenderPosition = MutableLiveData(2)
    val club = MutableLiveData<String>("")
    val genders = Gender.values().map { it.fullString() }

    init {
        mutableRider.addSource(mutableRider){
            it?.let { rider->
                val genInt = Gender.values().indexOf(rider.gender)
                if(genInt != selectedGenderPosition.value){
                    selectedGenderPosition.value = genInt
                }
                if(firstName.value != rider.firstName){
                    firstName.value = rider.firstName
                }
                if(lastName.value != rider.lastName){
                    lastName.value = rider.lastName
                }
                if(club.value != rider.club){
                    club.value = rider.club
                }
                if(yearOfBirth.value != rider.dateOfBirth.year.toString()){
                    yearOfBirth.value = rider.dateOfBirth.year.toString()
                }
            }
        }
        mutableRider.addSource(firstName){res->
            res?.let {str->
                mutableRider.value?.let { rider->
                    if(rider.firstName != str){
                        mutableRider.value = rider.copy(firstName = str)
                    }
                }
            }
        }
        mutableRider.addSource(lastName){res->
            res?.let { str->
                mutableRider.value?.let { rider->
                    if(rider.lastName != str){
                        mutableRider.value = rider.copy(lastName = str)
                    }
                }
            }
        }
        mutableRider.addSource(club){res->
            res?.let { str->
                mutableRider.value?.let { rider->
                    if(rider.club != str){
                        mutableRider.value = rider.copy(club = str)
                    }
                }
            }
        }
        mutableRider.addSource(yearOfBirth){res->
            res?.let { str->
                mutableRider.value?.let { rider->
                    if(rider.dateOfBirth.year.toString() != str){
                       mutableRider.value = rider.copy(dateOfBirth = rider.dateOfBirth.withYear(str.toIntOrNull()?:0))
                    }
                }
            }
        }
        mutableRider.addSource(selectedGenderPosition){res->
            res?.let { genPos->
                val newGen = Gender.values()[genPos]
                mutableRider.value?.let { rider->
                    if(rider.gender != newGen){
                        mutableRider.value = rider.copy(gender = newGen)
                    }
                }
            }
        }
    }

    fun initialise(riderId: Long){
        if(mutableRider.value == null){
            mutableRider.addSource(repository.getRider(riderId)){result: Rider? ->
                result?.let {
                    mutableRider.value = result
                }
            }
        }

    }

    fun addOrUpdate(){
        viewModelScope.launch(Dispatchers.IO) {
            mutableRider.value?.let { repository.insertOrUpdate(it) }
        }
    }

    fun delete(){
        viewModelScope.launch(Dispatchers.IO) {
            mutableRider.value?.let { repository.delete(it) }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}

