package com.android.jared.linden.timingtrials.edititem


import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Gender
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import kotlinx.coroutines.*
import org.threeten.bp.LocalDate
import javax.inject.Inject


class EditRiderViewModel @Inject constructor(private val repository: IRiderRepository): ViewModel() {


    val clubs: LiveData<List<String>> = repository.allClubs
    val categories: LiveData<List<String>> = repository.allCategories

    val mutableRider: MediatorLiveData<Rider> = MediatorLiveData()

    val firstName = MutableLiveData<String>("")
    val lastName: MutableLiveData<String> = MutableLiveData("")
    val yearOfBirth = MutableLiveData("")
    val selectedGenderPosition = MutableLiveData(2)
    val club = MutableLiveData<String>("")
    val category = MutableLiveData<String>("")
    val genders = Gender.values().map { it.fullString() }

    private val currentId = MutableLiveData<Long>(0L)

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
                if(category.value != rider.category){
                    category.value = rider.category
                }
                val yobString = rider.dateOfBirth?.year?.toString() ?:""
                if(yearOfBirth.value != yobString){
                    yearOfBirth.value = yobString
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
        mutableRider.addSource(category){res->
            res?.let { str->
                mutableRider.value?.let { rider->
                    if(rider.category != str){
                        mutableRider.value = rider.copy(category = str)
                    }
                }
            }
        }
        mutableRider.addSource(yearOfBirth){res->
            res?.let { str->
                mutableRider.value?.let { rider->
                    val strInt = str.toIntOrNull()
                    if(rider.dateOfBirth?.year != strInt && strInt != null){
                       mutableRider.value = rider.copy(dateOfBirth = LocalDate.of(strInt, 1, 1))
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
        mutableRider.addSource(Transformations.switchMap(currentId){
            if(it != mutableRider.value?.id){
                repository.getRider(it)
            }else{
                null
            }

        }){res->
            res?.let { rider->
                if(mutableRider.value != rider){
                    mutableRider.value = rider
                }
            }
        }
    }

    fun changeRider(riderId: Long){
        if(currentId.value != riderId){
            currentId.value = riderId
        }
    }



    fun addOrUpdate(){
        viewModelScope.launch(Dispatchers.IO) {
            mutableRider.value?.let { rider->
                val trimmed = rider.copy(
                        firstName = rider.firstName.trim(),
                        lastName = rider.lastName.trim(),
                        club = rider.club.trim(),
                        category = rider.category.trim())
                repository.insertOrUpdate(trimmed)
            }
            mutableRider.postValue(Rider.createBlank())
        }

    }

    fun delete(){
        viewModelScope.launch(Dispatchers.IO) {
            mutableRider.value?.let { repository.delete(it) }
            mutableRider.postValue(Rider.createBlank())
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}

