package com.android.jared.linden.timingtrials.viewmodels

import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.util.ObservableViewModel

class RiderViewModel(val rider: Rider): ObservableViewModel() {


    val fullName = rider.firstName + " " + rider.lastName

    val firstName = rider.firstName

    val lastName = rider.lastName

    val club = ObservableField<String>(rider.club)

    var editRider = {(rider):Rider -> Unit}

    fun changeName(v: View):Boolean{
        editRider(rider)
        return true
    }


}