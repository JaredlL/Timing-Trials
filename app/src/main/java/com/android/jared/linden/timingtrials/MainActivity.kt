package com.android.jared.linden.timingtrials

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.android.jared.linden.timingtrials.viewdata.DataBaseViewPagerFragmentDirections
import kotlinx.android.synthetic.main.activity_main.*


const val REQUEST_CREATE_FILE_CSV = 1
const val REQUEST_IMPORT_FILE = 2
const val REQUEST_CREATE_FILE_SPREADSHEET = 3
const val REQUEST_CREATE_FILE_JSON = 4

class MainActivity : AppCompatActivity() {


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
                    intent.type = "text/*"
                    startActivityForResult(intent, REQUEST_IMPORT_FILE)

                    Toast.makeText(this, "Select CSV File", Toast.LENGTH_SHORT).show()
                    drawer_layout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.app_bar_test->{
//                    val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToTitleFragment()
//                    navController.navigate(action)

                    drawer_layout.closeDrawer(GravityCompat.START)
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
                    //main_app_bar_layout.setExpanded(true)
                }
            }
        }

    }



}
