package com.jaredlinden.timingtrials.setup


import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.jaredlinden.timingtrials.IFabCallbacks


import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.adapters.OrderableRiderListAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.jaredlinden.timingtrials.data.FilledTimeTrialRider
import com.jaredlinden.timingtrials.data.NumberMode
import com.jaredlinden.timingtrials.databinding.FragmentOrderRidersBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderRidersFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val setupVm: SetupViewModel by activityViewModels()
        val viewModel = setupVm.orderRidersViewModel
        
        val mAdapter = OrderableRiderListAdapter(requireContext()).apply { onMove = {x,y -> viewModel.moveItem(x, y)} }

        viewModel.getOrderableRiderData().observe(viewLifecycleOwner) { ttData ->
            ttData?.let{
                if(ttData.timeTrialHeader.numberRules.mode == NumberMode.MAP){
                    mAdapter.setData(ttData){
                        showSetNumberDialog(it, setupVm)
                    }
                }else{
                    mAdapter.setData(ttData){
                    }
                }
            }
        }

        val binding = FragmentOrderRidersBinding.inflate(inflater, container, false).apply {
            val dragDropManager = RecyclerViewDragDropManager().apply {
                setInitiateOnMove(false)
                setInitiateOnLongPress(true)
                setLongPressTimeout(300)
            }
            val wrappedAdapter = dragDropManager.createWrappedAdapter(mAdapter)
            sortableRecyclerView.apply {
                adapter = wrappedAdapter
                layoutManager = LinearLayoutManager(requireContext())
                itemAnimator = DraggableItemAnimator()

            }
            dragDropManager.attachRecyclerView(sortableRecyclerView)
        }

        (requireActivity() as IFabCallbacks).setFabVisibility(View.GONE)
        return binding.root
    }

    private fun showSetNumberDialog(rd: FilledTimeTrialRider, setupVm: SetupViewModel){
        val alert = AlertDialog.Builder(requireContext())
        val edittext = EditText(requireContext())

        edittext.setText((rd.timeTrialRiderData.assignedNumber?:1).toString())
        alert.setTitle("${getString(R.string.set_number)} (${rd.riderData.fullName()})")

        alert.setView(edittext)

        edittext.inputType = InputType.TYPE_CLASS_NUMBER

        edittext.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                edittext.text?.toString()?.toIntOrNull()?.let {newNum->
                    setupVm.orderRidersViewModel.getOrderableRiderData().value?.let { tt->
                        if(tt.riderList.filterNot { it.riderData.id == rd.riderData.id }.any { it.timeTrialRiderData.assignedNumber == newNum }){
                            edittext.error = getString(R.string.number_already_taken_swap)
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
                setupVm.orderRidersViewModel.setRiderNumber(it, rd)
            }
        }

        alert.setNegativeButton(R.string.cancel) { _, _ -> }

        alert.show()
        edittext.minEms = 3
        edittext.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            OrderRidersFragment()
    }
}
