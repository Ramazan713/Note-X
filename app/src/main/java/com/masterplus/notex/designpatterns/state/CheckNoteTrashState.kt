package com.masterplus.notex.designpatterns.state

import android.view.Menu
import android.view.MenuInflater
import com.masterplus.notex.R
import com.masterplus.notex.adapters.CheckNoteAdapter
import com.masterplus.notex.databinding.FragmentDisplayCheckListNoteBinding
import com.masterplus.notex.enums.NoteKinds

class CheckNoteTrashState(context: CheckNoteEditorContext) :NoteStateAbstract<FragmentDisplayCheckListNoteBinding>(context) {
    override fun initExecutor(binding: FragmentDisplayCheckListNoteBinding) {
        noteKindListener?.invoke(NoteKinds.TRASH_KIND)
        binding.showTagsCheckNote.isEnabled=false
        binding.selectBookFromCheckNote.root.isEnabled=false
        binding.editTitleCheckNote.isCursorVisible=false
        binding.editTitleCheckNote.isFocusable=false
        binding.editTitleCheckNote.isFocusableInTouchMode=false
        binding.recyclerCheckNote.adapter.let { adapter ->
            if(adapter is CheckNoteAdapter)
                adapter.setEnabled(false)
        }
    }

    override fun setOnCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_trash_menu,menu)
    }

    override fun setOnPrepareMenu(menu: Menu) {

    }

    override fun clearInitAffects(binding: FragmentDisplayCheckListNoteBinding) {
        binding.showTagsCheckNote.isEnabled=true
        binding.selectBookFromCheckNote.root.isEnabled=true
        binding.editTitleCheckNote.isCursorVisible=true
        binding.editTitleCheckNote.isFocusable=true
        binding.editTitleCheckNote.isFocusableInTouchMode=true
        binding.recyclerCheckNote.adapter.let { adapter ->
            if(adapter is CheckNoteAdapter)
                adapter.setEnabled(true)
        }
    }
}