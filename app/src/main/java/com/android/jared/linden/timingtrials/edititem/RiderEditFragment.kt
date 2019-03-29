package com.android.jared.linden.timingtrials.edititem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentRiderBinding
import com.android.jared.linden.timingtrials.util.argument
import kotlinx.android.synthetic.main.fragment_rider.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


const val RIDER_ID_EXTRA = "rider_id"

class RiderEditFragment : Fragment() {

    companion object {
        fun newInstance(riderId: Long): RiderEditFragment {
            val args = Bundle().apply { putLong(RIDER_ID_EXTRA, riderId) }
            return RiderEditFragment().apply { arguments = args }
        }
    }

    //private lateinit var riderViewModel:
    private val riderId by argument<Long>(RIDER_ID_EXTRA)
    private val riderViewModel: RiderViewModel by viewModel { parametersOf(riderId) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val mAdapter = ArrayAdapter<String>(requireActivity(), R.layout.support_simple_spinner_dropdown_item, mutableListOf())

        riderViewModel.clubs.observe(viewLifecycleOwner, Observer{
            mAdapter.clear()
            it.forEachIndexed { index, s -> mAdapter.insert(s, index)  }
            mAdapter.notifyDataSetChanged()
        })

        val binding = DataBindingUtil.inflate<FragmentRiderBinding>(inflater, R.layout.fragment_rider, container, false).apply {
            viewModel = riderViewModel
            lifecycleOwner = (this@RiderEditFragment)
            autoCompleteClub.setAdapter(mAdapter)
            editRiderFab.setOnClickListener {
                if(riderViewModel.rider.value?.firstName != ""){
                    riderViewModel.addOrUpdate()
                    activity?.finish()
                }else{
                    Toast.makeText(activity, "Rider must have firstname set", Toast.LENGTH_SHORT).show()
                }

            }

        }

        return binding.root
    }


}
