package com.jaredlinden.timingtrials.setup


import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaredlinden.timingtrials.IFabCallbacks


import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.adapters.OrderableRiderListAdapter
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.jaredlinden.timingtrials.data.FilledTimeTrialRider
import com.jaredlinden.timingtrials.data.NumberMode
import kotlinx.android.synthetic.main.fragment_order_riders.*


class OrderRidersFragment : Fragment() {

    private lateinit var viewModel: IOrderRidersViewModel
    private lateinit var mAdapter: OrderableRiderListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        viewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.orderRidersViewModel
        
        mAdapter = OrderableRiderListAdapter(requireContext()).apply { onMove = {x,y -> viewModel.moveItem(x, y)} }

        viewModel.getOrderableRiderData().observe(viewLifecycleOwner, Observer { ttData ->
            ttData?.let{
                if(ttData.timeTrialHeader.numberRules.mode == NumberMode.MAP){
                    mAdapter.setData(ttData){
                        showSetNumberDialog(it)
                    }
                }else{
                    mAdapter.setData(ttData){

                    }
                }

            }
        })
        (requireActivity() as IFabCallbacks).setVisibility(View.GONE)


        return inflater.inflate(R.layout.fragment_order_riders, container, false)
    }

    fun showSetNumberDialog(rd: FilledTimeTrialRider){
        val alert = AlertDialog.Builder(requireContext())
        val edittext = EditText(requireContext())

        edittext.setText((rd.timeTrialData.assignedNumber?:1).toString())
        alert.setTitle("${getString(R.string.set_number)} (${rd.riderData.fullName()})")

        alert.setView(edittext)



        edittext.inputType = InputType.TYPE_CLASS_NUMBER

        edittext.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                edittext.text?.toString()?.toIntOrNull()?.let {newNum->
                    viewModel.getOrderableRiderData().value?.let { tt->
                        if(tt.riderList.filterNot { it.riderData.id == rd.riderData.id }.any { it.timeTrialData.assignedNumber == newNum }){
                            edittext.error = getString(R.string.number_already_taken)
                        }else{
                            edittext.error = null
                        }
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        alert.setPositiveButton(R.string.ok) { _, _ ->

            edittext.text?.toString()?.toIntOrNull()?.let {
                viewModel.setRiderNumber(it, rd)
            }

        }

        alert.setNegativeButton(R.string.cancel) { _, _ -> }

        alert.show()
        edittext.minEms = 3
        edittext.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dragDropManager = RecyclerViewDragDropManager().apply {
            setInitiateOnMove(false)
            setInitiateOnLongPress(true)
            setLongPressTimeout(300)
        }

        val wrappedAdapter = dragDropManager.createWrappedAdapter(mAdapter)
        val mSortableRecyclerView = sortableRecyclerView
        mSortableRecyclerView.adapter = wrappedAdapter
        mSortableRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        //mSortableRecyclerView.itemAnimator = DraggableItemAnimator()
        dragDropManager.attachRecyclerView(sortableRecyclerView)
    }


    companion object {
        @JvmStatic
        fun newInstance() =
                OrderRidersFragment()
    }

}
