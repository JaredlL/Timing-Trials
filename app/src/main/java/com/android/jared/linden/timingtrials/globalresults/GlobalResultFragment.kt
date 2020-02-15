package com.android.jared.linden.timingtrials.globalresults

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.IFabCallbacks
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.adapters.GenericListItemAdapter
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.databinding.FragmentGlobalResultBinding
import com.android.jared.linden.timingtrials.databinding.FragmentListGenericBinding
import com.android.jared.linden.timingtrials.domain.GlobalResultData
import com.android.jared.linden.timingtrials.ui.GenericListItemNext
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_list_generic.*
import java.lang.Exception

class GlobalResultFragment : Fragment() {

    private val args: GlobalResultFragmentArgs by navArgs()
    private lateinit var genericItemViewModel: GlobalResultViewModel
    private lateinit var adapter: GenericListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        genericItemViewModel = requireActivity().getViewModel { requireActivity().injector.globalResultViewModel() }

        viewManager = LinearLayoutManager(requireActivity())
        adapter = GenericListItemAdapter(requireActivity())

        val fabCallback = (requireActivity() as? IFabCallbacks)
        fabCallback?.setVisibility(View.GONE)

        val binding = DataBindingUtil.inflate<FragmentGlobalResultBinding>(inflater, R.layout.fragment_global_result, container, false).apply {
            lifecycleOwner = this@GlobalResultFragment

            genericRecyclerView.adapter = adapter
            genericRecyclerView.layoutManager = viewManager
        }

        genericItemViewModel.typeIdLiveData.observe(viewLifecycleOwner, object : Observer<GenericListItemNext> {
            override fun onChanged(t: GenericListItemNext?) {
                val b = t
            }
        })

        genericItemViewModel.resMed.observe(viewLifecycleOwner, object : Observer<GlobalResultData> {
            override fun onChanged(data: GlobalResultData?) {
                data?.let {
                    (requireActivity() as AppCompatActivity).supportActionBar?.title = data.title
                    binding.listHeading.item = data.resultHeading
                    adapter.setItems(data.resultsList)
                }

            }
        })






        genericItemViewModel.setTypeIdData(GenericListItemNext(args.itemTypeId, args.itemId))

        adapter.onClick = {
            if (it.itemType == TimeTrial::class.java.simpleName) {
                val act = GlobalResultFragmentDirections.actionGlobalResultFragmentToResultFragment(it.nextId
                        ?: 0)
                findNavController().navigate(act)
            } else {
                val act = GlobalResultFragmentDirections.actionGlobalResultFragmentToGlobalResultFragment(it.nextId
                        ?: 0, it.itemType)
                findNavController().navigate(act)
            }

        }



        return binding.root
    }
}

