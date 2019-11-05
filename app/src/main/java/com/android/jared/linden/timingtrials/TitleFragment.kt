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
import com.android.jared.linden.timingtrials.databinding.FragmentTitleBinding


class TitleFragment : Fragment()
{

    private lateinit var titleViewmodel: TitleViewModel

    private lateinit var testViewModel: TestViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        titleViewmodel = getViewModel { injector.mainViewModel() }
        testViewModel = getViewModel { injector.testViewModel() }

        val binding =  DataBindingUtil.inflate<FragmentTitleBinding>(inflater, R.layout.fragment_title, container, false).apply{

            startTtSetupButton.setOnClickListener{
                val action = TitleFragmentDirections.actionTitleFragmentToSetupViewPagerFragment2()
                Navigation.findNavController(this.root).navigate(action)
            }

            viewDatabaseButton.setOnClickListener {
                val action = TitleFragmentDirections.actionDataBaseViewPagerFragmentToDataBaseViewPagerFragment2()
                Navigation.findNavController(this.root).navigate(action)
            }

            testSetupButton.setOnClickListener {
                testViewModel.insertSetupTt()
                val action = TitleFragmentDirections.actionTitleFragmentToSetupViewPagerFragment2()
                Navigation.findNavController(this.root).navigate(action)

            }

            testTimingButton.setOnClickListener {
                val tvm = getViewModel { injector.testViewModel() }
                tvm.insertTimingTt()
                val tIntent = Intent(requireActivity(), TimingActivity::class.java)
                startActivity(tIntent)

            }
        }





        binding.testResults1.setOnClickListener {
            testViewModel.insertFinishedTt()
            testViewModel.newId.observe(this, Observer {res->
                res?.let {
                    val mIntent = Intent(requireActivity(), ResultActivity::class.java)
                    mIntent.putExtra(ITEM_ID_EXTRA, it)
                    startActivity(mIntent)
                    testViewModel.newId.removeObservers(this)
                }

            })

        }







        return binding.root
    }
}