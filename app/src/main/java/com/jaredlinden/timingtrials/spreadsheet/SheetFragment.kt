package com.jaredlinden.timingtrials.spreadsheet

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.databinding.FragmentSpreadsheetBinding
import com.jaredlinden.timingtrials.resultexplorer.GlobalResultViewModelData
import com.jaredlinden.timingtrials.util.getLengthConverter
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector


class SheetFragment : Fragment()  {

    private val args: SheetFragmentArgs by navArgs()

    override fun onDestroyView() {
        super.onDestroyView()

        //this.arguments?.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {


        val fabCallback = (requireActivity() as? IFabCallbacks)
        fabCallback?.setVisibility(View.GONE)

        setHasOptionsMenu(true)

        //val view =  inflater.inflate(R.layout.fragment_spreadsheet, container, false)

        val binding = DataBindingUtil.inflate<FragmentSpreadsheetBinding>(inflater, R.layout.fragment_spreadsheet, container, false)

        val vm = requireActivity().getViewModel { requireActivity().injector.globalResultViewModel() }

        //(requireActivity() as AppCompatActivity).supportActionBar?.title = "Results"

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)


        val tv: TextView = layoutInflater.inflate(R.layout.list_item_spreadsheet, null).findViewById(R.id.spreadSheetTextView)
        val p = Paint().apply {
            typeface = tv.typeface
            textSize = tv.textSize
        }

        vm.setColumnsContext(GlobalResultViewModelData(args.itemId, args.itemTypeId, getLengthConverter()), p)

        val density = displayMetrics.density.toInt()
        val adapter = SheetAdapter(requireContext(), displayMetrics, p, ::snackBarCallback)
        val recyclerView = binding.recyclerView

        vm.resultSpreadSheet.observe(viewLifecycleOwner, Observer { it?.let {
            adapter.setNewItems(it)
            recyclerView.layoutManager = SheetLayoutManager(it)
        }
        })





//        vm.getResultSheet(getLengthConverter()) {s -> p.measureText(s)}.observe(viewLifecycleOwner, Observer { res->
//            res?.let {
//                adapter.setNewItems(it)
//                recyclerView.layoutManager = SheetLayoutManager(it)
//
//                //recyclerView.invalidate()
//            }
//        })

//        vm.getItemName(args.itemId, args.itemTypeId).observe(viewLifecycleOwner, Observer {
//            it?.let { name ->
//                if(args.itemTypeId == Rider::class.java.simpleName){
//                    vm.setRiderColumnFilter(name)
//                }else{
//                    vm.setCourseColumnFilter(name)
//                }
//            }
//
//        })

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

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            //Hide keyboard when bottom sheet drops
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    val view: View? = requireActivity().currentFocus
                    view?.let { v ->
                        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm?.hideSoftInputFromWindow(v.windowToken, 0)

                    }
                }
            }

        })



        return binding.root
    }

    var hasShownSnackBar = false
    fun snackBarCallback(){
        if(!hasShownSnackBar){
            view?.let {
                Snackbar.make(it, R.string.click_again_to_clear, Snackbar.LENGTH_SHORT).show()
                hasShownSnackBar = true
            }
        }


    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.resultExplorerFilters -> {
                if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }else if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
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