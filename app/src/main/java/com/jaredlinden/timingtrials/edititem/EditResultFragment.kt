package com.jaredlinden.timingtrials.edititem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentEditResultBinding
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.hideKeyboard
import com.jaredlinden.timingtrials.util.injector

class EditResultFragment : Fragment() {



    private val args: EditResultFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val resultVm = requireActivity().getViewModel { requireActivity().injector.editResultViewModel() }


        //Set title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = if(args.resultId == 0L) getString(R.string.add_result) else getString(R.string.edit_result)


        val fabCallback = (requireActivity() as IFabCallbacks)
        fabCallback.setImage(R.drawable.ic_done_white_24dp)
        fabCallback.setVisibility(View.VISIBLE)

        fabCallback.setAction {
            resultVm.save()
        }

        resultVm.changeRider.observe(viewLifecycleOwner, EventObserver{
            if(it){

            }
        })

        resultVm.resultSaved.observe(viewLifecycleOwner,EventObserver{
            if(it){
                hideKeyboard()
                findNavController().popBackStack()
            }
        })

        resultVm.setResult(args.resultId)

        val binding = DataBindingUtil.inflate<FragmentEditResultBinding>(inflater, R.layout.fragment_edit_result, container, false).apply {
            viewModel = resultVm
            lifecycleOwner = this@EditResultFragment
        }

        return binding.root

    }


}