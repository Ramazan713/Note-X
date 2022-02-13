package com.masterplus.notex.designpatterns.state

import android.view.Menu
import android.view.MenuInflater
import com.masterplus.notex.R
import com.masterplus.notex.databinding.FragmentDisplayCheckListNoteBinding
import com.masterplus.notex.designpatterns.factory.NoteStateFactory
import com.masterplus.notex.enums.NoteKinds

class CheckNoteEditorContext(binding: FragmentDisplayCheckListNoteBinding) :
    NoteEditorContextAbstract<FragmentDisplayCheckListNoteBinding>(binding) {
    override fun defaultOnCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.display_note_menu,menu)
        inflater.inflate(R.menu.archive_menu,menu)
        inflater.inflate(R.menu.display_checklist_menu,menu)
    }

    override fun transNullToNote(noteKinds: NoteKinds) {
        state.clearInitAffects(binding)
        setCurrentState(NoteStateFactory.getCheckNoteState(noteKinds,this))
        state.initExecutor(binding)
    }

    override fun recoverNote() {
        state.clearInitAffects(binding)
        setCurrentState(CheckNoteAllKindState(this))
        state.initExecutor(binding)
    }

    override fun setCurrentState(noteKinds: NoteKinds) {
        state=NoteStateFactory.getCheckNoteState(noteKinds,this)
        initNote()
    }
}