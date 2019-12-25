package com.android.jared.linden.timingtrials.viewdata


import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.MainActivity
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.databinding.FragmentListGenericBinding
import com.android.jared.linden.timingtrials.ui.SelectableCourseViewModel
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_list_generic.*


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


        listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }

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
                    res?.let {(adapter as? GenericListAdapter<SelectableCourseViewModel>)?.setItems(it)}
                })
            }
            ITEM_TIMETRIAL ->{
                viewFactory = TimeTrialViewHolderFactory(listViewModel, viewLifecycleOwner)
                adapter = GenericListAdapter(requireContext(), viewFactory)
                listViewModel.allTimeTrials.observe(viewLifecycleOwner, Observer{res->
                    res?.let {(adapter as? GenericListAdapter<TimeTrialHeader>)?.setItems(it)}
                })
            }
        }



        viewManager = LinearLayoutManager(context)

        val binding = DataBindingUtil.inflate<FragmentListGenericBinding>(inflater, R.layout.fragment_list_generic, container, false).apply{
            lifecycleOwner = (this@GenericListFragment)
            listHeading.addView(viewFactory.createTitle(inflater, container), 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            genericRecyclerView.adapter = adapter
            genericRecyclerView.layoutManager = viewManager
            viewFactory.performFabAction(genericListFab)

        }

        return binding.root
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val p = genericListFab.layoutParams as CoordinatorLayout.LayoutParams
//        p.anchorId = listHeading.id
//        genericListFab.layoutParams = p
//        (requireActivity() as MainActivity).
//    }

    companion object {
        fun newInstance(itemType: String): GenericListFragment {
            val args = Bundle().apply { putString(ITEM_TYPE_EXTRA, itemType) }
            return GenericListFragment().apply { arguments = args }
        }
    }
}

class FAB_Hide_on_Scroll(context: Context?, attrs: AttributeSet?) : FloatingActionButton.Behavior() {
    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        //child -> Floating Action Button
        if (dyConsumed > 0) {
            val layoutParams = child.layoutParams as CoordinatorLayout.LayoutParams
            val fab_bottomMargin = layoutParams.bottomMargin
            child.animate().translationY((child.height + fab_bottomMargin).toFloat()).setInterpolator(LinearInterpolator()).start()
        } else if (dyConsumed < 0) {
            child.animate().translationY(0F).setInterpolator(LinearInterpolator()).start()
        }
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }


}

class FixScrollingFooterBehavior : ScrollingViewBehavior {
    private var appBarLayout: AppBarLayout? = null

    constructor() : super() {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (appBarLayout == null) {
            appBarLayout = dependency as AppBarLayout
        }
        val result = super.onDependentViewChanged(parent, child, dependency)
        val bottomPadding = calculateBottomPadding(appBarLayout)
        val paddingChanged = bottomPadding != child.paddingBottom
        if (paddingChanged) {
            child.setPadding(
                    child.paddingLeft,
                    child.paddingTop,
                    child.paddingRight,
                    bottomPadding)
            child.requestLayout()
        }
        return paddingChanged || result
    }

    // Calculate the padding needed to keep the bottom of the view pager's content at the same location on the screen.
    private fun calculateBottomPadding(dependency: AppBarLayout?): Int {
        val totalScrollRange = dependency!!.totalScrollRange
        return totalScrollRange + dependency.top
    }
}
