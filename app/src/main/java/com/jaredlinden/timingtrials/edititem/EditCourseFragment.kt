package com.jaredlinden.timingtrials.edititem

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.databinding.FragmentEditCourseBinding
import com.jaredlinden.timingtrials.util.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditCourseFragment : Fragment() {



    private val args: EditCourseFragmentArgs by navArgs()
    private val courseViewModel: EditCourseViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val courseId = args.courseId
        setHasOptionsMenu(true)
        courseViewModel.setLengthConverter(getLengthConverter())
        courseViewModel.changeCourse(courseId)
        courseViewModel.mutableCourse.observe(viewLifecycleOwner, Observer {  })

        //Set title
        (requireActivity() as AppCompatActivity).supportActionBar?.title = if(args.courseId == 0L) getString(R.string.add_course) else getString(R.string.edit_course)

        val fabCallback = (requireActivity() as IFabCallbacks)

        fabCallback.setFabImage(R.drawable.ic_done_white_24dp)
        fabCallback.setFabVisibility(View.VISIBLE)

        courseViewModel.doJumpToCourseResults.observe(viewLifecycleOwner, EventObserver{
            val action = EditCourseFragmentDirections.actionEditCourseFragmentToSheetFragment(Course::class.java.simpleName, it)
            findNavController().navigate(action)
        })

        courseViewModel.updateSuccess.observe(viewLifecycleOwner, EventObserver{
            if(it){
                findNavController().popBackStack()
            }
        })

        courseViewModel.message.observe(viewLifecycleOwner, EventObserver{
            Toast.makeText(requireContext(), requireContext().getText(it), Toast.LENGTH_LONG).show()
        })



        val binding = DataBindingUtil.inflate<FragmentEditCourseBinding>(inflater, R.layout.fragment_edit_course, container, false).apply {
            viewModel = courseViewModel
            lifecycleOwner = (this@EditCourseFragment)
            fabCallback.fabClickEvent.observe(viewLifecycleOwner, EventObserver {
                if(it){
                    if(courseViewModel.courseName.value?.trim().isNullOrBlank()) Toast.makeText(requireContext(), getString(R.string.course_requires_name), Toast.LENGTH_SHORT).show()
                    else{
                        courseViewModel.addOrUpdate()
                        //findNavController().popBackStack()
                    }
                }


            })

            cttNameEdit.setOnEditorActionListener{_, actionId, keyEvent ->
                if ((keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    courseViewModel.addOrUpdate()
                    //findNavController().popBackStack()
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
    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
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
