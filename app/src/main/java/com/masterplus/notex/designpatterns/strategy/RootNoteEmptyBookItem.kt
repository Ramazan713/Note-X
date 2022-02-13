package com.masterplus.notex.designpatterns.strategy

import android.content.Context
import com.masterplus.notex.R
import com.masterplus.notex.enums.NoteKinds

class RootNoteEmptyBookItem
    :RootNoteItemAbstract() {
    override fun getTitle(context: Context): String = context.getString(R.string.unselected_text)

    override fun getNoteKinds(): List<NoteKinds> = arrayListOf(NoteKinds.ALL_KIND,NoteKinds.ARCHIVE_KIND)

    override fun getBackgroundEmptyListDescription(context: Context): String = context.getString(R.string.added_note_appear_text)

    override fun getImageTitleDrawableId(): Int = R.drawable.ic_baseline_library_books_24

    override fun getBackgroundDrawableIdForEmptyNoteList(): Int = R.drawable.ic_baseline_note_add_24
}