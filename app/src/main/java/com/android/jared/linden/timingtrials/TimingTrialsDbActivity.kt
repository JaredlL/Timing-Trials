package com.android.jared.linden.timingtrials
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.jared.linden.timingtrials.fragments.RiderListFragment
import kotlinx.android.synthetic.main.activity_database.*


const val RIDER_EXTRA = "rider_extra"

class TimingTrialsDbActivity : AppCompatActivity()  {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)
    }
}