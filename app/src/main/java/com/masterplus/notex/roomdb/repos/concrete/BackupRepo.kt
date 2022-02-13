package com.masterplus.notex.roomdb.repos.concrete

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.masterplus.notex.dependencyinjection.SettingPreferences
import com.masterplus.notex.enums.OrderNote
import com.masterplus.notex.roomdb.entities.*
import com.masterplus.notex.roomdb.models.backups.BackupNote
import com.masterplus.notex.roomdb.models.backups.UnitedBackup
import com.masterplus.notex.roomdb.repos.abstraction.IBackupRepo
import com.masterplus.notex.roomdb.repos.abstraction.IReminderAlarmRepo
import com.masterplus.notex.roomdb.services.BackupDao
import javax.inject.Inject

class BackupRepo @Inject constructor(private val backupDao: BackupDao,
                                     private val reminderAlarmRepo: IReminderAlarmRepo,
                                     private val sharedPreferences: SharedPreferences,
                                     @SettingPreferences private val settingPreferences: SharedPreferences,
                                     private val context: Context
) :
    IBackupRepo {

    override suspend fun formBackupUnitedBackup():UnitedBackup{
        val backupNotes= mutableListOf<BackupNote>()
        backupDao.getAllNotes().forEach { note->
            val noteId=note.uid
            val contentNotes=backupDao.getBackUpContentsWithNoteId(noteId)
            val tags=backupDao.getTagNamesWithNoteId(noteId)
            val bookName=backupDao.getBookName(note.bookId)
            val reminder=backupDao.getBackupReminderWithNoteId(noteId)
            val trash=backupDao.getBackupTrashesWithNoteId(noteId)

            val backupNote= BackupNote(note.title,note.typeContent,note.kindNote,note.weight,note.isCheck,note.color
                ,note.updateDate,note.allTypeVisible,contentNotes, tags, bookName, reminder, trash)
            backupNotes.add(backupNote)
        }
        val tags=backupDao.getBackupTags()
        val books=backupDao.getBackupBooks()
        return UnitedBackup(backupNotes,tags, books,getSettings())
    }

    override suspend fun loadUnitedBackup(unitedBackup: UnitedBackup){
        backupDao.insertBooks(unitedBackup.books.map { Book(it.name,isVisibleItems = it.isVisibleItems) })
        backupDao.insertTags(unitedBackup.tags.map { Tag(it.name)})
        setSettings(unitedBackup.settings)
        unitedBackup.notes.forEach { backupNote ->
            val book:Book=backupDao.getBookWithName(backupNote.bookName?:"")?:Book("",isVisibleItems = false)
            val note=Note(backupNote.title,backupNote.typeContent,backupNote.kindNote,backupNote.weight,backupNote.isCheck
                ,backupNote.color,backupNote.updateDate,backupNote.allTypeVisible,book.bookId)
            val noteId=backupDao.insertNote(note)
            backupDao.insertContentNotes(backupNote.contentNotes.map { ContentNote(noteId,it.text,it.weight,it.isCheck) })

            backupNote.reminder?.let {
                Reminder(noteId,it.createdDate,it.nextDate,it.reminderType,it.isCompleted).let {reminder->
                    backupDao.insertReminder(reminder)
                    reminderAlarmRepo.insertOrUpdateNextAlarm(reminder)
                }
            }
            backupNote.trash?.let { backupDao.insertTrash(Trash(noteId,it.date)) }
            backupNote.tags.forEach { tagName->
                backupDao.getTagIdWithName(tagName)?.let { tagId->backupDao.insertTagWithNote(
                    TagNoteCrossRef(noteId, tagId)
                ) }
            }
        }

    }
    override suspend fun deleteContentTables(){
        backupDao.getNoteIds().forEach { noteId->
            reminderAlarmRepo.deleteReminderAlarm(noteId)
        }
        backupDao.deleteAllNotes()
        backupDao.deleteAllContentNotes()
        backupDao.deleteAllBooks()
        backupDao.deleteAllNoteWithTags()
        backupDao.deleteAllTags()
        backupDao.deleteAllTrashes()
        backupDao.deleteAllReminders()
    }


    private fun getSettings():Map<String,Any>{
        val settings= mutableMapOf<String,Any>()
        sharedPreferences.all.entries.forEach {
            it.value?.let { value-> settings[it.key] = value}
        }
        settingPreferences.all.entries.forEach {
            it.value?.let { value-> settings[it.key] = value }
        }
        return settings
    }
    private fun setSettings(settings:Map<String,Any>){
        sharedPreferences.apply {
            edit().putString("orderNote", getOrDefault(settings,"orderNote",OrderNote.EDIT_TIME.toString())).apply()
            edit().putBoolean("isContentsVisible", getOrDefault(settings,"isContentsVisible",true)).apply()
            edit().putBoolean("isTagsVisible", getOrDefault(settings,"isTagsVisible",true)).apply()
            edit().putBoolean("isTagsVisible", getOrDefault(settings,"isTagsVisible",true)).apply()
            edit().putBoolean("isFirstAddCheckNoteItem",getOrDefault(settings,"isFirstAddCheckNoteItem",true)).apply()
            edit().putInt("rootNoteSpanSize", getOrDefault(settings,"rootNoteSpanSize",1)).apply()
            edit().putBoolean("isDescendingOrder", getOrDefault(settings,"isDescendingOrder",true)).apply()
            edit().putInt("font_size_text", getOrDefault(settings,"font_size_text",18)).apply()
        }

        settingPreferences.apply {
            edit().putString("setSelectLang",getOrDefault(settings,"setSelectLang","en")).apply()
            edit().putBoolean("logOutBackupLocalAuto",getOrDefault(settings,"logOutBackupLocalAuto",true)).apply()
            edit().putBoolean("logOutBackupCloudAuto",getOrDefault(settings,"logOutBackupCloudAuto",true)).apply()
        }

    }

    private inline fun<reified T> getOrDefault(settings:Map<String,Any>, key:String, default:T):T{
        settings[key]?.let {
            if(it is T) return it
            if(it is Number && default is Int) return it.toInt() as T
        }
        return default
    }

}