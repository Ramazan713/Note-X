package com.masterplus.notex.designpatterns.state

import android.view.Menu
import android.view.MenuInflater
import androidx.viewbinding.ViewBinding
import com.masterplus.notex.enums.NoteKinds

abstract class NoteEditorContextAbstract<T> constructor(protected val binding: T) where T:ViewBinding{
    protected lateinit var state: NoteStateAbstract<T>
    private var noteKindListener:((noteKind:NoteKinds)->Unit)? = null

    protected fun initNote(){
        state.initExecutor(binding)
        noteKindListener?.let { listener-> state.setNoteKindsListener(listener)}
    }

    abstract fun defaultOnCreateMenu(menu: Menu, inflater: MenuInflater)
    abstract fun transNullToNote(noteKinds: NoteKinds)
    abstract fun recoverNote()

    fun setNoteKindsListener(listener:(noteKind:NoteKinds)->Unit){
        state.setNoteKindsListener(listener)
        noteKindListener=listener
    }

    fun setCurrentState(state: NoteStateAbstract<T>){
        this.state=state
        initNote()
    }
    abstract fun setCurrentState(noteKinds: NoteKinds)


    fun setOnPrepareMenu(menu: Menu){
        state.setOnPrepareMenu(menu)
    }

    fun setOnCreateMenu(menu: Menu, inflater: MenuInflater){
        state.setOnCreateMenu(menu,inflater)
    }


}