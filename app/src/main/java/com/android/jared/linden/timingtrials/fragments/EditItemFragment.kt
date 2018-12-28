package com.android.jared.linden.timingtrials.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.viewmodels.MyViewModelFactory
import com.android.jared.linden.timingtrials.viewmodels.RiderViewModel
import com.android.jared.linden.timingtrials.viewmodels.RidersViewModel

class EditItemFragment : Fragment() {

    companion object {
        fun newInstance() = EditItemFragment()
    }

    private lateinit var viewModel: RiderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_item_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RiderViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
