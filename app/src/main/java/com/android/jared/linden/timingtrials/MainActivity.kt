package com.android.jared.linden.timingtrials

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
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
