package com.masterplus.notex.views.dialogFragments

import android.app.AlarmManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.masterplus.notex.R

import com.masterplus.notex.databinding.FragmentReminderDiaBinding
import com.masterplus.notex.enums.ReminderTypes
import com.masterplus.notex.utils.ShowMessage
import com.masterplus.notex.utils.Utils.getDateWithFormat
import com.masterplus.notex.viewmodels.ReminderDiaViewModel
import com.masterplus.notex.viewmodels.items.DestroyListenerViewModel
import com.masterplus.notex.viewmodels.items.SetCalenderItemViewModel
import com.masterplus.notex.views.noLayout.DatePickerDialogFragment
import com.masterplus.notex.views.noLayout.TimePickerDialogFragment
import com.masterplus.notex.views.view.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ReminderDiaFragment : CustomDialogFragment() {

    private var _binding: FragmentReminderDiaBinding? = null
    private val binding get() = _binding!!
    private var calender: Calendar = Calendar.getInstance().apply { add(Calendar.MINUTE,15) }

    private val viewModel:ReminderDiaViewModel by viewModels()
    private val viewModelSetCalender: SetCalenderItemViewModel by viewModels()
    private val reminderTypes = ReminderTypes.values()
    private val viewModelDestroyListener: DestroyListenerViewModel by viewModels({requireParentFragment()})

    var reminderAlreadyChange:Boolean=false

    private var noteId:Long = 0L

    @Inject
    lateinit var alarmManager: AlarmManager

    @Inject
    lateinit var showMessage: ShowMessage


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedReminderTypePos",binding.spinnerReminder.selectedItemPosition)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentReminderDiaBinding.inflate(layoutInflater,container,false)

        requireArguments().let { args->
            noteId = args.getLong("noteId")
            viewModel.loadReminderWithNoteId(noteId)
        }

        savedInstanceState?.let { bundle ->
            bundle.getInt("selectedReminderTypePos",-1).let { pos->
                if(pos!=-1){
                    binding.spinnerReminder.setSelection(pos)
                    reminderAlreadyChange=true
                }
            }
        }


        showDateText()
        showTimeText()

        setSpinView()
        setButtonClicks()
        setObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    private fun setObservers(){
        viewModelSetCalender.liveItem.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            calender=it
            reminderAlreadyChange=true
            showDateText()
            showTimeText()
        })
        viewModel.reminder.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it!=null){
                getDateWithFormat(it.nextDate)?.let { date: Date ->
                    if(!reminderAlreadyChange){
                        calender.time=date
                        binding.spinnerReminder.setSelection(it.reminderType.ordinal)
                    }
                    binding.btRemoveReminder.visibility=View.VISIBLE
                    binding.titleReminderDia.text=getString(R.string.edit_reminder_text)

                    showDateText()
                    showTimeText()
                }
            }else{
                binding.btRemoveReminder.visibility=View.GONE
            }
        })
    }

    private fun setButtonClicks(){
        binding.cardDateReminder.setOnClickListener {
            val datePickerDialogFragment=DatePickerDialogFragment()
            datePickerDialogFragment.arguments = Bundle().also { it.putSerializable("calender",calender)}
            datePickerDialogFragment.show(childFragmentManager,"")
        }
        binding.cardTimeReminder.setOnClickListener {
            val timePickerDialogFragment=TimePickerDialogFragment()
            timePickerDialogFragment.arguments = Bundle().also { it.putSerializable("calender",calender)}
            timePickerDialogFragment.show(childFragmentManager,"")
        }
        binding.btCancelReminder.setOnClickListener {
            dismiss()
        }
        binding.btSaveReminder.setOnClickListener {
            if(Calendar.getInstance().after(calender)){
                showMessage.showLong(getString(R.string.reminder_setup_later_text))
            }else{
                saveReminder()
                dismiss()
            }

        }
        binding.btRemoveReminder.setOnClickListener {
            viewModel.removeReminderWithAlarmManager(noteId)
            dismiss()
        }


    }

    private fun setSpinView(){
        val spinItems= listOf<String>(getString(R.string.not_repeating_text),getString(R.string.daily_text)
            ,getString(R.string.weekly_text),getString(R.string.monthly_text))
        val spinAdapter= ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1,spinItems)
        binding.spinnerReminder.let { spinner ->
            spinner.adapter=spinAdapter
            spinner.setSelection(0)
        }
    }

    private fun saveReminder(){
        calender.set(Calendar.SECOND,0)
        viewModel.insertOrUpdateReminderWithAlarmManager(noteId,calender,
            reminderTypes[binding.spinnerReminder.selectedItemPosition])
    }
    private fun showDateText(){
        val dateFormat= SimpleDateFormat("d MMM", Locale.getDefault())
        binding.textDateRemainder.text=dateFormat.format(calender.time)
    }
    private fun showTimeText(){
        val timeFormat= SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.textTimeRemainder.text=timeFormat.format(calender.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        viewModelDestroyListener.setIsClosed(true)
    }

}