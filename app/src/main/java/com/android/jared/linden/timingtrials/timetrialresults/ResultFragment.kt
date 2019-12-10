package com.android.jared.linden.timingtrials.timetrialresults

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.VOLUME_EXTERNAL
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.REQUEST_CREATE_FILE_CSV
import com.android.jared.linden.timingtrials.databinding.FragmentTimetrialResultBinding
import com.android.jared.linden.timingtrials.domain.csv.CsvResultWriter
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_timetrial_result.*
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
        resultGridAdapter = ResultListAdapter(requireActivity())
        resultGridAdapter.setHasStableIds(true)

        requireActivity().invalidateOptionsMenu()
        setHasOptionsMenu(true)

        resultViewModel = requireActivity().getViewModel {  requireActivity().injector.resultViewModel() }.apply { initialise(args.timeTrialId) }

        val binding = DataBindingUtil.inflate<FragmentTimetrialResultBinding>(inflater, R.layout.fragment_timetrial_result, container, false).apply {

            fragResultRecyclerView.isNestedScrollingEnabled = false
            fragResultRecyclerView.layoutManager = viewManager
            fragResultRecyclerView.adapter = resultGridAdapter
            fragResultRecyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL))
            //fragResultRecyclerView.recycledViewPool.setMaxRecycledViews(1,0)
            insertResultsButton.setOnClickListener {
                resultViewModel.insertResults()
            }
        }


        resultViewModel.resultsAreInserted.observe(viewLifecycleOwner, Observer {
            if(it == false){
                binding.insertResultsButton.visibility = View.VISIBLE
            }else{
                binding.insertResultsButton.visibility = View.GONE
            }
        })

        resultViewModel.timeTrial.observe(viewLifecycleOwner, Observer { res->
            res?.let {
                binding.titleText.text = "${it.timeTrialHeader.ttName} ${resources.getString(R.string.results)}"
                binding.courseText.text = "${it.timeTrialHeader.course?.courseName} ${it.timeTrialHeader.course?.length} KM"
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
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Toast.makeText(requireActivity(), "Permission Request", Toast.LENGTH_SHORT).show()
    }

    private fun writeCsv(uri: Uri){

        val tt = resultViewModel.timeTrial.value
        val results = resultViewModel.results.value

        if(tt != null && results != null){
            try {
                val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                if(haveOrRequestFilePermission() && outputStream != null){
                    val trans = CsvResultWriter(tt, results)
                    trans.writeToPath(outputStream)


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
                Toast.makeText(requireActivity(), "Save failed - ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


   fun convertDpToPixels(dp: Float): Int {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics))
    }

    fun takeScreenShot(view: View){
        var toggleButtonVisibility = false
        if (insertResultsButton.visibility == View.VISIBLE) {
            insertResultsButton.visibility = View.GONE
            toggleButtonVisibility = true
        }

        try {


           // val sv = horizontalScrollView
           // val view = sv.getChildAt(0)
            val now = Date()
            val nowChars = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)

            val ttName = resultViewModel.timeTrial.value?.let {
                it.timeTrialHeader.ttName
            }

            val imgName = "${ttName?:nowChars}.jpg"

            val scrollViewWidth = horizontalScrollView.getChildAt(0).width

            val sr = fragResultRecyclerView.computeVerticalScrollRange()

            val gridHeight = if(sr == 0) fragResultRecyclerView.height else sr

            //https://dev.to/pranavpandey/android-create-bitmap-from-a-view-3lck
            view.measure(View.MeasureSpec.makeMeasureSpec(scrollViewWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(gridHeight+200, View.MeasureSpec.EXACTLY))

            view.layout(0,0, view.measuredWidth, view.measuredHeight)
            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)

           if( view.background!=null) {
               view.background.draw(canvas)
            }else{
               canvas.drawColor(Color.WHITE)
           }
            view.draw(canvas)






            when(Build.VERSION.SDK_INT){

                //29
                Build.VERSION_CODES.Q -> saveScreenshotQ(bitmap, imgName)

                //26-28
                in(Build.VERSION_CODES.O..Build.VERSION_CODES.P) -> saveScreenshotO(bitmap, imgName)

                else -> throw Exception("Version Unsupported")

            }



        } catch (e:Throwable) {
            // Several error may come out with file handling or DOM
            throw Exception(e)

        }
        finally {
            if(toggleButtonVisibility){
                insertResultsButton.visibility = View.VISIBLE
            }
        }
    }

    //API 29
    @TargetApi(Build.VERSION_CODES.Q)
    fun saveScreenshotQ(bitmap: Bitmap, imageName:String){

        val filePath = File(MediaStore.VOLUME_EXTERNAL_PRIMARY + imageName)
        val imageOut = FileOutputStream(filePath)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageOut)

        val dets = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.TITLE, imageName)
            put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "Timing Trials")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }

        val data = requireActivity().contentResolver.insert(MediaStore.Images.Media.getContentUri(VOLUME_EXTERNAL), dets)
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

    //API 26
    @TargetApi(Build.VERSION_CODES.O)
    fun saveScreenshotO(bitmap: Bitmap, imageName:String){

        if(haveOrRequestFilePermission()) {
            val path = requireActivity().getExternalFilesDir(null)
            val filePath = File(path, imageName)

            val imageOut = FileOutputStream(filePath)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageOut)

            val contentResolver = requireActivity().contentResolver
            val insertedImageString = MediaStore.Images.Media.insertImage(contentResolver, filePath.path, imageName, "Timing Trials")

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
                put(MediaStore.Images.Media.TITLE, imageName)
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.MediaColumns.DATA, filePath.path)
            }
            requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            openScreenshot(Uri.parse(insertedImageString))

//                val dets = ContentValues().apply {
//                    put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
//                }

//                MediaScannerConnection.scanFile(requireActivity(), arrayOf(filePath.toString()), null) {s,u->
//                    val data = requireActivity().contentResolver.insert(MediaStore.Images.Media.getContentUri(VOLUME_EXTERNAL), dets)
//                    openScreenshot(data)
//                }
//                 val data = requireActivity().contentResolver.insert(MediaStore.Images.Media.getContentUri(VOLUME_EXTERNAL), dets)
//
//                imageOut.flush()
//                imageOut.close()
//                openScreenshot(data)


        }

    }



    private fun openScreenshot(imageFile: Uri?) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(imageFile, "image/*")
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