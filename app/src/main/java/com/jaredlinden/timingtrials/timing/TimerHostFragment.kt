package com.jaredlinden.timingtrials.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jaredlinden.timingtrials.R
import kotlinx.android.synthetic.main.fragment_host.*

class TimerHostFragment : Fragment() {

    private val TIMERTAG = "timing_tag"
    private val STATUSTAG = "status_tag"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_host, container, false)

        childFragmentManager.findFragmentByTag(TIMERTAG)?: TimerFragment.newInstance().also {
            childFragmentManager.beginTransaction().apply{
                add(R.id.higherFrame, it, TIMERTAG)
                commit()
            }
        }

        childFragmentManager.findFragmentByTag(STATUSTAG)?: RiderStatusFragment.newInstance().also {
            childFragmentManager.beginTransaction().apply{
                add(R.id.lowerFrame, it, STATUSTAG)
                commit()
            }
        }

        return v

    }

}