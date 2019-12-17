package com.android.jared.linden.timingtrials.edititem

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentRiderBinding
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

class EditRiderFragment : Fragment() {


    private val args: EditRiderFragmentArgs by navArgs()
    private lateinit var riderViewModel: EditRiderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        riderViewModel = requireActivity().getViewModel { requireActivity().injector.riderViewModel() }
        riderViewModel.mutableRider.observe(viewLifecycleOwner, Observer {  })

        riderViewModel.changeRider(args.riderId)
        setHasOptionsMenu(true)

        val mAdapter = ArrayAdapter<String>(requireActivity(), R.layout.support_simple_spinner_dropdown_item, mutableListOf())


        riderViewModel.clubs.observe(viewLifecycleOwner, Observer{
            mAdapter.clear()
            it.forEachIndexed { index, s -> mAdapter.insert(s, index)  }
            mAdapter.notifyDataSetChanged()
        })

        //Set title
        (requireActivity() as AppCompatActivity).title = if(args.riderId == 0L) getString(R.string.add_rider) else getString(R.string.edit_rider)


        val binding = DataBindingUtil.inflate<FragmentRiderBinding>(inflater, R.layout.fragment_rider, container, false).apply {
            viewModel = riderViewModel
            lifecycleOwner = (this@EditRiderFragment)
            autoCompleteClub.setAdapter(mAdapter)
            editRiderFab.setOnClickListener {
                if(riderViewModel.mutableRider.value?.firstName != ""){
                    riderViewModel.addOrUpdate()
                    findNavController().popBackStack()
                }else{
                    Toast.makeText(activity, "Rider must have firstname set", Toast.LENGTH_SHORT).show()
                }

            }

        }


        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete_deleteitem -> {
                showDeleteDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showDeleteDialog(){
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_rider))
                .setMessage(resources.getString(R.string.confirm_delete_rider_message))
                .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                    riderViewModel.delete()
                    findNavController().popBackStack()
                }
                .setNegativeButton("Dismiss"){_,_->

                }
                .create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //menu.clear()
        inflater.inflate(R.menu.menu_delete, menu)
    }


}
