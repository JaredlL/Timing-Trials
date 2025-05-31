package com.jaredlinden.timingtrials

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jaredlinden.timingtrials.data.NumberMode
import com.jaredlinden.timingtrials.databinding.ActivityMainBinding
import com.jaredlinden.timingtrials.dialog.ErrorDialog
import com.jaredlinden.timingtrials.functors.Left
import com.jaredlinden.timingtrials.functors.Right
import com.jaredlinden.timingtrials.test.TitleViewModel
import com.jaredlinden.timingtrials.timing.TimingActivity
import com.jaredlinden.timingtrials.util.Event
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.FROM_TIMING_TRIALS
import com.jaredlinden.timingtrials.util.HAS_SHOWN_ONBOARDING
import com.jaredlinden.timingtrials.util.PREF_NUMBERING_MODE
import com.jaredlinden.timingtrials.viewdata.DataBaseViewPagerFragmentDirections
import com.jaredlinden.timingtrials.viewdata.IOViewModel
import com.jaredlinden.timingtrials.viewdata.ListViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.net.URL
import kotlin.math.abs
import androidx.core.content.edit


interface IFabCallbacks{
    fun currentVisibility(): Int

    fun setFabVisibility(visibility: Int)

    fun setFabImage(resourceId: Int)

    val fabClickEvent: MutableLiveData<Event<Boolean>>
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), IFabCallbacks {

    private val ioViewModel:IOViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var rootCoordinator: CoordinatorLayout
    var drawButtonPressed = 0

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


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
                    ioViewModel.readUrlInput(url)
                }catch (e:Exception){
                    displayError("Importing demo data failed", e)
                }
            }
            .show().apply {
                findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
            }
    }

    private val writeAllResults = registerForActivityResult(ActivityResultContracts.CreateDocument("application/tt")){
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
            PreferenceManager.getDefaultSharedPreferences(this).edit() {
                putBoolean(
                    HAS_SHOWN_ONBOARDING,
                    true
                )
            }
        }

        binding.mainFab.setOnClickListener {
            fabClickEvent.postValue(Event(true))
        }

        ioViewModel.importMessage.observe(this, EventObserver {
            when (it){
                is Right -> Snackbar.make(rootCoordinator, it.value, Snackbar.LENGTH_LONG).show()
                is Left -> displayError(it.value.first, it.value.second)
            }
        })

        setSupportActionBar(binding.toolbar)
        binding.navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        rootCoordinator = binding.mainActivityCoordinator

        val vm: TitleViewModel by viewModels ()
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
                    try {
                        importData.launch(arrayOf("*/*"))
                    } catch (e : Exception){
                        displayError("Failed to import data", e)
                    }

                    Snackbar.make(rootCoordinator, "Select CSV or .tt File", Snackbar.LENGTH_SHORT).show()
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
                    try {
                        val date = LocalDateTime.now()
                        val fString = date.format(DateTimeFormatter.ofPattern("dd-MM-yy"))
                        writeAllResults.launch("Timing Trials Export $fString.tt")
                    } catch (e : Exception){
                        displayError("Failed to export data", e)
                    }

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

                intent.data = null
                importData(data)
            }
        }
    }

    private fun importData(uri: Uri){
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val fName = getFileName(uri)
            if(inputStream != null){
                ioViewModel.readInput(fName,uri, inputStream)
            }
        } catch (e: Exception){
            displayError("Failed to import data", e)
        }

    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            if ( cursor!= null && cursor.moveToFirst()) {
                val index =
                    if (cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) < 0)
                    {
                        0
                    }else{
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    }
                result = cursor.getString(index)
            }
            cursor?.close()
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
                ioViewModel.writeAllTimeTrialsToPath(outputStream)
                ioViewModel.allResultsWrittenEvent.observe(this, EventObserver{
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.setDataAndType(uri, "text/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.putExtra(FROM_TIMING_TRIALS, true)
                    startActivity(intent)
                })
            }
        }
        catch(e: Exception)
        {
            displayError("Exporting results failed", e)
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

    private fun displayError(title: String, e: Exception){
        ErrorDialog.display(this@MainActivity, title, e)
    }

    override val fabClickEvent: MutableLiveData<Event<Boolean>> = MutableLiveData()
    override fun setFabImage(resourceId: Int) {
        mainFab?.setImageResource(resourceId)
    }
}


