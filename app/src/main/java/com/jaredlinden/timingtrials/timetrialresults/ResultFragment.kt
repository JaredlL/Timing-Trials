package com.jaredlinden.timingtrials.timetrialresults

import android.Manifest
import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY
import android.text.InputType
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.MainActivity
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentTimetrialResultBinding
import com.jaredlinden.timingtrials.domain.JsonResultsWriter
import com.jaredlinden.timingtrials.domain.csv.CsvTimeTrialResultWriter
import com.jaredlinden.timingtrials.util.*
import kotlinx.android.synthetic.main.fragment_timetrial_result.*
import timber.log.Timber
import java.io.IOException
import java.util.*


class ResultFragment : Fragment() {

    private val args: ResultFragmentArgs by navArgs()

    lateinit var resultViewModel: ResultViewModel
    lateinit var viewManager: GridLayoutManager
    lateinit var resultGridAdapter: ResultListAdapter







    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {

        viewManager = GridLayoutManager(requireActivity(), 2)



        resultGridAdapter = ResultListAdapter(requireActivity()) { id->
            id?.let {
                val action = ResultFragmentDirections.actionResultFragmentToEditResultFragment(id, resultViewModel.timeTrial.value?.timeTrialHeader?.id?:0L)
                findNavController().navigate(action)
            }
        }
        resultGridAdapter.setHasStableIds(true)

        setHasOptionsMenu(true)

        (requireActivity() as? IFabCallbacks)?.setFabVisibility(View.GONE)

        resultViewModel = requireActivity().getViewModel {  requireActivity().injector.resultViewModel() }.apply { changeTimeTrial(args.timeTrialId) }

        val binding = DataBindingUtil.inflate<FragmentTimetrialResultBinding>(inflater, R.layout.fragment_timetrial_result, container, false).apply {

            fragResultRecyclerView.setHasFixedSize(true)
            fragResultRecyclerView.isNestedScrollingEnabled = false
            fragResultRecyclerView.layoutManager = viewManager
            fragResultRecyclerView.adapter = resultGridAdapter
            fragResultRecyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL))

        }


        resultViewModel.timeTrial.observe(viewLifecycleOwner, Observer { res->
            res?.let {
                binding.titleText.text = it.timeTrialHeader.ttName
                if(res.timeTrialHeader.description.isBlank()){
                    binding.resultNotesTextView.visibility = View.GONE
                }else{
                    binding.resultNotesTextView.visibility = View.VISIBLE
                    binding.resultNotesTextView.text = res.timeTrialHeader.description
                }
            }
        })


        resultViewModel.results.observe(viewLifecycleOwner, Observer {res->
            res?.let {newRes->
                if(newRes.isNotEmpty()){
                    val rowLength = newRes.first().row.size
                    viewManager.spanCount = rowLength + 2
                    viewManager.spanSizeLookup = (object : GridLayoutManager.SpanSizeLookup(){
                        override fun getSpanSize(position: Int): Int {
                            return if (position.rem(rowLength) == 0 || position.rem(rowLength) == 2) {
                                2
                            }else {
                                1
                            }
                        }
                    })
                    resultGridAdapter.setResults(newRes)
                }

            }
        })


        if(!PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(HAS_SHOWN_TIMETRIAL_RESULT_TIPS, false)){
            showTipsDialog()
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putBoolean(HAS_SHOWN_TIMETRIAL_RESULT_TIPS, true).apply()
        }

        return binding.root
    }

    fun showTipsDialog(){

        val mColor = ContextCompat.getColor(requireContext(), R.color.secondaryDarkColor)
        val d = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_baseline_help_outline_24)
        Utils.colorDrawable(mColor, d)

        AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.tips))
                .setIcon(d)
                .setMessage(R.string.tip_longpress_row_to_edit)
                .setPositiveButton(R.string.ok){_,_->

                }
                .show()
    }


    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
            //Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            permissionRequiredEvent.getContentIfNotHandled()?.invoke()
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
            Toast.makeText(requireContext(), "Permission Denied. Allow permissions in android settings.", Toast.LENGTH_LONG).show()
        }
    }

    var permissionRequiredEvent:Event<() -> Unit> = Event{}

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_results, menu)
    }

    val createCsvFile = registerForActivityResult(ActivityResultContracts.CreateDocument()){
        it?.let {
            writeCsv(it)
        }
    }

    val createJsonFile = registerForActivityResult(ActivityResultContracts.CreateDocument()){
        it?.let {
            writeJson(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.resultScreenshot -> {
                permissionRequiredEvent = Event{view?.let {
                    takeScreenShot(it)
                }?:Unit}
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                true
            }

            R.id.resultMenuClearNotes ->{
                resultViewModel.clearNotesColumn()
                true
            }
            R.id.resultMenuEditDescription ->{
                val alert = AlertDialog.Builder(requireContext())
                val edittext = EditText(requireContext())

                edittext.setText(resultViewModel.timeTrial.value?.timeTrialHeader?.description?:"")
                alert.setTitle(R.string.edit_description)

                alert.setView(edittext)
                edittext.isSingleLine = false
                edittext.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                edittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                alert.setPositiveButton(R.string.ok) { _, _ ->
                    resultViewModel.updateDescription(edittext.text.toString())

                }

                alert.setNegativeButton(R.string.cancel) { _, _ -> }

                alert.show()
                true
            }

            R.id.resultMenuAddRow ->{
                resultViewModel.timeTrial.value?.timeTrialHeader?.id?.let {
                    val action = ResultFragmentDirections.actionResultFragmentToEditResultFragment(0L, it)
                    findNavController().navigate(action)
                }
                true
            }

            R.id.resultMenuExport->{

                AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.choose_export_type))
                        .setIcon(R.mipmap.tt_logo_round)
                        //.setMessage(R.string.export_file_description)
                        .setItems(R.array.exportTypes){_, i ->
                            when(resources.getStringArray(R.array.exportTypes)[i]){
                               getString(R.string.tt_file) ->{
                                   permissionRequiredEvent = Event{ createJsonFile.launch("${resultViewModel.timeTrial.value?.timeTrialHeader?.ttName?:"results"}.tt") }
                                   requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                               }
                                getString(R.string.csv_file) ->{
                                    permissionRequiredEvent = Event{ createCsvFile.launch("${resultViewModel.timeTrial.value?.timeTrialHeader?.ttName?:"results"}.csv") }
                                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            }
                        }

                        .setNegativeButton(R.string.cancel) { _, _ ->

                        }.show()


                true
            }

            R.id.resultMenuTips->{
                showTipsDialog()
                true
            }

            R.id.resultMenuDelete->{
                showDeleteDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    fun showDeleteDialog(){
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_timetrial))
                .setMessage(resources.getString(R.string.confirm_delete_timetrial_results_message))
                .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                    resultViewModel.delete()
                    findNavController().popBackStack()
                }
                .setNegativeButton(resources.getString(R.string.dismiss)){_,_->

                }
                .create().show()
    }

    private fun writeCsv(uri: Uri){

        val tt = resultViewModel.timeTrial.value
        val results = resultViewModel.results.value

        if(tt != null && results != null){
            try {
                val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                if(outputStream != null){
                    val modified = results.mapIndexed {i,x -> x.row.mapIndexed { j, y -> if(i ==0 && j == 0) ">>" + (y.content.value?:"") else y.content.value?:"" } }
                    val trans = CsvTimeTrialResultWriter(tt, modified, getLengthConverter())
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

    private fun writeJson(uri: Uri){
        val tt = resultViewModel.timeTrial.value
        val results = resultViewModel.results.value

        if(tt != null && results != null){
            try {
                val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                if(outputStream != null){

                    JsonResultsWriter().writeToPath(outputStream, tt)

                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.setDataAndType(uri, "text/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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


    fun dpToPixels(dip:Int): Int{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip.toFloat(), resources.displayMetrics).toInt()
    }

    fun takeScreenShot(view: View){

        val oldWidth = view.width
        val oldHeight =  view.height
        try {


           // val sv = horizontalScrollView
           // val view = sv.getChildAt(0)
            val now = Date()
            val nowChars = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)

            val ttName = resultViewModel.timeTrial.value?.timeTrialHeader?.ttName
            
            val imgName = "${ttName?:nowChars}.png"



            val scrollViewWidth = horizontalScrollView.getChildAt(0).width
            //val scrollViewWidth = 200

            val sr = fragResultRecyclerView.computeVerticalScrollRange()

            val gridHeight = if(sr == 0) fragResultRecyclerView.height else sr

            //https://dev.to/pranavpandey/android-create-bitmap-from-a-view-3lck
            view.measure(View.MeasureSpec.makeMeasureSpec(scrollViewWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(gridHeight+dpToPixels(300), View.MeasureSpec.EXACTLY))

            view.layout(0,0, view.measuredWidth, view.measuredHeight)

            val resutWaterMarkTextView = view.findViewById<TextView>(R.id.resutWaterMarkTextView)
            //resutWaterMarkTextView.measure(View.MeasureSpec.makeMeasureSpec(, View.MeasureSpec.EXACTLY), View.MeasureSpec.EXACTLY)
            val tv1Height = resutWaterMarkTextView.measuredHeight

            val titleText = view.findViewById<TextView>(R.id.titleText)
            //titleText.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY)
            val titleTextHeight = titleText.measuredHeight

            val resultNotesTextView = view.findViewById<TextView>(R.id.resultNotesTextView)
            //resultNotesTextView.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY)
            val notesHeight = resultNotesTextView.measuredHeight

            val sum = tv1Height + titleTextHeight + notesHeight + dpToPixels(50)

            view.measure(View.MeasureSpec.makeMeasureSpec(scrollViewWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(gridHeight+sum, View.MeasureSpec.EXACTLY))

            view.layout(0,0, view.measuredWidth, view.measuredHeight)

            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)

           if( view.background!=null) {
               view.background.draw(canvas)
            }else{
               val typedValue = TypedValue()
               val theme = requireContext().theme
               theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
               val color = typedValue.data
               canvas.drawColor(color)
           }
            view.draw(canvas)


            val fileName = Utils.createFileName(imgName)

            when(Build.VERSION.SDK_INT){

                //29-30
                in(Build.VERSION_CODES.Q..Build.VERSION_CODES.R) ->
                    saveScreenshotQ(bitmap, fileName)
                //26-28
                in(Build.VERSION_CODES.O..Build.VERSION_CODES.P) ->
                    saveScreenshotO(bitmap, fileName)
                //21-25
                in(Build.VERSION_CODES.LOLLIPOP..Build.VERSION_CODES.N) -> saveScreenshotN(bitmap, fileName)

                else -> throw Exception("Version Unsupported")

            }



        } catch (e:Throwable) {
            // Several error may come out with file handling or DOM
            throw Exception(e)

        }
        finally {

            view.measure(View.MeasureSpec.makeMeasureSpec(oldWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(oldHeight, View.MeasureSpec.EXACTLY))

            view.layout(0,0, view.measuredWidth, view.measuredHeight)
        }
    }

    //API 29-30
    @TargetApi(Build.VERSION_CODES.R)
    fun saveScreenshotQ(bitmap: Bitmap, imageName:String){

        val cr = requireActivity().contentResolver

        val contentVals = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.TITLE, imageName)
            put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "Timing Trials")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }


        val data = cr.insert(MediaStore.Images.Media.getContentUri(VOLUME_EXTERNAL_PRIMARY), contentVals)

        data?.let {
            cr.openOutputStream(data)?.let {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)

                Timber.d("Created image Filepath API 29-30 -> ${data.path}")

                it.flush()
                it.close()
            }

        }



        Timber.d("Inserted image URI API 29-30 -> $data")
        openScreenshot(data)
    }


    //API 21-25
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun saveScreenshotN(bitmap: Bitmap, imageName:String){

        val cr = requireActivity().contentResolver

        val contentVals = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.TITLE, imageName)
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        }

        val data = requireActivity().contentResolver.insert(EXTERNAL_CONTENT_URI, contentVals)

        data?.let {
            cr.openOutputStream(data)?.let {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)

                Timber.d("Created image Filepath API 21-25 -> ${data.path}")

                it.flush()
                it.close()
            }

        }
        openScreenshot(data)
    }

    //API 26-28
    @TargetApi(Build.VERSION_CODES.O)
    fun saveScreenshotO(bitmap: Bitmap, imageName:String){

        val cr = requireActivity().contentResolver

        //Dont divide milis by 1000!
            val contentVals = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
                put(MediaStore.Images.Media.TITLE, imageName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                //put(MediaStore.MediaColumns.DATA, filePath.absolutePath)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            }

        val data = cr.insert(EXTERNAL_CONTENT_URI, contentVals)

        data?.let {
            cr.openOutputStream(data)?.let {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)

                Timber.d("Created image Filepath API 26 -> ${data.path}")

                it.flush()
                it.close()
            }
            //refreshGallery(it)
        }


            Timber.d("Inserted image URI API 26 -> ${data?.path}")
            openScreenshot(data)
    }




    private fun openScreenshot(imageFile: Uri?) {
        val intent = Intent()
        intent.setDataAndType(imageFile, "image/png")
        intent.action = Intent.ACTION_VIEW
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        Timber.d("Request open  ${imageFile?.path}")
        startActivity(intent)
    }

}


class DividerItemDecoration(context: Context, orientation: Int) : RecyclerView.ItemDecoration() {

    private val mDivider: Drawable?

    private var mOrientation: Int = 0

    init {
        val a = context.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
        setOrientation(orientation)
    }

    fun setOrientation(orientation: Int) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw IllegalArgumentException("invalid orientation")
        }
        mOrientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    fun drawVertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val right = left + mDivider!!.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider!!.intrinsicHeight)
        } else {
            outRect.set(0, 0, mDivider!!.intrinsicWidth, 0)
        }
    }

    companion object {

        private val ATTRS = intArrayOf(android.R.attr.listDivider)

        val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL

        val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }
}