package com.android.jared.linden.timingtrials.setup

import androidx.lifecycle.LiveData
import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider

interface IOrderRidersViewModel{
    fun getOrderableRiderData(): LiveData<TimeTrial?>
    fun moveItem(fromPosition: Int, toPosition:Int)
}