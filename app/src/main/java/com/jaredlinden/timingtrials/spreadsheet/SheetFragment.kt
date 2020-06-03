package com.jaredlinden.timingtrials.spreadsheet

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentSpreadsheetBinding
import com.jaredlinden.timingtrials.util.getLengthConverter
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector


class SheetFragment : Fragment()  {

    private val args: SheetFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {


        val fabCallback = (requireActivity() as? IFabCallbacks)
        fabCallback?.setVisibility(View.GONE)

        setHasOptionsMenu(true)

        //val view =  inflater.inflate(R.layout.fragment_spreadsheet, container, false)

        val binding = DataBindingUtil.inflate<FragmentSpreadsheetBinding>(inflater, R.layout.fragment_spreadsheet, container, false)

        val vm = requireActivity().getViewModel { requireActivity().injector.globalResultViewModel() }

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Results"

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val density = displayMetrics.density.toInt()
        val adapter = SheetAdapter(requireContext(), density)
        val recyclerView = binding.recyclerView

        vm.getItemName(args.itemId, args.itemTypeId).observe(viewLifecycleOwner, Observer {
            val s = it?:""
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "$s Results"
        })

        vm.getResultSheet(getLengthConverter()).observe(viewLifecycleOwner, Observer { res->
            res?.let {
                recyclerView.layoutManager = SheetLayoutManager(it)
                adapter.setNewItems(it)
                //recyclerView.invalidate()
            }
        })

//        vm.getRiderResultList(args.itemId, args.itemTypeId).observe(viewLifecycleOwner, Observer {res->
//            res?.let {
//                val resultSheet = if(args.itemTypeId == Rider::class.java.simpleName){
//                    RiderResultListSpreadSheet(it, getLengthConverter())
//                }else{
//                    CourseResultListSpreadSheet(it, getLengthConverter())
//                }
//                recyclerView.layoutManager = SheetLayoutManager(resultSheet)
//                adapter.setNewItems(resultSheet)
//
//            }
//
//        })
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        bottomSheetBehavior = BottomSheetBehavior.from(binding.filterSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN




        return binding.root
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.resultExplorerFilters -> {
                if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }else if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                    val view: View? = requireActivity().currentFocus
                    view?.let { v ->
                        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm?.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //menu.clear()
        inflater.inflate(R.menu.menu_results_explorer, menu)
    }
}