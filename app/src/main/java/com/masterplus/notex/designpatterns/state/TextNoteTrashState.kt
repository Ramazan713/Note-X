package com.masterplus.notex.designpatterns.state

import android.view.Menu
import android.view.MenuInflater
import com.masterplus.notex.R
import com.masterplus.notex.databinding.FragmentDisplayTextNoteBinding
import com.masterplus.notex.enums.NoteKinds

class TextNoteTrashState(private val context: TextNoteEditorContext
):NoteStateAbstract<FragmentDisplayTextNoteBinding>(context) {
    override fun initExecutor(binding: FragmentDisplayTextNoteBinding) {
        noteKindListener?.invoke(NoteKinds.TRASH_KIND)
        binding.showTagsTextNote.isEnabled=false
        binding.selectBookFromTextNote.root.isEnabled=false
        binding.editContentTextNote.isCursorVisible=false
        binding.editContentTextNote.isFocusable=false
        binding.editContentTextNote.isFocusableInTouchMode=false
        binding.editTitleTextNote.isFocusableInTouchMode=false
        binding.editTitleTextNote.isFocusable=false
        binding.editTitleTextNote.isCursorVisible=false
    }

    override fun setOnCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_trash_menu,menu)
    }

    override fun setOnPrepareMenu(menu: Menu) {}

    override fun clearInitAffects(binding: FragmentDisplayTextNoteBinding) {
        binding.showTagsTextNote.isEnabled=true
        binding.selectBookFromTextNote.root.isEnabled=true
        binding.editContentTextNote.isCursorVisible=true
        binding.editContentTextNote.isFocusable=true
        binding.editContentTextNote.isFocusableInTouchMode=true
        binding.editTitleTextNote.isFocusable=true
        binding.editTitleTextNote.isCursorVisible=true
        binding.editTitleTextNote.isFocusableInTouchMode=true
    }
}