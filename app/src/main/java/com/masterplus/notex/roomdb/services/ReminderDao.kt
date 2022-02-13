package com.masterplus.notex.roomdb.services

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.masterplus.notex.roomdb.entities.Reminder
import com.masterplus.notex.roomdb.models.NoteReminder

@Dao
interface ReminderDao {

    @Query("select * from reminders where noteId=:noteId")
    suspend fun getReminderWithNoteId(noteId:Long): Reminder?

    @Query("select * from reminders where noteId=:noteId")
    suspend fun getNoteReminderWithNoteId(noteId: Long):NoteReminder?

    @Query("select * from reminders where noteId=:noteId")
    fun getLiveReminderWithNoteId(noteId: Long):LiveData<Reminder?>

    @Insert
    suspend fun insertReminder(reminder: Reminder):Long

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("delete from reminders where noteId=:noteId")
    suspend fun deleteReminderWithNoteId(noteId: Long)

    @Query("update reminders set isCompleted=:isCompleted where noteId=:noteId")
    suspend fun updateCompletedReminder(isCompleted:Boolean,noteId: Long)

    @Query("update reminders set nextDate=:nextDate where noteId=:noteId")
    suspend fun updateNextDateReminder(nextDate:String,noteId: Long)

    @Query("select * from reminders where isCompleted=0")
    suspend fun getUnCompletedReminders():List<Reminder>

    @Query("""select count(noteId)size from reminders where datetime('now','localtime')<=datetime(nextDate)
            and isCompleted=0""")
    suspend fun getActiveReminderCount():Int

    @Query("select exists(select 1 from reminders where noteId=:noteId)")
    suspend fun checkReminderExists(noteId: Long):Boolean

}