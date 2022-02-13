package com.masterplus.notex.roomdb.repos.concrete

import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.roomdb.entities.Trash
import com.masterplus.notex.roomdb.repos.abstraction.IReminderAlarmRepo
import com.masterplus.notex.roomdb.repos.abstraction.IReminderRepo
import com.masterplus.notex.roomdb.repos.abstraction.ITrashRepo
import com.masterplus.notex.roomdb.services.ContentNoteDao
import com.masterplus.notex.roomdb.services.NoteDao
import com.masterplus.notex.roomdb.services.TagDao
import com.masterplus.notex.roomdb.services.TrashDao
import com.masterplus.notex.utils.Utils
import javax.inject.Inject

class TrashRepo @Inject constructor(private val trashDao: TrashDao,
                                    private val contentNoteDao: ContentNoteDao,
                                    private val noteDao: NoteDao,
                                    private val tagDao: TagDao,
                                    private val alarmRepo: IReminderAlarmRepo,
                                    private val reminderRepo: IReminderRepo
):ITrashRepo {
    override suspend fun sendNotesToTrashWithBookId(bookId: Long) {

        noteDao.getNoteIdsFromBookId(bookId).forEach { noteId->
            reminderRepo.deleteReminderWithNoteId(noteId)
            alarmRepo.deleteReminderAlarm(noteId)
            trashDao.insertTrash(Trash(noteId,Utils.getFormatDate()))
        }
        trashDao.sendNotesToTrashWithBookId(bookId)

    }

    override suspend fun sendNoteToTrashWithNoteId(noteId: Long) {
        trashDao.sendNoteToTrashWithNoteId(noteId)
        trashDao.insertTrash(Trash(noteId,Utils.getFormatDate()))
        reminderRepo.deleteReminderWithNoteId(noteId)
        alarmRepo.deleteReminderAlarm(noteId)

    }

    override suspend fun removeNoteForEver(noteId: Long) {
        trashDao.deleteTrashWithNoteId(noteId)
        contentNoteDao.deleteContentNotesWithNoteId(noteId)
        noteDao.deleteNoteWithNoteId(noteId)
        tagDao.deleteTagWithNoteWithNoteId(noteId)
    }

    override suspend fun recoverNote(noteId: Long) {
        trashDao.deleteTrashWithNoteId(noteId)
        noteDao.changeNoteKinds(noteId,NoteKinds.ALL_KIND)
    }
}