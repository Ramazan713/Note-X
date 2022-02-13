package com.masterplus.notex.views

import android.content.SharedPreferences
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.masterplus.notex.adapters.NotePagingAdapter
import com.masterplus.notex.utils.CustomAlerts
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.utils.ShowMessage
import javax.inject.Inject

class NoteFragmentFactory @Inject constructor(
    private val notePagingAdapter: NotePagingAdapter,
    private val imm:InputMethodManager,
    private val showMessage: ShowMessage,
    private val customAlerts: CustomAlerts,
    private val customGetDialogs: CustomGetDialogs,
    private val sharedPreferences: SharedPreferences
):FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className){
            NoteFragment::class.java.name->{
                NoteFragment(notePagingAdapter,showMessage,customAlerts,customGetDialogs,sharedPreferences)
            }
            DisplayTextNoteFragment::class.java.name->{DisplayTextNoteFragment(imm,showMessage,customAlerts,customGetDialogs,sharedPreferences)}
            BookFragment::class.java.name-> BookFragment(customAlerts,customGetDialogs)
            TagsFragment::class.java.name-> TagsFragment(customAlerts,customGetDialogs)
            DisplayCheckListNoteFragment::class.java.name->{DisplayCheckListNoteFragment(imm, showMessage,customAlerts,customGetDialogs,sharedPreferences)}
            SettingsFragment::class.java.name->{
                SettingsFragment(sharedPreferences, customAlerts)
            }

            else->super.instantiate(classLoader, className)
        }


    }
}