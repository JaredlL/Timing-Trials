package com.android.jared.linden.timingtrials

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.databinding.FragmentTitleBinding
import kotlinx.android.synthetic.main.fragment_title.*

class TitleFragment : Fragment()
{

    private lateinit var titleViewModel: TitleViewModel

    private lateinit var testViewModel: TestViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        titleViewModel = requireActivity().getViewModel {  requireActivity().injector.mainViewModel() }
        testViewModel =  requireActivity().getViewModel {  requireActivity().injector.testViewModel() }

        titleViewModel.nonFinishedTimeTrial.observe(viewLifecycleOwner, Observer {tt->
            tt?.let { timeTrial->
                if(timeTrial.timeTrialHeader.status == TimeTrialStatus.IN_PROGRESS){
                    val tIntent = Intent(requireActivity(), TimingActivity::class.java)
                    startActivity(tIntent)
                }
            }
        })

        val binding =  DataBindingUtil.inflate<FragmentTitleBinding>(inflater, R.layout.fragment_title, container, false).apply{

            startTtSetupButton.setOnClickListener{
                titleViewModel.nonFinishedTimeTrial.observe(viewLifecycleOwner, Observer {result->
                    result?.let {timeTrial->
                        if(timeTrial.timeTrialHeader.copy(id = null) != TimeTrialHeader.createBlank()){
                            showSetupDialog(timeTrial)
                        }else{
                            val action = TitleFragmentDirections.actionTitleFragmentToSetupViewPagerFragment2()
                            findNavController().navigate(action)
                        }
                    }
                })

            }

            viewDatabaseButton.setOnClickListener {
                val action = TitleFragmentDirections.actionDataBaseViewPagerFragmentToDataBaseViewPagerFragment2()
                Navigation.findNavController(this.root).navigate(action)
            }

            testSetupButton.setOnClickListener {
                titleViewModel.nonFinishedTimeTrial.value?.let {
                    testViewModel.testSetup(it)
                }
            }


            testTimingButton.setOnClickListener {
                titleViewModel.nonFinishedTimeTrial.value?.let {
                    testViewModel.testTiming(it)
                }



            }
        }


        return binding.root
    }

   private fun showSetupDialog(timeTrial: TimeTrial){
        AlertDialog.Builder(requireActivity())
                .setTitle(resources.getString(R.string.resume_setup))
                .setMessage("${resources.getString(R.string.resume_setup)} ${timeTrial.timeTrialHeader.ttName}?")
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->

                    val action = TitleFragmentDirections.actionTitleFragmentToSetupViewPagerFragment2()
                    findNavController().navigate(action)
                }
                .setNegativeButton(resources.getString(R.string.start_new)){_,_->
                    titleViewModel.clearTimeTrial(timeTrial)
                    val action = TitleFragmentDirections.actionTitleFragmentToSetupViewPagerFragment2()
                    findNavController().navigate(action)
                }
                .setNeutralButton(resources.getString(R.string.dismiss)){_,_->

                }
                .create().show()
    }
}