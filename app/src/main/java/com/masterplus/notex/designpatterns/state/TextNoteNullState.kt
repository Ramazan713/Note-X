package com.masterplus.notex.designpatterns.state

import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.masterplus.notex.R
import com.masterplus.notex.databinding.FragmentDisplayTextNoteBinding

class TextNoteNullState(private val context: TextNoteEditorContext):NoteStateAbstract<FragmentDisplayTextNoteBinding>(context) {

    override fun initExecutor(binding: FragmentDisplayTextNoteBinding) {
        binding.showTagsTextNote.visibility = View.GONE
        binding.selectBookFromTextNote.root.isEnabled=false
    }

    override fun setOnCreateMenu(menu: Menu, inflater: MenuInflater) {
        context.defaultOnCreateMenu(menu, inflater)
    }

    override fun setOnPrepareMenu(menu: Menu) {
        menu.findItem(R.id.achieve_remove_menu_item)?.isVisible=false
        menu.findItem(R.id.achieve_add_menu_item)?.isVisible=false
        menu.findItem(R.id.display_note_set_reminder_menu_item)?.isVisible=false
        menu.findItem(R.id.display_note_share_menu_item)?.isVisible=false
        menu.findItem(R.id.display_note_remove_menu_item)?.isVisible=false
        menu.findItem(R.id.display_note_color_menu_item)?.isVisible=false
        menu.findItem(R.id.display_note_tag_menu_item)?.isVisible=false

    }

    override fun clearInitAffects(binding: FragmentDisplayTextNoteBinding) {
        binding.showTagsTextNote.visibility = View.VISIBLE
        binding.selectBookFromTextNote.root.isEnabled=true
    }
}