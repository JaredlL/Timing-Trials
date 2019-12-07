package com.android.jared.linden.timingtrials.transfer

import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.REQUEST_IMPORT_FILE
import com.android.jared.linden.timingtrials.databinding.FragmentImportBinding
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector



class ImportFragment : Fragment() {


    private lateinit var viewModel: ImportViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = requireActivity().getViewModel { requireActivity().injector.importViewModel() }

        val binding = DataBindingUtil.inflate<FragmentImportBinding>(inflater, R.layout.fragment_import, container, false).apply {
            selectFileButton.setOnClickListener {
                val intent = Intent(ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                //intent.categories.add(CATEGORY_OPENABLE)
                requireActivity().startActivityForResult(intent, REQUEST_IMPORT_FILE)
            }



        }
        return binding.root
    }


}
