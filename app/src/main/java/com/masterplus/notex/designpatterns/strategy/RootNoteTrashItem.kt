package com.masterplus.notex.designpatterns.strategy

import android.content.Context
import com.masterplus.notex.R
import com.masterplus.notex.enums.NoteKinds

class RootNoteTrashItem
    : RootNoteItemAbstract() {
    override fun getTitle(context: Context): String = context.getString(R.string.trash_text)

    override fun getNoteKinds(): List<NoteKinds> = arrayListOf(NoteKinds.TRASH_KIND)

    override fun getBackgroundEmptyListDescription(context: Context): String = context.getString(R.string.empty_trash_text)

    override fun getImageTitleDrawableId(): Int = R.drawable.ic_baseline_delete_24

    override fun getBackgroundDrawableIdForEmptyNoteList(): Int = R.drawable.ic_baseline_delete_sweep_24

    override fun getNoteKind(): NoteKinds = NoteKinds.TRASH_KIND
}