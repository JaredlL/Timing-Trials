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
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.android.jared.linden.timingtrials.viewdata.DataBaseViewPagerFragmentDirections
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_main.*
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_IMPORT_FILE ->{
                data?.data?.let {uri->
                    //  try {
                    val importVm = getViewModel { injector.importViewModel()}
                    val inputStream = contentResolver.openInputStream(uri)
                    if(inputStream != null){
                        importVm.readInput(uri.path, inputStream)
                    }
                    //         }
//                    catch(e: IOException)
//                    {
//                        e.printStackTrace()
//                        Toast.makeText(requireActivity(), "Save failed - ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
                }
            }
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

//class FAB_Hide_on_Scroll(context: Context?, attrs: AttributeSet?) : FloatingActionButton.Behavior() {
//    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
//        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
//        //child -> Floating Action Button
//        if (dyConsumed > 0 && child.visibility == View.VISIBLE) {
//            child.hide(object : OnVisibilityChangedListener() {
//
//                override fun onHidden(fab: FloatingActionButton) {
//                    super.onShown(fab)
//                    //fab.visibility = View.INVISIBLE
//                }
//            })
//
//        } else if (dyConsumed < 0 && child.visibility != View.VISIBLE) {
//            //child.show()
//        }
//    }
//
//    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
//        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
//    }
//
//
//}
