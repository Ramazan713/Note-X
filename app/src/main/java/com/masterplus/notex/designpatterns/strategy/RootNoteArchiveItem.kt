package com.masterplus.notex.designpatterns.strategy

import android.content.Context
import com.masterplus.notex.R
import com.masterplus.notex.enums.NoteKinds

class RootNoteArchiveItem
    : RootNoteItemAbstract() {
    override fun getTitle(context: Context): String = context.getString(R.string.archive_text)

    override fun getNoteKinds(): List<NoteKinds> = arrayListOf(NoteKinds.ARCHIVE_KIND)

    override fun getBackgroundEmptyListDescription(context: Context): String = context.getString(R.string.no_archived_note_text)

    override fun getImageTitleDrawableId(): Int = R.drawable.ic_baseline_archive_24

    override fun getBackgroundDrawableIdForEmptyNoteList(): Int = R.drawable.ic_baseline_archive_24

    override fun getNoteKind(): NoteKinds = NoteKinds.ARCHIVE_KIND
}