package com.jaredlinden.timingtrials.spreadsheet

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.jaredlinden.timingtrials.*
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.databinding.FragmentSpreadsheetBinding
import com.jaredlinden.timingtrials.domain.csv.CsvSheetWriter
import com.jaredlinden.timingtrials.domain.csv.CsvTimeTrialResultWriter
import com.jaredlinden.timingtrials.resultexplorer.GlobalResultViewModelData
import com.jaredlinden.timingtrials.util.getLengthConverter
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import java.io.IOException


class SheetFragment : Fragment()  {

    //private val args: SheetFragmentArgs by navArgs()

    override fun onDestroyView() {
        super.onDestroyView()

        //this.arguments?.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val itemTypeId = arguments?.getString("itemTypeId")?:""
        val itemId = arguments?.getLong("itemId")?:0L

        arguments?.remove("itemId")
        arguments?.remove("itemTypeId")

        val fabCallback = (requireActivity() as? IFabCallbacks)
        fabCallback?.setVisibility(View.GONE)

        setHasOptionsMenu(true)

        //val view =  inflater.inflate(R.layout.fragment_spreadsheet, container, false)

        val binding = DataBindingUtil.inflate<FragmentSpreadsheetBinding>(inflater, R.layout.fragment_spreadsheet, container, false)

        val vm = requireActivity().getViewModel { requireActivity().injector.globalResultViewModel() }


        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)


        val tv: TextView = layoutInflater.inflate(R.layout.list_item_spreadsheet, null).findViewById(R.id.spreadSheetTextView)
        val p = Paint().apply {
            typeface = tv.typeface
            textSize = tv.textSize
        }

        vm.setColumnsContext(GlobalResultViewModelData(itemId, itemTypeId, getLengthConverter()), p)

        val density = displayMetrics.density.toInt()
        val adapter = SheetAdapter(requireContext(), displayMetrics, p, ::snackBarCallback)
        val recyclerView = binding.recyclerView

        vm.resultSpreadSheet.observe(viewLifecycleOwner, Observer { it?.let {
            adapter.setNewItems(it)
            recyclerView.layoutManager = SheetLayoutManager(it)
        }
        })

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
            R.id.resultExplorerExport -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    val count = requireActivity().getViewModel { requireActivity().injector.globalResultViewModel() }.resultSpreadSheet.value?.data?.size?:0
                    val s = if(count == 0) " " else " $count "
                    putExtra(Intent.EXTRA_TITLE, "TimingTrials${s}Results Export.csv")
                    //MIME types
                    type = "text/csv"
                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker before your app creates the document.
                    //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                }
                startActivityForResult(intent, REQUEST_EXPLORER_CREATE_FILE_CSV)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_EXPLORER_CREATE_FILE_CSV->{
                data?.data?.let {
                    writeCsv(it)
                }
            }
        }
    }

    private fun writeCsv(uri: Uri){

        val vmSheet = requireActivity().getViewModel { requireActivity().injector.globalResultViewModel() }.resultSpreadSheet.value

        vmSheet?.let{sheet->
            try {
                val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                if(haveOrRequestFilePermission() && outputStream != null){
                    val trans = CsvSheetWriter(sheet)
                    trans.writeToPath(outputStream)


                    var intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.setDataAndType(uri, "text/csv")
                    //intent.data = uri
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    val activities: List<ResolveInfo> = requireActivity().packageManager.queryIntentActivities(
                            intent,
                            PackageManager.MATCH_DEFAULT_ONLY
                    )
                    if(activities.isEmpty()){
                        intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(uri, "text/*")
                        //intent.data = uri
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

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

    fun haveOrRequestFilePermission(): Boolean{
        return if(ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//                Toast.makeText(requireActivity(), "Show Rational", Toast.LENGTH_SHORT).show()
//            }else{
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 3)
            false
            // }
        }else{
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //menu.clear()
        inflater.inflate(R.menu.menu_results_explorer, menu)
    }
}