package com.android.jared.linden.timingtrials.viewmodels

import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.util.ObservableViewModel

class RiderViewModel(val rider: Rider): ObservableViewModel() {


    val fullName = rider.firstName + " " + rider.lastName

    @Bindable
    fun getFirstName(): String{
        return rider.firstName
    }

    @Bindable
    fun setFirstName(value: String){
        rider.firstName = value
    }

    @Bindable
    fun getLastName(): String{
        return rider.lastName
    }

    @Bindable
    fun setLastName(value: String){
        rider.lastName = value
    }

    @Bindable
    fun getClub(): String{
        return rider.club
    }

    @Bindable
    fun setClub(value: String){
        rider.club = value
    }

    val club = ObservableField<String>(rider.club)

    var editRider = {(rider):Rider -> Unit}

    fun changeName(v: View):Boolean{
        editRider(rider)
        return true
    }


}