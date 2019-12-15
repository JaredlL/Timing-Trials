package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrialRider

interface IOrderRidersViewModel{
    fun getOrderableRiders(): LiveData<List<FilledTimeTrialRider>>
    fun moveItem(fromPosition: Int, toPosition:Int)
}