package com.jaredlinden.timingtrials.spreadsheet

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.domain.GlobalResultData
import com.jaredlinden.timingtrials.util.getLengthConverter
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_spreadsheet.view.*

class SheetFragment : Fragment()  {

    private val args: SheetFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {


        val fabCallback = (requireActivity() as? IFabCallbacks)
        fabCallback?.setVisibility(View.GONE)

        val view =  inflater.inflate(R.layout.fragment_spreadsheet, container, false)


        val vm = requireActivity().getViewModel { requireActivity().injector.globalResultViewModel() }
        val conv = getLengthConverter()


        vm.resMed.observe(viewLifecycleOwner, object : Observer<GlobalResultData> {
            override fun onChanged(data: GlobalResultData?) {
                data?.let {
                    (requireActivity() as AppCompatActivity).supportActionBar?.title = data.title
                }

            }
        })


        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val density = displayMetrics.density.toInt()
        val adapter = SheetAdapter(requireContext(), density)
        val recyclerView = view.recyclerView
        vm.getRiderResultList(args.itemId, args.itemTypeId).observe(viewLifecycleOwner, Observer {res->
            res?.let {
                val resultSheet = if(args.itemTypeId == Rider::class.java.simpleName){
                    RiderResultListSpreadSheet(it, conv)
                }else{
                    CourseResultListSpreadSheet(it, conv)
                }
                adapter.setNewItems(resultSheet)
                recyclerView.layoutManager = SheetLayoutManager(resultSheet)
            }

        })

        //val opts = SheetLayoutManagerOptions(data, ('a'..'z').map { y-> y.toString()})

        //adapter.setNewItems(opts)




        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(false)



        recyclerView.adapter = adapter




        //adapter.notifyDataSetChanged()




        return view
    }
}