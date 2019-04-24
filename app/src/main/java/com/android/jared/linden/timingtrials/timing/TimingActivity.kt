package com.android.jared.linden.timingtrials.timing

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.OneShotPreDrawListener.add
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.setup.SelectCourseFragment
import com.android.jared.linden.timingtrials.setup.SetupTimeTrialFragment
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA

import kotlinx.android.synthetic.main.activity_timing.*

class TimingActivity : AppCompatActivity() {

    private val TIMERTAG = "timing_tag"
    private val STATUSTAG = "status_tag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timing)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val itemId = intent.getLongExtra(ITEM_ID_EXTRA, 0)

        /**
         * Check if the fragemts already exist in child fragment manager
         * To make sure we do not recreate fragments unnecessarily
         */

        supportFragmentManager.findFragmentByTag(TIMERTAG)?: TimerFragment.newInstance(itemId).also {
            supportFragmentManager.beginTransaction().apply{
                add(R.id.higherFrame, it, TIMERTAG)
                commit()
            }
        }

        supportFragmentManager.findFragmentByTag(STATUSTAG)?: RiderStatusFragment.newInstance(itemId).also {
            supportFragmentManager.beginTransaction().apply{
                add(R.id.lowerFrame, it, STATUSTAG)
                commit()
            }
        }
    }

}
