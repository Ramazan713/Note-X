package com.masterplus.notex.roomdb.repos.abstraction

import com.masterplus.notex.roomdb.entities.Reminder
import java.util.*

interface IReminderAlarmRepo {
    suspend fun setReminderAlarm(reminder: Reminder, calendar: Calendar)
    suspend fun deleteReminderAlarm(reminder: Reminder)
    suspend fun deleteReminderAlarm(noteId:Long)
    suspend fun setReminderAlarmWithSafe(reminder: Reminder, calendar: Calendar)
    suspend fun reloadAlarmIfReminderExists(reminder: Reminder,calendar: Calendar)
    suspend fun insertOrUpdateNextAlarm(reminder: Reminder)
    suspend fun updateNextAlarm(reminder: Reminder)
    suspend fun reloadReminderAlarms()
}