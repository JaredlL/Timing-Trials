package com.jaredlinden.timingtrials.resultexplorer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentResultFilterBinding
import com.jaredlinden.timingtrials.databinding.ListItemResultFilterBinding
import dagger.hilt.android.AndroidEntryPoint


@BindingAdapter("android:src")
fun setImageViewResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}

@AndroidEntryPoint
class ResultsFilterFragment : BottomSheetDialogFragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.AppTheme)

        val vm:ResultExplorerViewModel by activityViewModels()

        val adapter = ResultFilterAdapter(requireContext(), viewLifecycleOwner).apply { setHasStableIds(true)}
        val layoutManger = LinearLayoutManager(requireContext())
        adapter.setItems(vm.columnViewModels)
        view?.jumpDrawablesToCurrentState()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        adapter.setHasStableIds(true)

        val binding = DataBindingUtil.inflate<FragmentResultFilterBinding>(inflater, R.layout.fragment_result_filter, container, false).apply {

            filterRecycler.adapter  =adapter

            filterRecycler.layoutManager = layoutManger
            clearAllFiltersButton.setOnClickListener {
                vm.clearAllColumnFilters()
            }
            //filterRecycler.invalidate()
        }

        return binding.root

    }

}

class ResultFilterAdapter internal constructor(val context: Context, val vlo: LifecycleOwner): RecyclerView.Adapter<ResultFilterAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    class ViewHolder(val binding: ListItemResultFilterBinding, val vlo:LifecycleOwner): RecyclerView.ViewHolder(binding.root){

        fun bind(vm: ResultFilterViewModel){
            binding.viewModel = vm
            binding.lifecycleOwner = vlo
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mItems[position].let {
            holder.bind(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ListItemResultFilterBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_result_filter, parent, false)
        return ViewHolder(binding,vlo)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int{ return mItems.count() }

    private var mItems: List<ResultFilterViewModel> = listOf()

    fun setItems(newItems: List<ResultFilterViewModel>){
        mItems = newItems
        notifyDataSetChanged()
    }


}