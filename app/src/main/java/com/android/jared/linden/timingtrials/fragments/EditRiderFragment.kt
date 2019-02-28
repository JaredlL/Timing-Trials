package com.android.jared.linden.timingtrials.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentEditRiderBinding
import com.android.jared.linden.timingtrials.util.InjectorUtils
import com.android.jared.linden.timingtrials.viewmodels.RiderViewModel


const val RIDER_ID_EXTRA = "rider_id"

class EditRiderFragment : Fragment() {

    companion object {
        fun newInstance(riderId: Long): EditRiderFragment {
            val args = Bundle().apply { putLong(RIDER_ID_EXTRA, riderId) }
            return EditRiderFragment().apply { arguments = args }
        }
    }

    private lateinit var riderViewModel: RiderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val riderId = arguments?.getLong(RIDER_ID_EXTRA, 0) ?: 0
        val factory = InjectorUtils.provideRiderViewModelFactory(requireActivity(), riderId)
        riderViewModel = ViewModelProviders.of(this, factory).get(RiderViewModel::class.java)

        val binding = DataBindingUtil.inflate<FragmentEditRiderBinding>(inflater, R.layout.fragment_edit_rider, container, false).apply {
            viewModel = riderViewModel
            lifecycleOwner = (this@EditRiderFragment)

            editRiderFab.setOnClickListener {
                riderViewModel.addOrUpdate()
                activity?.finish()
            }

        }

        return binding.root
    }


}
