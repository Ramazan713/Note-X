package com.masterplus.notex.designpatterns.state

import android.view.Menu
import android.view.MenuInflater
import com.masterplus.notex.R
import com.masterplus.notex.databinding.FragmentDisplayCheckListNoteBinding
import com.masterplus.notex.enums.NoteKinds

class CheckNoteAllKindState(private val context: CheckNoteEditorContext) :
    NoteStateAbstract<FragmentDisplayCheckListNoteBinding>(context) {
    override fun setOnCreateMenu(menu: Menu, inflater: MenuInflater) {
        context.defaultOnCreateMenu(menu, inflater)
    }

    override fun setOnPrepareMenu(menu: Menu) {
        menu.findItem(R.id.achieve_add_menu_item)?.isVisible=true
        menu.findItem(R.id.achieve_remove_menu_item)?.isVisible=false
    }

    override fun clearInitAffects(binding: FragmentDisplayCheckListNoteBinding) {
    }

    override fun initExecutor(binding: FragmentDisplayCheckListNoteBinding) {
        noteKindListener?.invoke(NoteKinds.ALL_KIND)
    }
}