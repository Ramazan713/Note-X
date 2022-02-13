package com.masterplus.notex.designpatterns.state

import android.view.Menu
import android.view.MenuInflater
import com.masterplus.notex.R
import com.masterplus.notex.databinding.FragmentDisplayTextNoteBinding
import com.masterplus.notex.designpatterns.factory.NoteStateFactory
import com.masterplus.notex.enums.NoteKinds

class TextNoteEditorContext(binding: FragmentDisplayTextNoteBinding) :
    NoteEditorContextAbstract<FragmentDisplayTextNoteBinding>(binding)
{

    override fun defaultOnCreateMenu(menu: Menu, inflater: MenuInflater){
        inflater.inflate(R.menu.display_note_menu,menu)
        inflater.inflate(R.menu.archive_menu,menu)
    }

    override fun transNullToNote(noteKinds: NoteKinds){
        state.clearInitAffects(binding)
        setCurrentState(NoteStateFactory.getTextNoteState(noteKinds,this))
        state.initExecutor(binding)
    }
    override fun recoverNote(){
        state.clearInitAffects(binding)
        setCurrentState(TextNoteAllKindState(this))
        state.initExecutor(binding)
    }

    override fun setCurrentState(noteKinds: NoteKinds) {
        this.state=NoteStateFactory.getTextNoteState(noteKinds,this)
        initNote()
    }


}