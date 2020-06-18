package com.jaredlinden.timingtrials.edititem

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.R

class EditResultFragment : Fragment() {


    private lateinit var viewModel: EditResultViewModel

    private val args: EditResultFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Toast.makeText(requireContext(), args.resultId.toString(), Toast.LENGTH_SHORT).show()
        return inflater.inflate(R.layout.fragment_edit_result, container, false)
    }


}