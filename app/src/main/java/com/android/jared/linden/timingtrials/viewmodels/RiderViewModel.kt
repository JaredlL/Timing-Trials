package com.android.jared.linden.timingtrials.viewmodels

import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.util.ObservableViewModel

class RiderViewModel(rider: Rider): ObservableViewModel() {
    val name = rider.firstName + " " + rider.lastName
    val club = rider.club
}