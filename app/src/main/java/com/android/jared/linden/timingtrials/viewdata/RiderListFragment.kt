package com.android.jared.linden.timingtrials.viewdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.databinding.FragmentListGenericBinding
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

class RiderListFragment : Fragment(){

    private lateinit var listViewModel: ListViewModel
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var adapter: GenericListAdapter<Rider>
    private lateinit var viewFactory: GenericViewHolderFactory<Rider>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }
        viewFactory = RiderViewHolderFactory()
        adapter = GenericListAdapter(requireContext(), viewFactory)
        viewManager = LinearLayoutManager(context)

        listViewModel.filteredAllRiders.observe(viewLifecycleOwner, Observer{res->
            res?.let {adapter.setItems(it)}
        })

        val binding = DataBindingUtil.inflate<FragmentListGenericBinding>(inflater, R.layout.fragment_list_generic, container, false).apply{
            lifecycleOwner = (this@RiderListFragment)
            listHeading.addView(viewFactory.createTitle(inflater, container), 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            genericRecyclerView.adapter = adapter
            genericRecyclerView.layoutManager = viewManager
//            viewFactory.performFabAction(genericListFab)

        }

        return binding.root

    }


}