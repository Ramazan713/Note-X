package com.masterplus.notex.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.masterplus.notex.roomdb.entities.*
import com.masterplus.notex.roomdb.services.*
import com.masterplus.notex.roomdb.views.*

@Database(entities = [Book::class,ContentNote::class, Note::class,Reminder::class,
    Tag::class,TagNoteCrossRef::class,Trash::class,BackupFile::class],
    views = [BookCountView::class,CompletedNoteView::class,ContentBriefView::class,
            TagCountView::class,TagsTextView::class],version = 1)
abstract class NoteDatabase:RoomDatabase() {
    abstract fun noteDao():NoteDao
    abstract fun contentNoteDao():ContentNoteDao
    abstract fun tagDao():TagDao
    abstract fun reminderDao():ReminderDao
    abstract fun bookDao():BookDao
    abstract fun trashDao():TrashDao
    abstract fun ultimateNoteDao():UltimateNoteDao
    abstract fun backupFileDao():BackupFileDao
    abstract fun backupDao():BackupDao
}