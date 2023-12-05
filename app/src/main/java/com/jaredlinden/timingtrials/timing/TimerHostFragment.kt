package com.jaredlinden.timingtrials.timing

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.jaredlinden.timingtrials.BuildConfig
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.NumberMode
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.databinding.FragmentTimerBinding
import com.jaredlinden.timingtrials.databinding.FragmentTimerHostBinding
import com.jaredlinden.timingtrials.select.SELECTED_RIDERS
import com.jaredlinden.timingtrials.util.*
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Instant

@AndroidEntryPoint
class TimerHostFragment : Fragment() {

    private val viewModel : TimingViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })

        (activity as IFabCallbacks).apply {
            setFabVisibility(View.GONE)
        }

        val binding = FragmentTimerHostBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    var selectedNumber : Event<Int?> = Event(null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<LongArray>(SELECTED_RIDERS)?.observe(viewLifecycleOwner){ selectedIds->

            selectedIds?.firstOrNull()?.let {id->
                viewModel.timeTrial.value?.let {tt->
                    val numb = selectedNumber.getContentIfNotHandled()
                    if(tt.timeTrialHeader.numberRules.mode == NumberMode.MAP && numb != null){
                        viewModel.addLateRider(id, numb)
                    }else{
                        viewModel.addLateRider(id, null)
                    }
                }
            }

            findNavController().currentBackStackEntry?.savedStateHandle?.remove<LongArray>(SELECTED_RIDERS)
        }
    }

    fun showExitDialog(){
        (requireActivity() as ITimingActivity).showExitDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_timing, menu)
        if(BuildConfig.DEBUG){
            menu.findItem(R.id.timingTest)?.isVisible = true
            menu.findItem(R.id.timingTest2)?.isVisible = true
        }
    }

    var prevBackPress = 0L
    fun onBackPressed() {
        val tt = viewModel.timeTrial.value

        if(tt==null)
        {
            (requireActivity() as ITimingActivity).endTiming()
        }

        if(System.currentTimeMillis() > prevBackPress + 2000){
            Toast.makeText(requireContext(), "Tap again to end", Toast.LENGTH_SHORT).show()
            prevBackPress = System.currentTimeMillis()
        }else{

            viewModel.timeTrial.value?.let{
                if((it.timeTrialHeader.startTime?.toInstant() ?: Instant.MAX) > Instant.now()){
                    (requireActivity() as ITimingActivity).showExitDialogWithSetup()
                }else{
                    showExitDialog()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId){

            R.id.timingMenuTips-> {
                showTipsDialog()
                true
            }
            R.id.timingTest->{
                viewModel.testFinishAll()
                true
            }

            R.id.timingTest2->{
                viewModel.testFinishTt()
                true
            }

            R.id.timingMenuSettings->{
                val action = TimerHostFragmentDirections.actionTimerHostFragmentToMainPrefsFragment()
                findNavController().navigate(action)
                true
            }

            R.id.timingMenuAddLateRider->{

                viewModel.timeTrial.value?.let {tt->
                    if(tt.timeTrialHeader.numberRules.mode == NumberMode.MAP){
                        showSetNumberDialog(tt)
                    }else{
                        val ids = viewModel.timeTrial.value?.riderList?.mapNotNull { it.riderId() }?: listOf()
                        val action = TimerHostFragmentDirections.actionTimerHostFragmentToSelectRiderFragment(ids.toLongArray(), true)
                        findNavController().navigate(action)
                    }
                }
                true
            }
            else -> true
        }
    }

    fun showSetNumberDialog(tt:TimeTrial){
        val alert = AlertDialog.Builder(requireContext())
        val edittext = EditText(requireContext())

        val usedNumbers = tt.riderList.mapNotNull { it.timeTrialData.assignedNumber }
        val max = if(usedNumbers.any()) usedNumbers.maxOrNull()?:0 else 0

        edittext.setText((max + 1).toString())
        alert.setTitle(getString(R.string.what_number_will_late_rider_use))

        alert.setView(edittext)
        edittext.inputType = InputType.TYPE_CLASS_NUMBER

        edittext.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                edittext.text?.toString()?.toIntOrNull()?.let {newNum->
                        if(usedNumbers.contains(newNum)){
                            edittext.error = getString(R.string.number_already_taken)
                        }else{
                            edittext.error = null
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
                if(!usedNumbers.contains(it)){
                    selectedNumber = Event(it)
                    val ids = viewModel.timeTrial.value?.riderList?.mapNotNull { it.riderId() }?: listOf()
                    val action = TimerHostFragmentDirections.actionTimerHostFragmentToSelectRiderFragment(ids.toLongArray(), true)
                    findNavController().navigate(action)
                }else{
                    Toast.makeText(requireContext(), getString(R.string.number_already_taken), Toast.LENGTH_SHORT).show()
                }
            }
        }

        alert.setNegativeButton(R.string.cancel) { _, _ -> }

        alert.show()
        edittext.minEms = 3
        edittext.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    fun showTipsDialog(){
        val htmlString =
                """    
    &#8226; ${getString(R.string.tip_timing_press_timer)}<br/><br/>
    &#8226; ${getString(R.string.tip_timing_assign_event)}<br/><br/>
    &#8226; ${getString(R.string.tip_timing_press_event)}<br/><br/>
    &#8226; ${getString(R.string.tip_timing_longpress_number)}<br/><br/>
    &#8226; ${getString(R.string.tip_timing_longpress_event)}
    
 """

        val html = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val mColor = ContextCompat.getColor(requireContext(), R.color.secondaryDarkColor)
        val d = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_help_outline_24)
        Utils.colorDrawable(mColor, d)

        AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.tips))
                .setIcon(d)
                .setMessage(html)
                .setPositiveButton(R.string.ok){_,_->
                }
                .show()
    }
}