package com.masterplus.notex.views.noLayout

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.masterplus.notex.viewmodels.items.SetCalenderItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
@AndroidEntryPoint
class TimePickerDialogFragment : DialogFragment() {
    private val viewModelSetItem: SetCalenderItemViewModel by viewModels({requireParentFragment()})


    private lateinit var c:Calendar
    private var listener= TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
        c.set(Calendar.HOUR_OF_DAY,hourOfDay)
        c.set(Calendar.MINUTE,minute)
        viewModelSetItem.setItem(c)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        requireArguments().let { args->
            c = args.getSerializable("calender") as Calendar
        }
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        return TimePickerDialog(activity, listener, hour, minute,true)
    }

}