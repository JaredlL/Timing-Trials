package com.android.jared.linden.timingtrials.globalresults

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.adapters.GenericListItemAdapter
import com.android.jared.linden.timingtrials.databinding.FragmentListGenericBinding
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

class GlobalResultFragment : Fragment()
{

    private val args: GlobalResultFragmentArgs by navArgs()
    private lateinit var genericItemViewModel: GlobalResultViewModel
    private lateinit var adapter: GenericListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        genericItemViewModel = requireActivity().getViewModel{ requireActivity().injector.globalResultViewModel()}

        viewManager = LinearLayoutManager(requireActivity())
        adapter = GenericListItemAdapter(requireActivity())

        val binding = DataBindingUtil.inflate<FragmentListGenericBinding>(inflater, R.layout.fragment_list_generic, container, false).apply{
            lifecycleOwner = this@GlobalResultFragment
            genericRecyclerView.adapter = adapter
            genericRecyclerView.layoutManager = viewManager
        }

        genericItemViewModel.resultsToDisplay.observe(viewLifecycleOwner, Observer {res->
            res?.let {
                adapter.setItems(it)
            }
        })

        genericItemViewModel.titleString.observe(this, Observer {item ->
            item?.let {

            }
        })

        genericItemViewModel.init(args.itemTypeId, args.itemId)



        return binding.root
    }
}