package com.android.jared.linden.timingtrials.timetrialresults

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentTimetrialResultBinding
import com.android.jared.linden.timingtrials.edititem.EditRiderFragmentArgs
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_timetrial_result.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ResultFragment : Fragment() {

    private val args: ResultFragmentArgs by navArgs()

    lateinit var resultViewModel: ResultViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {

        val viewManager = GridLayoutManager(requireActivity(), 2)
        val adapter = ResultListAdapter(requireActivity())
        adapter.setHasStableIds(true)

        requireActivity().invalidateOptionsMenu()
        setHasOptionsMenu(true)

        resultViewModel = requireActivity().getViewModel {  requireActivity().injector.resultViewModel() }.apply { initialise(args.timeTrialId) }

        val binding = DataBindingUtil.inflate<FragmentTimetrialResultBinding>(inflater, R.layout.fragment_timetrial_result, container, false).apply {

            fragResultRecyclerView.isNestedScrollingEnabled = false
            fragResultRecyclerView.layoutManager = viewManager
            fragResultRecyclerView.adapter = adapter
            fragResultRecyclerView.addItemDecoration(DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL))
        }







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
                    adapter.setResults(newRes)
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
            R.id.doneButton -> {
                resultViewModel.insertResults()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun takeScreenShot(view: View){
        try {
            val now = Date()
            android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
            // image naming and path  to include sd card  appending name you choose for file
            val mPath = requireActivity().applicationInfo.dataDir + "/" + now + ".jpg"

            // create bitmap screen capture
            //val v1 = getWindow().getDecorView().getRootView()

            val bitmap = Bitmap.createBitmap(view.width,
                    view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            val imageFile = File(mPath)

            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            openScreenshot(imageFile)
        } catch (e:Throwable) {
            // Several error may come out with file handling or DOM
            e.printStackTrace()
        }
    }

    private fun openScreenshot(imageFile: File) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val uri = Uri.fromFile(imageFile)
        intent.setDataAndType(uri, "image/*")
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