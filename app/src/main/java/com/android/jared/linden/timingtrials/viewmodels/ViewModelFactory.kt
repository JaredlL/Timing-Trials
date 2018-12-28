package com.android.jared.linden.timingtrials.viewmodels

import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.android.jared.linden.timingtrials.data.Rider


class MyViewModelFactory(private val mApplication: Application, private val rider: Rider) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RiderViewModel(rider) as T
    }
}