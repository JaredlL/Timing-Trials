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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks

import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.databinding.FragmentEditRiderBinding
import com.jaredlinden.timingtrials.util.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditRiderFragment : Fragment() {


    private val args: EditRiderFragmentArgs by navArgs()
    private val riderViewModel: EditRiderViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        setHasOptionsMenu(true)


        riderViewModel.mutableRider.observe(viewLifecycleOwner, Observer {  })

        riderViewModel.changeRider(args.riderId)





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

        riderViewModel.message.observe(viewLifecycleOwner, EventObserver{
            Toast.makeText(requireContext(), requireContext().getText(it), Toast.LENGTH_LONG).show()
        })

        riderViewModel.doJumpToRiderResults.observe(viewLifecycleOwner, EventObserver{
            val action = EditRiderFragmentDirections.actionEditRiderFragmentToSheetFragment(Rider::class.java.simpleName, it)
            findNavController().navigate(action)
        })

        riderViewModel.updateSuccess.observe(viewLifecycleOwner, EventObserver{
            if(it){
                findNavController().popBackStack()
            }
        })


        //Set title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = if(args.riderId == 0L) getString(R.string.add_rider) else getString(R.string.edit_rider)

        val fabCallback = (requireActivity() as IFabCallbacks)

        fabCallback.setFabImage(R.drawable.ic_done_white_24dp)
        fabCallback.setFabVisibility(View.VISIBLE)

        val binding = DataBindingUtil.inflate<FragmentEditRiderBinding>(inflater, R.layout.fragment_edit_rider, container, false).apply {
            viewModel = riderViewModel
            lifecycleOwner = (this@EditRiderFragment)
            autoCompleteClub.setAdapter(clubAdapter)
            autoCompleteCategory.setAdapter(categoryAdapter)
            fabCallback.fabClickEvent.observe(viewLifecycleOwner, EventObserver {
                if(it){
                    if(!riderViewModel.mutableRider.value?.firstName?.trim().isNullOrBlank()){
                        riderViewModel.addOrUpdate()
                        //findNavController().popBackStack()

                    }else{
                        Toast.makeText(requireContext(), getString(R.string.rider_must_have_first_name_set), Toast.LENGTH_SHORT).show()
                    }
                }
            })
            autoCompleteCategory.setOnEditorActionListener{_, actionId, keyEvent ->
                if ((keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    riderViewModel.addOrUpdate()
                }
                return@setOnEditorActionListener false
            }

        }
        //For some reason gender spinner sometimes doesnt update
        binding.invalidateAll()



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

    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }

    fun showDeleteDialog(){
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_rider))
                .setMessage(resources.getString(R.string.confirm_delete_rider_message))
                .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                    riderViewModel.delete()
                    findNavController().popBackStack()
                }
                .setNegativeButton(R.string.dismiss){_,_->

                }
                .create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //menu.clear()
        inflater.inflate(R.menu.menu_delete, menu)
    }


}
