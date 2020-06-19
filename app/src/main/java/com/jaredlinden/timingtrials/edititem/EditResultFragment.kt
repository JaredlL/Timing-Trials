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
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.adapters.SelectableRiderListAdapter
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.databinding.FragmentEditResultBinding
import com.jaredlinden.timingtrials.databinding.FragmentSelectriderListBinding
import com.jaredlinden.timingtrials.setup.SelectRidersFragment
import com.jaredlinden.timingtrials.setup.SetupViewPagerFragmentDirections
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
                val action = EditResultFragmentDirections.actionEditResultFragmentToSelectRidersFragment(SelectRidersFragment.SELECT_RIDER_FRAGMENT_SINGLE)
                findNavController().navigate(action)
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