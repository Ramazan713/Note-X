package com.masterplus.notex.designpatterns.strategy

import android.content.Context
import com.masterplus.notex.enums.NoteFlags
import com.masterplus.notex.enums.NoteKinds
import java.io.Serializable

abstract class RootNoteItemAbstract:Serializable {

    abstract fun getTitle(context: Context):String
    abstract fun getNoteKinds():List<NoteKinds>
    abstract fun getBackgroundEmptyListDescription(context: Context):String
    abstract fun getImageTitleDrawableId():Int
    abstract fun getBackgroundDrawableIdForEmptyNoteList():Int

    open fun getNoteFlag():NoteFlags = NoteFlags.DEFAULT_NOTE
    open fun getNoteKind():NoteKinds = NoteKinds.ALL_KIND
}