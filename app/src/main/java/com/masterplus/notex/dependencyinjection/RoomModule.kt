package com.masterplus.notex.dependencyinjection

import android.content.Context
import androidx.room.Room
import com.masterplus.notex.roomdb.NoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Singleton
    @Provides
    fun injectRoomDatabase(@ApplicationContext context: Context):NoteDatabase=Room.databaseBuilder(
        context,NoteDatabase::class.java,"notesDB").build()

    @Singleton
    @Provides
    fun injectNoteDao(database: NoteDatabase)=database.noteDao()

    @Singleton
    @Provides
    fun injectBookDao(database: NoteDatabase)=database.bookDao()

    @Singleton
    @Provides
    fun injectReminderDao(database: NoteDatabase)=database.reminderDao()

    @Singleton
    @Provides
    fun injectContentNoteDao(database: NoteDatabase)=database.contentNoteDao()

    @Singleton
    @Provides
    fun injectTrashDao(database: NoteDatabase)=database.trashDao()

    @Singleton
    @Provides
    fun injectTagDao(database: NoteDatabase)=database.tagDao()

    @Singleton
    @Provides
    fun injectUltimateNoteDao(database: NoteDatabase)=database.ultimateNoteDao()

    @Singleton
    @Provides
    fun injectBackupFileDao(database: NoteDatabase) = database.backupFileDao()

    @Singleton
    @Provides
    fun injectBackupRepo(database: NoteDatabase) = database.backupDao()

}