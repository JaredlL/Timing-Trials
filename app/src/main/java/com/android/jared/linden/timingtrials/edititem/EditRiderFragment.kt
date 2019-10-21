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
import com.android.jared.linden.timingtrials.data.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

class EditRiderFragment : Fragment() {

    companion object {
        fun newInstance(riderId: Long): EditRiderFragment {
            val args = Bundle().apply { putLong(ITEM_ID_EXTRA, riderId) }
            return EditRiderFragment().apply { arguments = args }
        }
    }

    //private lateinit var riderViewModel:
    private val riderId by argument<Long>(ITEM_ID_EXTRA)
    private lateinit var riderViewModel: EditRiderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        riderViewModel = getViewModel { injector.riderViewModel().apply { initialise(riderId) } }
        val mAdapter = ArrayAdapter<String>(requireActivity(), R.layout.support_simple_spinner_dropdown_item, mutableListOf())

        riderViewModel.clubs.observe(viewLifecycleOwner, Observer{
            mAdapter.clear()
            it.forEachIndexed { index, s -> mAdapter.insert(s, index)  }
            mAdapter.notifyDataSetChanged()
        })

        activity?.title = if(riderId == 0L) getString(R.string.add_rider) else getString(R.string.edit_rider)

        val binding = DataBindingUtil.inflate<FragmentRiderBinding>(inflater, R.layout.fragment_rider, container, false).apply {
            viewModel = riderViewModel
            lifecycleOwner = (this@EditRiderFragment)
            autoCompleteClub.setAdapter(mAdapter)
            editRiderFab.setOnClickListener {
                if(riderViewModel.mutableRider.value?.firstName != ""){
                    riderViewModel.addOrUpdate()
                    activity?.finish()
                }else{
                    Toast.makeText(activity, "Rider must have firstname set", Toast.LENGTH_SHORT).show()
                }

            }
            deleteButton.setOnClickListener {
                riderViewModel.delete()
                activity?.finish()
            }

        }

        return binding.root
    }


}
