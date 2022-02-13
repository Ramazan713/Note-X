package com.masterplus.notex.designpatterns.strategy

import android.content.Context
import com.masterplus.notex.R
import com.masterplus.notex.enums.NoteKinds

class RootNoteReminderItem
    : RootNoteItemAbstract() {
    override fun getTitle(context: Context): String = context.getString(R.string.reminder_text)

    override fun getNoteKinds(): List<NoteKinds> = arrayListOf(NoteKinds.ALL_KIND,NoteKinds.ARCHIVE_KIND)

    override fun getBackgroundEmptyListDescription(context: Context): String = context.getString(R.string.note_note_with_reminder)

    override fun getImageTitleDrawableId(): Int = R.drawable.ic_baseline_notifications_24

    override fun getBackgroundDrawableIdForEmptyNoteList(): Int = R.drawable.ic_baseline_notification_add_24
}