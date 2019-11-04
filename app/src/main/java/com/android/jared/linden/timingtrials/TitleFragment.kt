package com.android.jared.linden.timingtrials

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.jared.linden.timingtrials.data.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.timetrialresults.ResultActivity
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.databinding.FragmentTitleBinding


class TitleFragment : Fragment()
{

    var setupTimeTrial: TimeTrial? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val vm = getViewModel { injector.mainViewModel() }

        val binding =  DataBindingUtil.inflate<FragmentTitleBinding>(inflater, R.layout.fragment_title, container, false).apply{

            maButtManageRiders.setOnClickListener {
                val action = TitleFragmentDirections.actionDataBaseViewPagerFragmentToDataBaseViewPagerFragment2()
                Navigation.findNavController(this.root).navigate(action)
            }

            createSetupButton.setOnClickListener {
                val tvm = getViewModel { injector.testViewModel() }
                tvm.insertSetupTt()
                val action = TitleFragmentDirections.actionTitleFragmentToSetupViewPagerFragment2()
                Navigation.findNavController(this.root).navigate(action)

            }

            createTimingButton.setOnClickListener {
                val tvm = getViewModel { injector.testViewModel() }
                tvm.insertTimingTt()
                val tIntent = Intent(requireActivity(), TimingActivity::class.java)
                startActivity(tIntent)

            }





        }



        binding.createFinishedButton.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }
            tvm.insertFinishedTt()
            tvm.newId.observe(this, Observer {res->
                res?.let {
                    val mIntent = Intent(requireActivity(), ResultActivity::class.java)
                    mIntent.putExtra(ITEM_ID_EXTRA, it)
                    startActivity(mIntent)
                    tvm.newId.removeObservers(this)
                }

            })

        }







        return binding.root
    }
}