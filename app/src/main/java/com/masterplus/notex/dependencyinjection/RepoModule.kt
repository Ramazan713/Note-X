package com.masterplus.notex.dependencyinjection

import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import com.masterplus.notex.api.*
import com.masterplus.notex.api.services.GDriveService
import com.masterplus.notex.managers.AuthManager
import com.masterplus.notex.managers.GDriveManager
import com.masterplus.notex.roomdb.repos.abstraction.*
import com.masterplus.notex.roomdb.repos.concrete.*
import com.masterplus.notex.roomdb.services.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {
    @Singleton
    @Provides
    fun injectNoteRepo(noteDao:NoteDao) = NoteRepo(noteDao) as INoteRepo

    @Singleton
    @Provides
    fun injectContentNoteRepo(contentNoteDao: ContentNoteDao) = ContentNoteRepo(contentNoteDao) as IContentNoteRepo

    @Singleton
    @Provides
    fun injectTagRepo(tagDao: TagDao) = TagRepo(tagDao) as ITagRepo

    @Singleton
    @Provides
    fun injectBookRepo(bookDao: BookDao) = BookRepo(bookDao) as IBookRepo

    @Singleton
    @Provides
    fun injectTrashRepo(trashDao: TrashDao,contentNoteDao: ContentNoteDao,noteDao: NoteDao,
                        tagDao: TagDao,reminderRepo: IReminderRepo,reminderAlarmRepo: IReminderAlarmRepo) =
        TrashRepo(trashDao,contentNoteDao, noteDao, tagDao,reminderAlarmRepo,reminderRepo) as ITrashRepo

    @Singleton
    @Provides
    fun injectUltimateNoteRepo(ultimateNoteDao: UltimateNoteDao) = UltimateNoteRepo(ultimateNoteDao) as IUltimateNoteRepo

    @Singleton
    @Provides
    fun injectReminderRepo(reminderDao: ReminderDao) = ReminderRepo(reminderDao) as IReminderRepo

    @Singleton
    @Provides
    fun injectReminderAlarmRepo(reminderDao: ReminderDao,
                                @ApplicationContext context: Context,
                                alarmManager: AlarmManager) =
        ReminderAlarmRepo(context, alarmManager, reminderDao) as IReminderAlarmRepo

    @Provides
    fun injectBackupFileRepo(backupFileDao: BackupFileDao) = BackupFileRepo(backupFileDao) as IBackupFileRepo

    @Singleton
    @Provides
    fun injectGDriveRepo(authManager: AuthManager, driveRetrofit:GDriveService) =
        GDriveRepo(authManager,driveRetrofit)

    @Provides
    fun injectBackupFileManager(backupFileRepo: IBackupFileRepo, gDriveRepo: GDriveRepo,
                                sharedPreferences: SharedPreferences,context: Context)
    = GDriveManager(backupFileRepo, gDriveRepo, sharedPreferences,context)

    @Provides
    fun injectBackupRepo(backupDao: BackupDao, reminderAlarmRepo: IReminderAlarmRepo,
                         sharedPreferences: SharedPreferences, @SettingPreferences settingPreferences:SharedPreferences,
                         context: Context) =
        BackupRepo(backupDao, reminderAlarmRepo,sharedPreferences, settingPreferences,context) as IBackupRepo
}