package com.jaredlinden.timingtrials.resultexplorer

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
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.adapters.GenericListItemAdapter
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.databinding.FragmentGlobalResultBinding
import com.jaredlinden.timingtrials.domain.GlobalResultData
import com.jaredlinden.timingtrials.ui.GenericListItemNext
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector

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

