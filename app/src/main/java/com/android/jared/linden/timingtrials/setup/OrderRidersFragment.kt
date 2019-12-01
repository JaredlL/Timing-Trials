package com.android.jared.linden.timingtrials.setup


import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager


import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.adapters.OrderableRiderListAdapter
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import kotlinx.android.synthetic.main.fragment_order_riders.*


class OrderRidersFragment : Fragment() {

    private lateinit var viewModel: IOrderRidersViewModel
    private lateinit var mAdapter: OrderableRiderListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        viewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.orderRidersViewModel
        
        mAdapter = OrderableRiderListAdapter(requireContext()).apply { onMove = {x,y -> viewModel.moveItem(x, y)} }

        viewModel.getOrderableRiders().observe(viewLifecycleOwner, Observer { riders ->
            riders?.let {mAdapter.setRiders(riders)}
        })

        return inflater.inflate(R.layout.fragment_order_riders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dragDropManager = RecyclerViewDragDropManager().apply {
            setInitiateOnMove(false)
            setInitiateOnLongPress(true)
        }

        val wrappedAdapter = dragDropManager.createWrappedAdapter(mAdapter)
        val mSortableRecyclerView = sortableRecyclerView
        mSortableRecyclerView.adapter = wrappedAdapter
        mSortableRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        //mSortableRecyclerView.itemAnimator = DraggableItemAnimator()
        dragDropManager.attachRecyclerView(sortableRecyclerView)
    }

    fun supportsViewElevation(): Boolean{
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                OrderRidersFragment()
    }

}
