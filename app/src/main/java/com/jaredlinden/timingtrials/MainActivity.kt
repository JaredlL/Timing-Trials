package com.jaredlinden.timingtrials

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.jaredlinden.timingtrials.timing.TimingActivity
import com.jaredlinden.timingtrials.viewdata.DataBaseViewPagerFragmentDirections
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jaredlinden.timingtrials.data.NumberMode
import com.jaredlinden.timingtrials.databinding.ActivityMainBinding
import com.jaredlinden.timingtrials.util.*
import com.jaredlinden.timingtrials.viewdata.IOViewModel
import com.jaredlinden.timingtrials.viewdata.ListViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.IOException
import java.lang.Exception
import java.net.URL
import kotlin.math.abs




interface IFabCallbacks{
    fun currentVisibility(): Int

    fun setFabVisibility(visibility: Int)

    fun setFabImage(resourceId: Int)

    val fabClickEvent: MutableLiveData<Event<Boolean>>
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), IFabCallbacks {


    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var rootCoordinator: CoordinatorLayout

    var drawButtonPressed = 0
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener{ i, _->

        when(i.getString("dayNight", getString(R.string.system_default))){
            getString(R.string.light) -> setDefaultNightMode(MODE_NIGHT_NO)
            getString(R.string.dark) -> setDefaultNightMode(MODE_NIGHT_YES)
            getString(R.string.system_default) -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            getString(R.string.follow_battery_saver_feature) -> setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY)
            else -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }

    }

    fun showDemoDataDialog(){

        val html = HtmlCompat.fromHtml(getString(R.string.demo_data_description_ques), HtmlCompat.FROM_HTML_MODE_LEGACY)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.demo_data))
            .setIcon(R.mipmap.tt_logo_round)
            .setMessage(html)
            .setPositiveButton(R.string.ok){_,_->
                try{
                    val url = URL("https://bbcdn.githack.com/lindenj/timingtrialsdata/raw/master/LiveDebugRDFCC.tt")
                    val vm:IOViewModel by viewModels()
                    vm.readUrlInput(url)
                    vm.importMessage.observe(this, EventObserver{
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    })
                }catch (e:Exception){
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
            }
            .show().apply {
                findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
            }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            permissionRequiredEvent.getContentIfNotHandled()?.invoke()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    private var permissionRequiredEvent:Event<() -> Unit> = Event{}

    private val writeAllResults = registerForActivityResult(ActivityResultContracts.CreateDocument()){
        it?.let {
            writeAllResults(it)
        }
    }

    private val importData = registerForActivityResult(ActivityResultContracts.OpenDocument()){
        it?.let {
            importData(it)
        }
    }

    private lateinit var binding: ActivityMainBinding

    var mainFab : FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val collapsingToolbar = binding.collapsingToolbarLayout
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)
        collapsingToolbar.setupWithNavController(binding.toolbar, navController, appBarConfiguration)

        binding.mainAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset)-(appBarLayout?.totalScrollRange?:0) == 0) {
                //  Collapsed
                toolbarCollapsed = true
                refreshFab()
            } else if(abs(verticalOffset) ==0) {
                //Expanded
                toolbarCollapsed = false
                refreshFab()
            }
        }

        mainFab = binding.mainFab
        val drawer_layout = binding.drawerLayout
        val nav_view = binding.navView
        val main_app_bar_layout = binding.mainAppBarLayout

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener)

        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(HAS_SHOWN_ONBOARDING, false)){
            showDemoDataDialog()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(HAS_SHOWN_ONBOARDING, true).apply()
        }

        binding.mainFab.setOnClickListener {
            fabClickEvent.postValue(Event(true))
        }

        setSupportActionBar(binding.toolbar)
        binding.navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        rootCoordinator = binding.mainActivityCoordinator

        val vm:TitleViewModel by viewModels ()
        vm.timingTimeTrial.observe(this) {
            it?.let {
                val intent = Intent(this, TimingActivity::class.java)
                startActivity(intent)
            }
        }

        if(BuildConfig.DEBUG){
           nav_view.menu.findItem(R.id.app_bar_test).isVisible = true
        }

        nav_view.setNavigationItemSelectedListener {
            drawButtonPressed = it.itemId
            when(it.itemId){

                R.id.app_bar_new_timetrial -> {

                    drawer_layout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.app_bar_import -> {
                    importData.launch(arrayOf("*/*"))
                    Toast.makeText(this, "Select CSV or .tt File", Toast.LENGTH_LONG).show()
                    drawer_layout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.app_bar_test-> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.app_bar_settings -> {
                    drawer_layout.closeDrawer(GravityCompat.START)
                    true

                }
                R.id.app_bar_help -> {
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

                    permissionRequiredEvent = Event{ writeAllResults.launch("Timing Trials Export $fString.tt") }
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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
                    R.id.app_bar_help ->{
                        if(navController.currentDestination?.id == R.id.dataBaseViewPagerFragment){
                            val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToHelpPrefsFragment()
                            navController.navigate(action)
                        }else{
                            navController.navigate(R.id.helpPrefsFragment)
                        }
                    }
                    R.id.app_bar_new_timetrial -> {
                        val viewModel:ListViewModel by viewModels()
                        viewModel.timeTrialInsertedEvent.observe(this@MainActivity, EventObserver {
                            val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSelectCourseFragment2(it)
                            //navController.navigate(action)
                            navController.navigate(R.id.selectCourseFragment, action.arguments)
                        })
                        val mode = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(PREF_NUMBERING_MODE, NumberMode.INDEX.name)?.let {
                            NumberMode.valueOf(it)
                        } ?:NumberMode.INDEX

                        viewModel.insertNewTimeTrial(mode)
                    }
                    R.id.app_bar_spreadsheet->{
                        val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSheetFragment("",0)
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
                R.id.resultFragment->{
                    main_app_bar_layout.setExpanded(true)
                }
                R.id.sheetFragment->{
                    main_app_bar_layout.setExpanded(true)
                }
            }
        }

        intent?.let { intent->
            val data = intent.data

            if(!intent.getBooleanExtra(FROM_TIMING_TRIALS, false) && data != null){
                //TODO probably shouldnt fire this off every rotation
                importData(data)
            }

        }

    }

    private fun importData(uri: Uri){
        val importVm:IOViewModel by viewModels()
        val inputStream = contentResolver.openInputStream(uri)
        val fName = getFileName(uri)

        if(inputStream != null){
            importVm.readInput(fName,uri, inputStream)
            importVm.importMessage.observe(this, EventObserver {
                Snackbar.make(rootCoordinator, it, Snackbar.LENGTH_LONG).show()
            })
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.let {cursor->
                if (cursor.moveToFirst()) {
                    val index = if (cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) < 0)
                    {
                        0
                    }else{
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    }
                    result = cursor.getString(index)
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

    fun writeAllResults(uri: Uri){
        try {
            val outputStream = contentResolver.openOutputStream(uri)
            if(outputStream != null)
            {
                val allTtsVm:IOViewModel by viewModels()
                allTtsVm.writeAllTimeTrialsToPath(outputStream)
                allTtsVm.importMessage.observe(this, EventObserver{
                    Snackbar.make(rootCoordinator, it, Snackbar.LENGTH_LONG).show()
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.setDataAndType(uri, "text/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.putExtra(FROM_TIMING_TRIALS, true)
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

    private var toolbarCollapsed = false
    private var fabShouldShow = true
    override fun setFabVisibility(visibility: Int) {
        fabShouldShow = visibility == View.VISIBLE
    }

    override fun currentVisibility(): Int {
        return  mainFab?.visibility?:View.GONE
    }

    private fun refreshFab(){
        if((!toolbarCollapsed && fabShouldShow) && mainFab?.visibility != View.VISIBLE){
            mainFab?.show()
        }else if ((!fabShouldShow || toolbarCollapsed) && mainFab?.visibility == View.VISIBLE) {
            mainFab?.hide()
        }
    }

    override val fabClickEvent: MutableLiveData<Event<Boolean>> = MutableLiveData()
    override fun setFabImage(resourceId: Int) {
        mainFab?.setImageResource(resourceId)
    }
}


