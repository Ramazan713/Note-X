package com.masterplus.notex.designpatterns.state

import android.view.Menu
import android.view.MenuInflater
import androidx.viewbinding.ViewBinding
import com.masterplus.notex.enums.NoteKinds

abstract class NoteStateAbstract<T>(private val context: NoteEditorContextAbstract<T>) where T:ViewBinding{

    protected var noteKindListener:((noteKind:NoteKinds)->Unit)?=null
    fun setNoteKindsListener(listener:(noteKind:NoteKinds)->Unit){
        this.noteKindListener=listener
    }

    abstract fun initExecutor(binding: T)

    abstract fun setOnCreateMenu(menu: Menu, inflater: MenuInflater)

    abstract fun setOnPrepareMenu(menu: Menu)

    abstract fun clearInitAffects(binding: T)


}