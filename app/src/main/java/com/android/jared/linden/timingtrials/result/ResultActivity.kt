package com.android.jared.linden.timingtrials.result

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.timing.TimingViewModel
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)

        val timeTrialId by argument<Long>(ITEM_ID_EXTRA)
        val resultViewModel = getViewModel { injector.resultViewModel() }.apply { initialise(timeTrialId) }

        resultViewModel.timeTrial.observe(this, Observer {res->
            res?.let {
                it.helper.results
            }
        })



    }

}
