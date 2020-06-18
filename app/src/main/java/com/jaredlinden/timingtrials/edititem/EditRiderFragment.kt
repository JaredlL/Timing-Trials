package com.jaredlinden.timingtrials.edititem

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks

import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentEditRiderBinding
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector

class EditRiderFragment : Fragment() {


    private val args: EditRiderFragmentArgs by navArgs()
    private lateinit var riderViewModel: EditRiderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        riderViewModel = requireActivity().getViewModel { requireActivity().injector.riderViewModel() }
        riderViewModel.mutableRider.observe(viewLifecycleOwner, Observer {  })

        riderViewModel.changeRider(args.riderId)
        setHasOptionsMenu(true)



        val categoryAdapter = ArrayAdapter<String>(requireActivity(), R.layout.support_simple_spinner_dropdown_item, mutableListOf())
        riderViewModel.categories.observe(viewLifecycleOwner, Observer{res->
            res?.let {cats->
                categoryAdapter.clear()
                cats.filterNot { it.isBlank() }.forEachIndexed { index, s -> categoryAdapter.insert(s, index)  }
                categoryAdapter.notifyDataSetChanged()
            }
        })

        val clubAdapter = ArrayAdapter<String>(requireActivity(), R.layout.support_simple_spinner_dropdown_item, mutableListOf())
        riderViewModel.clubs.observe(viewLifecycleOwner, Observer{res->
            res?.let {clubs->
                clubAdapter.clear()
                clubs.filterNot { it.isBlank() }.forEachIndexed { index, s -> clubAdapter.insert(s, index)  }
                clubAdapter.notifyDataSetChanged()
            }
        })



        //Set title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = if(args.riderId == 0L) getString(R.string.add_rider) else getString(R.string.edit_rider)

        val fabCallback = (requireActivity() as IFabCallbacks)

        fabCallback.setImage(R.drawable.ic_done_white_24dp)
        fabCallback.setVisibility(View.VISIBLE)

        val binding = DataBindingUtil.inflate<FragmentEditRiderBinding>(inflater, R.layout.fragment_edit_rider, container, false).apply {
            viewModel = riderViewModel
            lifecycleOwner = (this@EditRiderFragment)
            autoCompleteClub.setAdapter(clubAdapter)
            autoCompleteCategory.setAdapter(categoryAdapter)
            fabCallback.setAction {
                if(!riderViewModel.mutableRider.value?.firstName?.trim().isNullOrBlank()){
                    riderViewModel.addOrUpdate()
                    findNavController().popBackStack()

                }else{
                    Toast.makeText(requireContext(), "Rider must have firstname set", Toast.LENGTH_SHORT).show()
                }

            }
            autoCompleteCategory.setOnEditorActionListener{_, actionId, keyEvent ->
                if ((keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    riderViewModel.addOrUpdate()
                    findNavController().popBackStack()
                }
                return@setOnEditorActionListener false
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
