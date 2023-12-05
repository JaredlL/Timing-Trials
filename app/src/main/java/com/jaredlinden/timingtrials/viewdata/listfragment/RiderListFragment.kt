package com.jaredlinden.timingtrials.viewdata.listfragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.databinding.FragmentListGenericBinding
import com.jaredlinden.timingtrials.databinding.ListItemRiderBinding
import com.jaredlinden.timingtrials.viewdata.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class RiderListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val listViewModel: ListViewModel by activityViewModels()
        Timber.d("Create")

        val viewFactory = RiderViewHolderFactory()
        val adapter = GenericListAdapter(requireContext(), viewFactory)
        val viewManager = LinearLayoutManager(context)

        adapter.setHasStableIds(true)

        listViewModel.filteredAllRiders.observe(viewLifecycleOwner) {res->
            res?.let {adapter.setItems(it)}
        }

        val binding = FragmentListGenericBinding.inflate(inflater, container, false).apply{
            lifecycleOwner = viewLifecycleOwner
            viewFactory.let {
                listHeading.addView(
                    it.createTitle(inflater, container),
                    0,
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
            genericRecyclerView.adapter = adapter
            genericRecyclerView.layoutManager = viewManager
        }
        return binding.root
    }

    override fun onDetach() {
        Timber.d("Detach")
        super.onDetach()
    }

}

class RiderViewHolder(binding: ListItemRiderBinding): GenericBaseHolder<Rider, ListItemRiderBinding>(binding) {

    override fun bind(data: Rider){
        binding.apply{
            rider = data
            riderLayout.setOnLongClickListener {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSheetFragment(data.javaClass.simpleName,data.id?:0)
                Navigation.findNavController(binding.root).navigate(action)
                true
            }

            riderLayout.setOnClickListener {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragment2ToEditRiderFragment(data.id ?: 0)
                Navigation.findNavController(binding.root).navigate(action)
            }

            executePendingBindings()
        }
    }
}

class RiderViewHolderFactory: GenericViewHolderFactory<Rider>() {
    override fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?): View {
        val b = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false).apply {
            genericTextView1.typeface = Typeface.DEFAULT_BOLD
            genericTextView2.typeface = Typeface.DEFAULT_BOLD
            genericTextView3.typeface = Typeface.DEFAULT_BOLD
        }
        return b.root
    }

    override fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?): GenericBaseHolder<Rider, ListItemRiderBinding> {
        val binding = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false)
        return RiderViewHolder(binding)
    }
    override fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data: Rider): View {
        val binding = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflator, R.layout.list_item_rider, parent, false).apply { rider = data }
        return binding.root
    }

}