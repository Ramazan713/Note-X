package com.masterplus.notex.roomdb.services

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.masterplus.notex.roomdb.entities.*
import com.masterplus.notex.roomdb.models.backups.*

@Dao
interface BackupDao {

    @Query("select * from notes")
    suspend fun getAllNotes():List<Note>

    @Query("select * from contentNotes where noteId=:noteId")
    suspend fun getBackUpContentsWithNoteId(noteId:Long):List<BackupContentNote>

    @Query("select name from books where bookId=:bookId")
    suspend fun getBookName(bookId:Long):String?

    @Query("select t.name from tags t inner join noteWithTags nwt on nwt.tagId=t.tagId where nwt.noteId=:noteId")
    suspend fun getTagNamesWithNoteId(noteId: Long):List<String>

    @Query("select * from reminders where noteId=:noteId")
    suspend fun getBackupReminderWithNoteId(noteId: Long):BackupReminder?

    @Query("select * from trashes where noteId=:noteId")
    suspend fun getBackupTrashesWithNoteId(noteId: Long):BackupTrash?

    @Query("select * from tags")
    suspend fun getBackupTags():List<BackupTag>

    @Query("select * from books")
    suspend fun getBackupBooks():List<BackupBook>

    @Query("select * from books where name=:name")
    suspend fun getBookWithName(name:String):Book?

    @Query("select tagId from tags where name=:name")
    suspend fun getTagIdWithName(name: String):Long?

    @Query("select noteId from notes")
    suspend fun getNoteIds():List<Long>

    @Insert
    suspend fun insertTags(tags:List<Tag>)

    @Insert
    suspend fun insertBooks(books:List<Book>)

    @Insert
    suspend fun insertContentNotes(contentNotes:List<ContentNote>)

    @Insert
    suspend fun insertNote(note:Note):Long

    @Insert
    suspend fun insertReminder(reminder:Reminder)

    @Insert
    suspend fun insertTrash(trash:Trash)

    @Insert
    suspend fun insertTagWithNote(tagNoteCrossRef: TagNoteCrossRef)

    @Query("delete from notes")
    suspend fun deleteAllNotes()

    @Query("delete from contentNotes")
    suspend fun deleteAllContentNotes()

    @Query("delete from books")
    suspend fun deleteAllBooks()

    @Query("delete from tags")
    suspend fun deleteAllTags()

    @Query("delete from noteWithTags")
    suspend fun deleteAllNoteWithTags()

    @Query("delete from trashes")
    suspend fun deleteAllTrashes()

    @Query("delete from reminders")
    suspend fun deleteAllReminders()



}