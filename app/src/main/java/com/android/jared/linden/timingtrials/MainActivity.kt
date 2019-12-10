package com.android.jared.linden.timingtrials

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.android.jared.linden.timingtrials.timetrialresults.ResultFragment
import kotlinx.android.synthetic.main.activity_main.*


const val REQUEST_CREATE_FILE_CSV = 1
const val REQUEST_IMPORT_FILE = 2
const val REQUEST_CREATE_FILE_SPREADSHEET = 3
const val REQUEST_CREATE_FILE_JSON = 4


class MainActivity : AppCompatActivity() {


    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }



}
