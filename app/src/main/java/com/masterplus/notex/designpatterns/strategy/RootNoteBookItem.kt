package com.masterplus.notex.designpatterns.strategy

import android.content.Context
import com.masterplus.notex.R
import com.masterplus.notex.enums.NoteFlags
import com.masterplus.notex.enums.NoteKinds
import javax.inject.Inject

class RootNoteBookItem @Inject constructor(private val title: String)
    : RootNoteItemAbstract() {
    override fun getTitle(context: Context): String = title


    override fun getNoteKinds(): List<NoteKinds> = arrayListOf(NoteKinds.ARCHIVE_KIND,NoteKinds.ALL_KIND)

    override fun getBackgroundEmptyListDescription(context: Context): String = context.getString(R.string.added_note_appear_text)

    override fun getImageTitleDrawableId(): Int = R.drawable.ic_baseline_library_books_24

    override fun getBackgroundDrawableIdForEmptyNoteList(): Int = R.drawable.ic_baseline_note_add_24

    override fun getNoteFlag(): NoteFlags = NoteFlags.BOOK_FROM_NOTE
}