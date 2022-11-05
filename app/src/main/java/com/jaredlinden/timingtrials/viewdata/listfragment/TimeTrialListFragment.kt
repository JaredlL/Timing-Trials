package com.jaredlinden.timingtrials.viewdata.listfragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.databinding.FragmentListGenericBinding
import com.jaredlinden.timingtrials.databinding.ListItemTimetrialBinding

import com.jaredlinden.timingtrials.util.ConverterUtils
import com.jaredlinden.timingtrials.viewdata.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TimeTrialListFragment : Fragment() {

    private val listViewModel: ListViewModel by viewModels()
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var adapter: GenericListAdapter<TimeTrialHeader>
    private lateinit var viewFactory: GenericViewHolderFactory<TimeTrialHeader>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        Timber.d("Create")

        viewFactory = TimeTrialViewHolderFactory(::longPress)
        adapter = GenericListAdapter(requireContext(), viewFactory)
        listViewModel.filteredAllTimeTrials.observe(viewLifecycleOwner, Observer{res->
            res?.let {adapter.setItems(it.sortedBy { it.status })}
        })

        viewManager = LinearLayoutManager(context)

        val binding = DataBindingUtil.inflate<FragmentListGenericBinding>(inflater, R.layout.fragment_list_generic, container, false).apply{
            lifecycleOwner = (this@TimeTrialListFragment)
            listHeading.addView(viewFactory.createTitle(inflater, container), 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            genericRecyclerView.adapter = adapter
            genericRecyclerView.layoutManager = viewManager

        }

        return binding.root

    }

    override fun onDetach() {
        Timber.d("Detach")
        super.onDetach()
    }

    private fun longPress(header: TimeTrialHeader){
        val msg = if(header.status == TimeTrialStatus.SETTING_UP){
            resources.getString(R.string.confirm_delete_setup_timetrial_message)
        }else{
            resources.getString(R.string.confirm_delete_timetrial_message)
        }


        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_timetrial))
                .setMessage(msg)
                .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                    header.id?.let {
                        listViewModel.deleteTimeTrial(it)
                    }

                }
                .setNegativeButton(getString(R.string.dismiss)){ _, _->

                }
                .create().show()
    }

}

class TimeTrialListViewHolder(binding: ListItemTimetrialBinding): GenericBaseHolder<TimeTrialHeader, ListItemTimetrialBinding>(binding) {
    private val _binding = binding

    var longPress = {header:TimeTrialHeader -> Unit}

    override fun bind(data: TimeTrialHeader){
        _binding.apply{
            viewModel = TimeTrialListItem(data)
            timetrialLayout.setOnClickListener {

                if(data.status == TimeTrialStatus.FINISHED){
                    val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToResultFragment(data.id?:0)
                    Navigation.findNavController(_binding.root).navigate(action)
                }else{
                    val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSetupViewPagerFragment(data.id?:0)
                    Navigation.findNavController(_binding.root).navigate(action)
                }



            }
            timetrialLayout.setOnLongClickListener {
                longPress(data)
                true
            }
            executePendingBindings()

        }
    }
}

data class TimeTrialListItem(val timeTrialHeader: TimeTrialHeader){
    val nameString: String = timeTrialHeader.ttName
    val dateString: String = if(timeTrialHeader.status == TimeTrialStatus.SETTING_UP){
        "Setting Up"
    }else{
        ConverterUtils.dateToDisplay(timeTrialHeader.startTime)
    }
}

class TimeTrialViewHolderFactory(val onLongPress: (tt:TimeTrialHeader) -> Unit): GenericViewHolderFactory<TimeTrialHeader>() {


    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        val binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false).apply {
            textView20.typeface = Typeface.DEFAULT_BOLD
            textView22.typeface = Typeface.DEFAULT_BOLD
        }
        return binding.root
    }

    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: TimeTrialHeader): View {
        val binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false).apply {
            viewModel = TimeTrialListItem(data)
        }
        return binding.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<TimeTrialHeader, ListItemTimetrialBinding> {
        val binding = DataBindingUtil.inflate<ListItemTimetrialBinding>(layoutInflator, R.layout.list_item_timetrial, parent, false)
        return TimeTrialListViewHolder(binding).apply { longPress = onLongPress }
    }
}