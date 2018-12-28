package com.android.jared.linden.timingtrials

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.databinding.ActivityEditItemBinding
import com.android.jared.linden.timingtrials.viewmodels.MyViewModelFactory
import com.android.jared.linden.timingtrials.viewmodels.RiderViewModel

import kotlinx.android.synthetic.main.activity_edit_item.*



class EditItemActivity : AppCompatActivity() {

    private lateinit var viewModel: RiderViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityEditItemBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_edit_item)


        var rider = intent?.getParcelableExtra(RIDER_EXTRA) ?: Rider("", "", "", 0)
        viewModel = ViewModelProviders.of(this, MyViewModelFactory(this.application, rider)).get(RiderViewModel::class.java!!)
        binding.viewModel = viewModel
        supportActionBar?.setTitle("Edit Rider")





        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

}
