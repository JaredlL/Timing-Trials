package com.jaredlinden.timingtrials.timetrialresults

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.VOLUME_EXTERNAL
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jaredlinden.timingtrials.*
import com.jaredlinden.timingtrials.databinding.FragmentTimetrialResultBinding
import com.jaredlinden.timingtrials.domain.JsonResultsWriter
import com.jaredlinden.timingtrials.domain.csv.CsvTimeTrialResultWriter
import com.jaredlinden.timingtrials.util.Utils
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_timetrial_result.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
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
                val action = ResultFragmentDirections.actionResultFragmentToEditResultFragment(id, 0L)
                findNavController().navigate(action)
            }
        }
        resultGridAdapter.setHasStableIds(true)

        setHasOptionsMenu(true)

        (requireActivity() as? IFabCallbacks)?.setVisibility(View.GONE)

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



        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_results, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.resultScreenshot -> {
                view?.let {
                    takeScreenShot(it)
                }
                true
            }

            R.id.resultMenuClearNotes ->{
                resultViewModel.clearNotesColumn()
                true
            }

            R.id.resultMenuCsv ->{
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, "${resultViewModel.timeTrial.value?.timeTrialHeader?.ttName?:"results"}.csv")
                    //MIME types
                    type = "text/csv"
                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker before your app creates the document.
                    //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                }
                startActivityForResult(intent, REQUEST_CREATE_FILE_CSV)
                true
            }
            R.id.resultMenuEditDescription ->{
                val alert = AlertDialog.Builder(requireContext())
                val edittext = EditText(requireContext())

                edittext.setText(resultViewModel.timeTrial.value?.timeTrialHeader?.description?:"")
                alert.setTitle(R.string.edit_description)

                alert.setView(edittext)
                edittext.isSingleLine = false
                edittext.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                edittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                alert.setPositiveButton(R.string.ok) { _, _ ->
                    resultViewModel.updateDescription(edittext.text.toString())

                }

                alert.setNegativeButton(R.string.cancel) { _, _ -> }

                alert.show()
                true
            }
            R.id.resultMenuJson->{
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, "${resultViewModel.timeTrial.value?.timeTrialHeader?.ttName?:"results"}.tt")
                    //MIME types
                    type = "text/*"
                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker before your app creates the document.
                    //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                }
                startActivityForResult(intent, REQUEST_CREATE_FILE_JSON)
                true
            }
            R.id.resultMenuDelete->{
                showDeleteDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CREATE_FILE_CSV->{
                    data?.data?.let {
                        writeCsv(it)
                    }
            }
            REQUEST_CREATE_FILE_JSON->{
                data?.data?.let {
                    writeJson(it)
                }
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Toast.makeText(requireActivity(), "Permission Request", Toast.LENGTH_SHORT).show()
    }

    fun showDeleteDialog(){
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_timetrial))
                .setMessage(resources.getString(R.string.confirm_delete_timetrial_results_message))
                .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                    resultViewModel.delete()
                    findNavController().popBackStack()
                }
                .setNegativeButton("Dismiss"){_,_->

                }
                .create().show()
    }

    private fun writeCsv(uri: Uri){

        val tt = resultViewModel.timeTrial.value
        val results = resultViewModel.results.value

        if(tt != null && results != null){
            try {
                val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                if(haveOrRequestFilePermission() && outputStream != null){
                    val trans = CsvTimeTrialResultWriter(tt, results)
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

    private fun writeJson(uri: Uri){
        val tt = resultViewModel.timeTrial.value
        val results = resultViewModel.results.value

        if(tt != null && results != null){
            try {
                val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                if(haveOrRequestFilePermission() && outputStream != null){

                    JsonResultsWriter().writeToPath(outputStream, tt)

                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.setDataAndType(uri, "text/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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


//   fun convertDpToPixels(dp: Float): Int {
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics).roundToInt()
//    }

    fun takeScreenShot(view: View){

        val oldWidth = view.width
        val oldHeight =  view.height
        try {


           // val sv = horizontalScrollView
           // val view = sv.getChildAt(0)
            val now = Date()
            val nowChars = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)

            val ttName = resultViewModel.timeTrial.value?.timeTrialHeader?.ttName
            
            val imgName = "${ttName?:nowChars}.jpeg"

            val scrollViewWidth = horizontalScrollView.getChildAt(0).width
            //val scrollViewWidth = 200

            val sr = fragResultRecyclerView.computeVerticalScrollRange()

            val gridHeight = if(sr == 0) fragResultRecyclerView.height else sr

            //https://dev.to/pranavpandey/android-create-bitmap-from-a-view-3lck
            view.measure(View.MeasureSpec.makeMeasureSpec(scrollViewWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(gridHeight+300, View.MeasureSpec.EXACTLY))

            view.layout(0,0, view.measuredWidth, view.measuredHeight)
            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)

           if( view.background!=null) {
               view.background.draw(canvas)
            }else{
               canvas.drawColor(Color.WHITE)
           }
            view.draw(canvas)


            val fileName = Utils.createFileName(imgName)

            when(Build.VERSION.SDK_INT){

                //29
                Build.VERSION_CODES.Q -> saveScreenshotQ(bitmap, fileName)
                //26-28
                in(Build.VERSION_CODES.O..Build.VERSION_CODES.P) -> saveScreenshotO(bitmap, fileName)
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

    //API 29
    @TargetApi(Build.VERSION_CODES.Q)
    fun saveScreenshotQ(bitmap: Bitmap, imageName:String){

        val filePath = File(MediaStore.VOLUME_EXTERNAL_PRIMARY + imageName)
        val imageOut = FileOutputStream(filePath)

        bitmap.compress(Bitmap.CompressFormat.PNG, 80, imageOut)

        Timber.d("Created image Filepath API 29 -> $filePath")

        val contentVals = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.TITLE, imageName)
            put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "Timing Trials")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.MediaColumns.DATA, filePath.path)
        }


        val data = requireActivity().contentResolver.insert(MediaStore.Images.Media.getContentUri(VOLUME_EXTERNAL), contentVals)

        Timber.d("Inserted image URI API 29 -> $data")

        imageOut.flush()
        imageOut.close()
        openScreenshot(data)
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

    //API 21-25
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun saveScreenshotN(bitmap: Bitmap, imageName:String){


        val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageName)
        val imageOut = FileOutputStream(filePath)
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, imageOut)

        Timber.d("Created image Filepath API 21-25 -> $filePath")

        val contentVals = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.TITLE, imageName)
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.MediaColumns.DATA, filePath.path)
        }

        val screenshotUri = requireActivity().contentResolver.insert(EXTERNAL_CONTENT_URI, contentVals)

        Timber.d("Inserted image URI API 21-25 -> $screenshotUri")

        imageOut.flush()
        imageOut.close()
        openScreenshot(screenshotUri)
    }

    //API 26-28
    @TargetApi(Build.VERSION_CODES.O)
    fun saveScreenshotO(bitmap: Bitmap, imageName:String){

        if(haveOrRequestFilePermission()) {
            //val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            val path = requireActivity().getExternalFilesDir(null)
            val filePath = File(path, imageName)
            val imageOut = FileOutputStream(filePath)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageOut)


            imageOut.flush()
            imageOut.close()

            Timber.d("Created image Filepath API 26-28 -> $filePath")


            val contentVals = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
                put(MediaStore.Images.Media.TITLE, imageName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.MediaColumns.DATA, filePath.absolutePath)
                put(MediaStore.Images.Media.SIZE, filePath.length())
                //put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            }

            val insertedImageString = requireActivity().contentResolver.insert(EXTERNAL_CONTENT_URI, contentVals)

           //requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            Timber.d("Inserted image URI API 26 -> $insertedImageString")




            openScreenshot(insertedImageString)



        }

    }



    private fun openScreenshot(imageFile: Uri?) {
        val intent = Intent()
        intent.setDataAndType(imageFile, "image/*")
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