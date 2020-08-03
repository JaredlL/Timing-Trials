package com.jaredlinden.timingtrials.edititem

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentEditResultBinding
import com.jaredlinden.timingtrials.setup.SelectRidersFragment
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.hideKeyboard
import com.jaredlinden.timingtrials.util.injector

class EditResultFragment : Fragment() {



    private val args: EditResultFragmentArgs by navArgs()

    lateinit var resultViewModel : EditResultViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        resultViewModel = requireActivity().getViewModel { requireActivity().injector.editResultViewModel() }

        setHasOptionsMenu(true)

        //Set title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = if(args.resultId == 0L) getString(R.string.add_result) else getString(R.string.edit_result)


        val fabCallback = (requireActivity() as IFabCallbacks)
        fabCallback.setImage(R.drawable.ic_done_white_24dp)
        fabCallback.setVisibility(View.VISIBLE)

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


        val binding = DataBindingUtil.inflate<FragmentEditResultBinding>(inflater, R.layout.fragment_edit_result, container, false).apply {
            viewModel = resultViewModel
            lifecycleOwner = this@EditResultFragment
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

//class SelectSingleRiderFragment : Fragment(){
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//
//        viewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.selectRidersViewModel
//
//        viewManager = LinearLayoutManager(context)
//        adapter = SelectableRiderListAdapter(requireContext())
//
//        adapter.setHasStableIds(true)
//        adapter.editRider = ::editRider
//
//        (requireActivity() as IFabCallbacks).apply {
//            setVisibility(View.VISIBLE)
//            setImage(R.drawable.ic_add_white_24dp)
//            setAction {
//                editRider(0)
//            }
//        }
//
//        val binding = DataBindingUtil.inflate<FragmentSelectriderListBinding>(inflater, R.layout.fragment_selectrider_list, container, false).apply {
//            lifecycleOwner = (this@SelectRidersFragment)
//            riderHeading.rider =  Rider.createBlank().copy( firstName = "Name", club = "Club")
//            riderHeading.checkBox.visibility =  View.INVISIBLE
//            riderRecyclerView.adapter = adapter
//            riderRecyclerView.layoutManager = viewManager
//
////            riderListFab.setOnClickListener {
////                editRider(0)
////            }
//        }
//
//        adapter.addRiderToSelection = {
//            viewModel.riderSelected(it)
//        }
//        adapter.removeRiderFromSelection = {
//            viewModel.riderUnselected(it)
//        }
//
//        viewModel.selectedRidersInformation.observe(viewLifecycleOwner, Observer {result->
//            result?.let {
//                adapter.setRiders(it)
//            }
//        })
//
//
//
//
//
//        return binding.root
//    }
//
//    private fun editRider(riderId: Long){
//        val action = SetupViewPagerFragmentDirections.actionSetupViewPagerFragment2ToEditRiderFragment(riderId)
//        findNavController().navigate(action)
//    }
//
//}