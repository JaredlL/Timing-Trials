package com.android.jared.linden.timingtrials.viewdata


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.*
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.databinding.FragmentListGenericBinding
import com.android.jared.linden.timingtrials.ui.CourseListViewWrapper
import com.android.jared.linden.timingtrials.util.*


/**
 * A fragment representing a list of items.
 * Activities containing this fragment MUST implement the
 */
class GenericListFragment : Fragment() {


    private lateinit var listViewModel: ListViewModel
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var adapter: GenericListAdapter<out Any>
    private lateinit var viewFactory: GenericViewHolderFactory<out Any>
    private lateinit var title: View

    private val itemType by argument<String>(ITEM_TYPE_EXTRA)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        listViewModel = getViewModel { injector.listViewModel() }

        when(itemType){
            ITEM_RIDER -> {
                viewFactory = RiderViewHolderFactory()
                adapter = GenericListAdapter(requireContext(), viewFactory)
                listViewModel.allRiders.observe(viewLifecycleOwner, Observer{res->
                    res?.let {(adapter as? GenericListAdapter<Rider>)?.setItems(it)}
                })
            }
            ITEM_COURSE -> {
                viewFactory = CourseViewHolderFactory()
                adapter = GenericListAdapter(requireContext(), viewFactory)
                listViewModel.allCourses.observe(viewLifecycleOwner, Observer{res->
                    res?.let {(adapter as? GenericListAdapter<CourseListViewWrapper>)?.setItems(it)}
                })
            }
            ITEM_TIMETRIAL ->{
                viewFactory = TimeTrialViewHolderFactory()
                adapter = GenericListAdapter(requireContext(), viewFactory)
                listViewModel.allTimeTrials.observe(viewLifecycleOwner, Observer{res->
                    res?.let {(adapter as? GenericListAdapter<TimeTrialHeader>)?.setItems(it)}
                })
            }
        }

        viewManager = LinearLayoutManager(context)

        val binding = DataBindingUtil.inflate<FragmentListGenericBinding>(inflater, R.layout.fragment_list_generic, container, false).apply{
            lifecycleOwner = (this@GenericListFragment)
            listHeading.addView(viewFactory.createTitle(inflater, container))
            genericRecyclerView.adapter = adapter
            genericRecyclerView.layoutManager = viewManager
            viewFactory.performFabAction(genericListFab)
        }

        return binding.root
    }




    companion object {
        fun newInstance(itemType: String): GenericListFragment {
            val args = Bundle().apply { putString(ITEM_TYPE_EXTRA, itemType) }
            return GenericListFragment().apply { arguments = args }
        }
    }
}
