package com.android.jared.linden.timingtrials.globalresults

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.adapters.GenericListItemAdapter
import com.android.jared.linden.timingtrials.data.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.data.ITEM_TYPE_EXTRA
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

import kotlinx.android.synthetic.main.activity_global_result.*

class GlobalResultActivity : AppCompatActivity() {

    private lateinit var genericItemViewModel: GlobalResultViewModel
    private lateinit var adapter: GenericListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_global_result)
        setSupportActionBar(toolbar)

        genericItemViewModel = getViewModel{ injector.globalResultViewModel()}

        viewManager = LinearLayoutManager(this)
        adapter = GenericListItemAdapter(this)

        genericResultRecyclerView.adapter = adapter
        genericResultRecyclerView.layoutManager = viewManager

        genericItemViewModel.init(intent.getStringExtra(ITEM_TYPE_EXTRA), intent.getLongExtra(ITEM_ID_EXTRA, 0))

        genericItemViewModel.resultsToDisplay.observe(this, Observer {items ->
            items?.let {
                adapter.setItems(items)

            }
        })



    }

}
