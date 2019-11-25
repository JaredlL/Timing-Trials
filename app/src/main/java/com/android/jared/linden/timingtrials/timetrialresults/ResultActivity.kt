//package com.android.jared.linden.timingtrials.timetrialresults
//
//import android.annotation.SuppressLint
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import androidx.lifecycle.Observer
//import androidx.recyclerview.widget.GridLayoutManager
//import com.android.jared.linden.timingtrials.data.ITEM_ID_EXTRA
//import com.android.jared.linden.timingtrials.util.argument
//import com.android.jared.linden.timingtrials.util.getViewModel
//import com.android.jared.linden.timingtrials.util.injector
//import kotlinx.android.synthetic.main.activity_result.*
//import androidx.recyclerview.widget.RecyclerView
//import android.graphics.Rect
//import android.graphics.drawable.Drawable
//import androidx.recyclerview.widget.LinearLayoutManager
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import java.io.File
//import java.io.FileOutputStream
//import java.util.*
//import android.content.Intent
//import android.net.Uri
//import android.util.AttributeSet
//import android.view.View
//import android.view.Menu
//import android.view.MenuItem
//import android.view.MotionEvent
//import android.widget.Toast
//import com.android.jared.linden.timingtrials.R
//
//
//class ResultActivity : AppCompatActivity() {
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_result)
//
////        viewResultsButton.setOnClickListener {
////            val v = resultRecyclerView
////            takeScreenShot(v)
////        }
//
//        //toolbar.inflateMenu(R.menu.menu_results)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayShowTitleEnabled(true)
//        supportActionBar?.title = "Results"
//
//        val timeTrialId by argument<Long>(ITEM_ID_EXTRA)
//        val resultViewModel = getViewModel { injector.resultViewModel() }.apply { initialise(timeTrialId) }
//
//        val viewManager = GridLayoutManager(this, 2)
//
//        val adapter = ResultListAdapter(this)
//
//        adapter.setHasStableIds(true)
//
//        resultViewModel.results.observe(this, Observer {res->
//            res?.let {newRes->
//                if(newRes.isNotEmpty()){
//                    val rowLength = newRes.first().row.size
//
//                    viewManager.spanCount = rowLength + 2
//                    viewManager.spanSizeLookup = (object : GridLayoutManager.SpanSizeLookup(){
//                        override fun getSpanSize(position: Int): Int {
//                            return if (position.rem(rowLength) == 0 || position.rem(rowLength) == 2) {
//                                2
//                            }else {
//                                1
//                            }
//
//                        }
//                    })
//                    adapter.setResults(newRes)
//                }
//
//            }
//        })
//
//        resultViewModel.timeTrial.observe(this, Observer {
//            it?.let { tt->
//
//                supportActionBar?.title= "${tt.timeTrialHeader.ttName} Results"
//            }
//        })
//
//
//        resultRecyclerView.isNestedScrollingEnabled = false
//        resultRecyclerView.layoutManager = viewManager
//        resultRecyclerView.adapter = adapter
//        resultRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
//
//        }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
//        // If you don't have res/menu, just create a directory named "menu" inside res
//        menuInflater.inflate(R.menu.menu_results, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    // handle button activities
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//
//        if (item.itemId == R.id.doneButton) {
//            val resultViewModel = getViewModel { injector.resultViewModel() }
//            resultViewModel.insertResults()
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    fun takeScreenShot(view: View){
//        try {
//            val now = Date()
//            android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
//            // image naming and path  to include sd card  appending name you choose for file
//            val mPath = getApplicationInfo().dataDir + "/" + now + ".jpg"
//
//            // create bitmap screen capture
//            //val v1 = getWindow().getDecorView().getRootView()
//
//            val bitmap = Bitmap.createBitmap(view.getWidth(),
//                    view.getHeight(), Bitmap.Config.ARGB_8888)
//            val canvas = Canvas(bitmap)
//            view.draw(canvas)
//
//            val imageFile = File(mPath)
//
//            val outputStream = FileOutputStream(imageFile)
//            val quality = 100
//            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
//            outputStream.flush()
//            outputStream.close()
//
//            openScreenshot(imageFile)
//        } catch (e:Throwable) {
//            // Several error may come out with file handling or DOM
//            e.printStackTrace()
//        }
//    }
//
//    private fun openScreenshot(imageFile: File) {
//        val intent = Intent()
//        intent.action = Intent.ACTION_VIEW
//        val uri = Uri.fromFile(imageFile)
//        intent.setDataAndType(uri, "image/*")
//        startActivity(intent)
//    }
//
//    }
//
//
//
//
