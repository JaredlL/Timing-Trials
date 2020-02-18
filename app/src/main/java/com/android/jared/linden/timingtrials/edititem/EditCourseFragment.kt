package com.android.jared.linden.timingtrials.edititem

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.jared.linden.timingtrials.IFabCallbacks
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentCourseBinding
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior


class EditCourseFragment : Fragment() {



    private val args: EditCourseFragmentArgs by navArgs()
    private lateinit var courseViewModel: EditCourseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val courseId = args.courseId

        courseViewModel = requireActivity().getViewModel { requireActivity().injector.courseViewModel() }

        setHasOptionsMenu(true)
        courseViewModel.changeCourse(courseId)
        courseViewModel.mutableCourse.observe(viewLifecycleOwner, Observer {  })

        //Set title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = if(args.courseId == 0L) getString(R.string.add_course) else getString(R.string.edit_course)

        val fabCallback = (requireActivity() as IFabCallbacks)

        fabCallback.setImage(R.drawable.ic_done_white_24dp)
        fabCallback.setVisibility(View.VISIBLE)

        val binding = DataBindingUtil.inflate<FragmentCourseBinding>(inflater, R.layout.fragment_course, container, false).apply {
            viewModel = courseViewModel
            lifecycleOwner = (this@EditCourseFragment)
            fabCallback.setAction {

                if(courseViewModel.courseName.value.isNullOrBlank()) Toast.makeText(requireContext(), getString(R.string.course_requires_name), Toast.LENGTH_SHORT).show()
                else{
                    courseViewModel.addOrUpdate()
                    findNavController().popBackStack()
                }
            }

            cttNameEdit.setOnEditorActionListener{_, actionId, keyEvent ->
                if ((keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    courseViewModel.addOrUpdate()
                    findNavController().popBackStack()
                }
                return@setOnEditorActionListener false
            }

        }

        return binding.root


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //menu.clear()
        inflater.inflate(R.menu.menu_delete, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete_deleteitem -> {
                showDeleteDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showDeleteDialog(){
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_course))
                .setMessage(resources.getString(R.string.confirm_delete_course_message))
                .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                    courseViewModel.deleteCourse()
                    findNavController().popBackStack()
                }
                .setNegativeButton("Dismiss"){_,_->

                }
                .create().show()
    }


}
