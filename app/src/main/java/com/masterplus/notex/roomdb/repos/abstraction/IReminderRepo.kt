package com.masterplus.notex.roomdb.repos.abstraction

import androidx.lifecycle.LiveData
import com.masterplus.notex.roomdb.entities.Reminder
import com.masterplus.notex.roomdb.models.NoteReminder

interface IReminderRepo {

    suspend fun getReminderWithNoteId(noteId:Long): Reminder?

    suspend fun insertReminder(reminder: Reminder):Long

    suspend fun deleteReminder(reminder: Reminder)

    suspend fun deleteReminderWithNoteId(noteId: Long)

    fun getLiveReminderWithNoteId(noteId: Long): LiveData<Reminder?>

    suspend fun getNoteReminderWithNoteId(noteId: Long): NoteReminder?


}