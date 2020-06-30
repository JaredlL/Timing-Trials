package com.jaredlinden.timingtrials.setup

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.adapters.CourseListAdapter
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.databinding.FragmentCourseListBinding
import com.jaredlinden.timingtrials.ui.SelectableCourseViewModel
import com.jaredlinden.timingtrials.util.*

class SelectCourseFragment : Fragment() {

    private lateinit var setupViewModel: SetupViewModel
    private lateinit var viewModel: ISelectCourseViewModel
    private lateinit var adapter: CourseListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val args: SelectCourseFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        setupViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }

        if(args.timeTrialId != -1L){
            setupViewModel.changeTimeTrial(args.timeTrialId)
        }

        (requireActivity() as IFabCallbacks).apply {
            setVisibility(View.VISIBLE)
            setImage(R.drawable.ic_add_white_24dp)
            setAction {
                val action = SelectCourseFragmentDirections.actionSelectCourseFragmentToEditCourseFragment(0,context?.getString(R.string.new_course)?:"")
                findNavController().navigate(action)
            }
        }

        viewModel = setupViewModel.selectCourseViewModel
        viewManager = LinearLayoutManager(context)
        adapter = CourseListAdapter(requireContext())
        adapter.editCourse = ::editCourse

        viewModel.getAllCourses().observe(viewLifecycleOwner, Observer { courses ->
            courses?.let{adapter.setCourses(it, getLengthConverter())}
        })
        adapter.courseSelected = { blobs ->

            //val origCourse = viewModel.getAllCourses().value?.selectedId
            viewModel.setSelectedCourse(blobs)
            val action = SelectCourseFragmentDirections.actionSelectCourseFragmentToSetupViewPagerFragment()
            findNavController().navigate(action)






        }

        adapter.setHasStableIds(true)

        val unitString = getLengthConverter().unitDef.miniString

        val heading: SelectableCourseViewModel = SelectableCourseViewModel("Course Name", "Distance ($unitString)", "CTT Name")

        val binding = DataBindingUtil.inflate<FragmentCourseListBinding>(inflater, R.layout.fragment_course_list, container, false).apply{
            lifecycleOwner = (this@SelectCourseFragment)
            courseHeading.courseVm = heading
            courseHeading.checkBox.visibility = View.INVISIBLE
            courseRecyclerView.adapter = adapter
            courseRecyclerView.layoutManager = viewManager
//            courseListFab.setOnClickListener {
//                val action = SelectCourseFragmentDirections.actionSelectCourseFragmentToEditCourseFragment(0,context?.getString(R.string.new_course)?:"")
//                findNavController().navigate(action)
//
//                //editCourse(0)
//               // dismiss()
//            }
        }


        return binding.root
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        // The only reason you might override this method when using onCreateView() is
//        // to modify any dialog characteristics. For example, the dialog includes a
//        // title by default, but your custom layout might not need it. So here you can
//        // remove the dialog title, but you must call the superclass to get the Dialog.
//        val dialog = super.onCreateDialog(savedInstanceState)
//        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//
//        val root: RelativeLayout = RelativeLayout(activity).apply {
//            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        }
//        dialog.setContentView(root)
//
//        //dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        return dialog
//    }

    private fun editCourse(course: Course){
        val action = SelectCourseFragmentDirections.actionSelectCourseFragmentToEditCourseFragment(course.id?:0, resources.getString(R.string.edit_course))
        findNavController().navigate(action)
    }


    companion object {
        fun newInstance(): SelectCourseFragment {
            return SelectCourseFragment()
        }
    }
}