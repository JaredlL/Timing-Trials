package com.jaredlinden.timingtrials.resultexplorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentResultFilterBinding


class ResultsFilterFragment : BottomSheetDialogFragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        val binding = DataBindingUtil.inflate<FragmentResultFilterBinding>(inflater, R.layout.fragment_result_filter, container, false).apply {
        }

        return binding.root

    }

}