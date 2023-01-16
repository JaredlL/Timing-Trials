package com.jaredlinden.timingtrials.resultexplorer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.MainActivity
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentSpreadsheetBinding
import com.jaredlinden.timingtrials.domain.csv.CsvSheetWriter
import com.jaredlinden.timingtrials.spreadsheet.SheetAdapter
import com.jaredlinden.timingtrials.spreadsheet.SheetLayoutManager
import com.jaredlinden.timingtrials.util.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class ResultExplorerFragment : Fragment()  {

    val viewModel: ResultExplorerViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val itemTypeId = arguments?.getString("itemTypeId")?:""
        val itemId = arguments?.getLong("itemId")?:0L

        val fabCallback = (requireActivity() as? IFabCallbacks)
        fabCallback?.setFabVisibility(View.GONE)

        setHasOptionsMenu(true)

        val binding = FragmentSpreadsheetBinding.inflate(inflater, container, false)

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)


        val tv: TextView = layoutInflater.inflate(R.layout.list_item_spreadsheet, null).findViewById(R.id.spreadSheetTextView)
        val p = Paint().apply {
            typeface = tv.typeface
            textSize = tv.textSize
        }

        viewModel.setColumnsContext(GlobalResultViewModelData(itemId, itemTypeId, getLengthConverter()), p)
        val adapter = SheetAdapter(requireContext(), displayMetrics, p, ::snackBarCallback)
        val recyclerView = binding.recyclerView

        viewModel.resultSpreadSheet.observe(viewLifecycleOwner) { it?.let {
            if(it.isEmpty){

                recyclerView.visibility = View.INVISIBLE
                adapter.setNewItems(it)
                recyclerView.layoutManager = SheetLayoutManager(it)

            }else{
                binding.emptyTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.setNewItems(it)
                recyclerView.layoutManager = SheetLayoutManager(it)
            }
        } }

        viewModel.navigateToTTId.observe(viewLifecycleOwner, EventObserver {
            val action = ResultExplorerFragmentDirections.actionSheetFragmentToResultFragment(it)
            findNavController().navigate(action)
        })

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        bottomSheetBehavior = BottomSheetBehavior.from(binding.filterSheet)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
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

       if(!PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(HAS_SHOWN_RESULT_EXPLORER_TIPS, false)){
           showTipsDialog()
           PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putBoolean(HAS_SHOWN_RESULT_EXPLORER_TIPS, true).apply()
       }

        return binding.root
    }

    fun snackBarCallback(){
        if(!viewModel.hasShownSnackBar){
            view?.let {
                Snackbar.make(it, R.string.click_again_to_clear, Snackbar.LENGTH_SHORT).show()
                viewModel.hasShownSnackBar = true
            }
        }
    }

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.resultExplorerFilters -> {
                if(bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior?.state == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }else if(bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                }

                true
            }
            R.id.resultExplorerExport -> {
                val count = viewModel.resultSpreadSheet.value?.data?.size?:0
                val s = if(count == 0) " " else " $count "
                permissionRequiredEvent = Event{ createCsvFile.launch("TimingTrials${s}Results Export.csv") }
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                true
            }

            R.id.resultExplorerTips ->{
                showTipsDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetBehavior = null
    }

    fun showTipsDialog(){

        val htmlString =
                """    
    &#8226; ${getString(R.string.tip_click_cell_add_filter)}<br/><br/>
    &#8226; ${getString(R.string.tip_click_cell_remove_filter)}<br/><br/>
    &#8226; ${getString(R.string.tip_click_cell_longpress)}<br/><br/>
    &#8226; ${getString(R.string.tip_click_cell_sort_header)}
    
 """

        val html = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)

        val mColor = ContextCompat.getColor(requireContext(), R.color.secondaryDarkColor)
        val d = ContextCompat.getDrawable(requireActivity (), R.drawable.ic_baseline_help_outline_24)
        Utils.colorDrawable(mColor, d)

        AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.tips))

                .setIcon(d)
                .setMessage(html)
                .setPositiveButton(R.string.ok){_,_->

                }
                .show()
    }

    private val createCsvFile = registerForActivityResult(ActivityResultContracts.CreateDocument()){
        it?.let {
            writeCsv(it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            permissionRequiredEvent.getContentIfNotHandled()?.invoke()
        } else {
            Toast.makeText(requireContext(), "Permission Denied. Allow permissions in android settings.", Toast.LENGTH_LONG).show()
        }
    }
    private var permissionRequiredEvent:Event<() -> Unit> = Event{}

    private fun writeCsv(uri: Uri){

        val vmSheet = viewModel.resultSpreadSheet.value

        vmSheet?.let{sheet->
            try {
                val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                if(outputStream != null){
                    val trans = CsvSheetWriter(sheet)
                    trans.writeToPath(outputStream)


                    var intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.setDataAndType(uri, "text/csv")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    val activities: List<ResolveInfo> = requireActivity().packageManager.queryIntentActivities(
                            intent,
                            PackageManager.MATCH_DEFAULT_ONLY
                    )
                    if(activities.isEmpty()){
                        intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(uri, "text/*")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    }
                    intent.putExtra(FROM_TIMING_TRIALS, true)

                    startActivity(intent)
                }
            }
            catch(e: IOException)
            {
                e.printStackTrace()
                Snackbar.make((requireActivity() as MainActivity).rootCoordinator, "Save failed - ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_results_explorer, menu)
    }
}