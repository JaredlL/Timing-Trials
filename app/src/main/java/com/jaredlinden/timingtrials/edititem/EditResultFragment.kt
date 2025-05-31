package com.jaredlinden.timingtrials.edititem

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentEditResultBinding
import com.jaredlinden.timingtrials.setup.SelectRidersFragment
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditResultFragment : Fragment() {

    private val args: EditResultFragmentArgs by navArgs()
    private val resultViewModel : EditResultViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        //Set title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = if(args.resultId == 0L) getString(R.string.add_result) else getString(R.string.edit_result)

        val fabCallback = (requireActivity() as IFabCallbacks)
        fabCallback.setFabImage(R.drawable.ic_done_white_24dp)
        fabCallback.setFabVisibility(View.VISIBLE)

        fabCallback.fabClickEvent.observe(viewLifecycleOwner, EventObserver {
            if(it){
                resultViewModel.save()
            }
        })

        resultViewModel.setResult(args.resultId, args.timeTrialId)
        resultViewModel.changeRider.observe(viewLifecycleOwner, EventObserver{
            if(it){
                val action = EditResultFragmentDirections.actionEditResultFragmentToSelectRidersFragment(SelectRidersFragment.SELECT_RIDER_FRAGMENT_SINGLE)
                findNavController().navigate(action)
            }
        })

        resultViewModel.resultSaved.observe(viewLifecycleOwner,EventObserver{
            if(it){
                hideKeyboard()
                findNavController().popBackStack()
            }
        })

        resultViewModel.deleted.observe(viewLifecycleOwner, EventObserver{
            if(it){
                findNavController().popBackStack()
            }
        })

        val binding = FragmentEditResultBinding.inflate(inflater, container, false).apply {
            viewModel = resultViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        // For some reason gender spinner sometimes doesn't update
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
                .setTitle(resources.getString(R.string.delete_result))
                .setMessage(resources.getString(R.string.confirm_delete_result_message))
                .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                    resultViewModel.delete()
                }
                .setNegativeButton(getString(R.string.dismiss)){_,_->

                }
                .create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //menu.clear()
        inflater.inflate(R.menu.menu_delete, menu)
    }
}
