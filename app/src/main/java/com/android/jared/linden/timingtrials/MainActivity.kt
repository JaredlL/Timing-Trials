package com.android.jared.linden.timingtrials

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.jared.linden.timingtrials.data.source.RiderDao
import com.android.jared.linden.timingtrials.edititem.CourseViewModel
import com.android.jared.linden.timingtrials.edititem.CourseViewModel_Factory
import com.android.jared.linden.timingtrials.setup.SetupActivity
import com.android.jared.linden.timingtrials.viewdata.CourseListViewModel
import com.android.jared.linden.timingtrials.viewdata.TimingTrialsDbActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        ma_butt_manageRiders.setOnClickListener{
            val intent = Intent(this@MainActivity, TimingTrialsDbActivity::class.java)
            startActivity(intent)
        }

        ma_butt_begintt.setOnClickListener {
            val intent = Intent(this@MainActivity, SetupActivity::class.java)
            startActivity(intent)


        }




    }
}
