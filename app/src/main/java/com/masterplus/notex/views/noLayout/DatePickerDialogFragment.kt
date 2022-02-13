package com.masterplus.notex.views.noLayout

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.masterplus.notex.viewmodels.items.SetCalenderItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
@AndroidEntryPoint
class DatePickerDialogFragment : DialogFragment(){
    private lateinit var c:Calendar
    private val viewModelSetItem: SetCalenderItemViewModel by viewModels({requireParentFragment()})


    private var listener= DatePickerDialog.OnDateSetListener { p0, year, month, dayOfMonth ->
        c.set(Calendar.YEAR,year)
        c.set(Calendar.MONTH,month)
        c.set(Calendar.DAY_OF_MONTH,dayOfMonth)
        viewModelSetItem.setItem(c)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        requireArguments().let { args->
            c = args.getSerializable("calender") as Calendar
        }
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dialog=DatePickerDialog(requireActivity(),listener, year, month, day)
        dialog.datePicker.minDate=System.currentTimeMillis()

        return dialog

    }
}