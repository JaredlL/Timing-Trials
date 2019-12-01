package com.android.jared.linden.timingtrials.transfer

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

class ImportFragment : Fragment() {


    private lateinit var viewModel: ImportViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = requireActivity().getViewModel { requireActivity().injector.importViewModel() }

        return inflater.inflate(R.layout.fragment_import, container, false)
    }


}
