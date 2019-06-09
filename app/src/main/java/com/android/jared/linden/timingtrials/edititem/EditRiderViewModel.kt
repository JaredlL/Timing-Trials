package com.android.jared.linden.timingtrials.edititem


import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Gender
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.roomrepo.IRiderRepository
import com.android.jared.linden.timingtrials.util.createLink
import kotlinx.coroutines.*
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject


class EditRiderViewModel @Inject constructor(private val repository: IRiderRepository): ViewModel() {


    val clubs: LiveData<List<String>> = repository.allClubs
    val mutableRider: MediatorLiveData<Rider> = MediatorLiveData()


    fun initialise(riderId: Long){
        if(mutableRider.value == null){
            mutableRider.addSource(repository.getRider(riderId)){result: Rider? ->
                result?.let {
                    mutableRider.value = result
                }
            }
        }

    }

    val genders = Gender.values().map { it.fullString() }

    val selectedGenderPosition = MutableLiveData(1).createLink(
            mutableRider,
            {new -> mutableRider.value?.let { Pair(Gender.values().indexOf(it.gender), it.copy(gender = Gender.values()[new])) }},
            {Gender.values().indexOf(mutableRider.value?.gender?:Gender.UNKNOWN)}
    )

    val yearOfBirth = MutableLiveData("").createLink(
            mutableRider,
            {new -> mutableRider.value?.let { Pair(it.dateOfBirth.year.toString(), it.copy(dateOfBirth = it.dateOfBirth.withYear(new?.toIntOrNull()?:0))) }},
            {mutableRider.value?.dateOfBirth?.year?.toString()?:""}
    )

//    var selectedGendarPosition = 1
//    set(value) {
//        mutableRider.value?.let {
//            mutableRider.value = it.copy(gender = Gender.values()[selectedGendarPosition])
//        }
//        field = value
//    }

    val lastName: MutableLiveData<String> = MutableLiveData<String>("").createLink(
            mutableRider,
            {new -> mutableRider.value?.let { Pair(it.lastName, it.copy(lastName = new)) }},
            {mutableRider.value?.lastName?:"" }
    )


    val club = MutableLiveData<String>("")
    private val clubMediator = MediatorLiveData<String>().apply {
        addSource(club) {str->
            mutableRider.value?.let { ri ->
                if(ri.club != str){
                    mutableRider.value = ri.copy(club = str)
                }
            }
        }
        addSource(mutableRider) { r->
            r?.let {
                if (club.value != r.club) {
                    club.value = r.club
                }
            }
        }
    }.also { it.observeForever {  } }

    val firstName = MutableLiveData<String>("")
    private val firstNameMediator = MediatorLiveData<String>().apply {
        addSource(firstName) {str->
            mutableRider.value?.let { ri ->
                    if(ri.firstName != str){
                        mutableRider.value = ri.copy(firstName = str)
                    }
            }
        }
        addSource(mutableRider) { r->
            r?.let {
                if (firstName.value != r.firstName) {
                    firstName.value = r.firstName
                }
            }

        }
    }.also { it.observeForever {  } }

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

