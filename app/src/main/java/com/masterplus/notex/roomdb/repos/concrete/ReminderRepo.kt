package com.masterplus.notex.roomdb.repos.concrete

import androidx.lifecycle.LiveData
import com.masterplus.notex.roomdb.entities.Reminder
import com.masterplus.notex.roomdb.models.NoteReminder
import com.masterplus.notex.roomdb.repos.abstraction.IReminderRepo
import com.masterplus.notex.roomdb.services.ReminderDao
import javax.inject.Inject

class ReminderRepo @Inject constructor(private val reminderDao: ReminderDao):IReminderRepo {
    override suspend fun getReminderWithNoteId(noteId: Long): Reminder? {
        return reminderDao.getReminderWithNoteId(noteId)
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    override suspend fun deleteReminderWithNoteId(noteId: Long) {
        reminderDao.deleteReminderWithNoteId(noteId)
    }

    override fun getLiveReminderWithNoteId(noteId: Long): LiveData<Reminder?> {
        return reminderDao.getLiveReminderWithNoteId(noteId)
    }


    override suspend fun getNoteReminderWithNoteId(noteId: Long): NoteReminder? {
        return reminderDao.getNoteReminderWithNoteId(noteId)
    }


}