package com.jaredlinden.timingtrials

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.jaredlinden.timingtrials.timing.TimingActivity
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import com.jaredlinden.timingtrials.viewdata.DataBaseViewPagerFragmentDirections
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.IOException
import kotlin.math.abs


const val REQUEST_CREATE_FILE_CSV = 1
const val REQUEST_IMPORT_FILE = 2
const val REQUEST_CREATE_FILE_SPREADSHEET = 3
const val REQUEST_CREATE_FILE_JSON = 4

interface IFabCallbacks{
    fun setVisibility(visibility: Int)
    fun setAction(action: () -> Unit)
    fun setImage(resourceId: Int)
}


class MainActivity : AppCompatActivity(), IFabCallbacks {


    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    //lateinit var mMainFab: FloatingActionButton
    lateinit var rootCoordinator: CoordinatorLayout

    var drawButtonPressed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val collapsingToolbar = collapsing_toolbar_layout
        val navController = findNavController(R.id.nav_host_fragment)
        //appBarConfiguration = AppBarConfiguration(navController.graph)
         appBarConfiguration = AppBarConfiguration(navController.graph, drawer_layout)
        collapsingToolbar.setupWithNavController(toolbar, navController, appBarConfiguration)

        main_app_bar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset)-(appBarLayout?.totalScrollRange?:0) == 0) {
                //  Collapsed
                toolbarCollapsed = true
                refreshFab()
            } else if(abs(verticalOffset) ==0) {
                //Expanded
                toolbarCollapsed = false
                refreshFab()

            }
        })


        mainFab.setOnClickListener {
            fabAction()
        }

        setSupportActionBar(toolbar)
        nav_view.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        //mMainFab = mainFab
        rootCoordinator = mainActivityCoordinator
        val vm = getViewModel { injector.mainViewModel()}
        vm.timingTimeTrial.observe(this, Observer {
            it?.let {
                val intent = Intent(this, TimingActivity::class.java)
                startActivity(intent)
            }

        })

        nav_view.setNavigationItemSelectedListener {
            drawButtonPressed = it.itemId
            when(it.itemId){

                R.id.app_bar_import -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.type = "*/*"
                    startActivityForResult(intent, REQUEST_IMPORT_FILE)

                    Toast.makeText(this, "Select CSV or .tt File", Toast.LENGTH_LONG).show()
                    drawer_layout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.app_bar_test-> {
//                    val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToTitleFragment()
//                    navController.navigate(action)

                    drawer_layout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.app_bar_settings -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    true

                }
                R.id.app_bar_spreadsheet -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    true

                }
                R.id.app_bar_export->{
                    val date = LocalDateTime.now()
                    val fString = date.format(DateTimeFormatter.ofPattern("dd-MM-yy"))

                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(Intent.EXTRA_TITLE, "Timing Trials Export $fString.tt")
                        //MIME types
                        type = "text/*"
                        // Optionally, specify a URI for the directory that should be opened in
                        // the system file picker before your app creates the document.
                        //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                    }
                    startActivityForResult(intent, REQUEST_CREATE_FILE_JSON)
                    true
                }
                else->{
                    true
                }
            }
        }



        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {

            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerClosed(drawerView: View) {

                when(drawButtonPressed){
                    R.id.app_bar_test->{
                        val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToTitleFragment()
                        navController.navigate(action)
                        drawer_layout.closeDrawer(GravityCompat.START)

                    }
                    R.id.app_bar_settings -> {
                        if(navController.currentDestination?.id == R.id.dataBaseViewPagerFragment){
                            val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSettingsFragment()
                            navController.navigate(action)
                        }else{
                            navController.navigate(R.id.settingsFragment)
                        }



                    }
                    R.id.app_bar_spreadsheet->{
                        val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSheetFragment()
                        navController.navigate(action)
                        drawer_layout.closeDrawer(GravityCompat.START)

                    }
                    else->{

                    }
                }
                drawButtonPressed = 0

            }

            override fun onDrawerOpened(drawerView: View) {

            }

        })

        navController.addOnDestinationChangedListener{_,dest,_->
            when(dest.id){
                R.id.editCourseFragment->{
                    main_app_bar_layout.setExpanded(true)
                }
                R.id.editRiderFragment->{
                    main_app_bar_layout.setExpanded(true)
                }
                R.id.globalResultFragment->{
                    main_app_bar_layout.setExpanded(true)
                }
                R.id.resultFragment->{
                    main_app_bar_layout.setExpanded(true)
                }
            }
        }

       intent?.data?.let {
            importData(it)
        }

    }

    private fun importData(uri: Uri){
        val importVm = getViewModel { injector.importViewModel()}
        val inputStream = contentResolver.openInputStream(uri)
        val fName = getFileName(uri)

        //Toast.makeText(this, b, Toast.LENGTH_SHORT).show()
        if(inputStream != null){
            importVm.readInput(fName, inputStream)
            importVm.importMessage.observe(this, EventObserver {
                Snackbar.make(rootCoordinator, it, Snackbar.LENGTH_LONG).show()
            })
        }
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val mcursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            mcursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            result?.let{
                val cut = it.lastIndexOf('/')
                if (cut != -1) {
                    result = it.substring(cut + 1)
                }
            }
        }
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_IMPORT_FILE ->{
                data?.data?.let {uri->
                    importData(uri)
                }
            }
            REQUEST_CREATE_FILE_JSON->{
                data?.data?.let {uri->
                    try {
                        val outputStream = contentResolver.openOutputStream(uri)
                        if(haveOrRequestFilePermission() && outputStream != null){
                            val allTtsVm = getViewModel { injector.importViewModel()}

                            allTtsVm.writeAllTimeTrialsToPath(outputStream)
                            allTtsVm.importMessage.observe(this, EventObserver{
                                Snackbar.make(rootCoordinator, it, Snackbar.LENGTH_LONG).show()
                                val intent = Intent()
                                intent.action = Intent.ACTION_VIEW
                                intent.setDataAndType(uri, "text/*")
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(intent)
                            })

                        }
                    }
                    catch(e: IOException)
                    {
                        e.printStackTrace()
                        Toast.makeText(this, "Save failed - ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }

    fun haveOrRequestFilePermission(): Boolean{
        return if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//                Toast.makeText(requireActivity(), "Show Rational", Toast.LENGTH_SHORT).show()
//            }else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 3)
            false
            // }
        }else{
            true
        }
    }

    private var toolbarCollapsed = false
    private var fabShouldShow = true
    override fun setVisibility(visibility: Int) {
        fabShouldShow = visibility == View.VISIBLE
        refreshFab()
    }

    private fun refreshFab(){
        if((!toolbarCollapsed && fabShouldShow) && mainFab.visibility != View.VISIBLE){
            mainFab.show()
        }else if ((!fabShouldShow || toolbarCollapsed) && mainFab.visibility == View.VISIBLE) {
            mainFab.hide()
        }
    }

    var fabAction: () -> Unit = {}
    override fun setAction(action: () -> Unit) {
        fabAction = action
    }

    override fun setImage(resourceId: Int) {
        mainFab.setImageResource(resourceId)
    }


}


