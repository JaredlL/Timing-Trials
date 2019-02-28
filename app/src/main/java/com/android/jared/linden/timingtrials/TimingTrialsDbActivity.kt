package com.android.jared.linden.timingtrials
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


const val RIDER_EXTRA = "rider_extra"
const val ITEM_TYPE_EXTRA = "item_type"
const val ITEM_ID_EXTRA = "item_id"
const val ITEM_RIDER = "item_rider"
const val ITEM_COURSE = "item_course"

class TimingTrialsDbActivity : AppCompatActivity()  {


    //private lateinit var ridersViewModel: RiderListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)

        //ridersViewModel = ViewModelProviders.of(this).get(RiderListViewModel::class.java)
        //ridersViewModel.setSelectable(true)

    }
}