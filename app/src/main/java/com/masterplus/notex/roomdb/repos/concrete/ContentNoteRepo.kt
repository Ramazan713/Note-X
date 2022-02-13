package com.masterplus.notex.roomdb.repos.concrete

import com.masterplus.notex.roomdb.entities.ContentNote
import com.masterplus.notex.roomdb.repos.abstraction.IContentNoteRepo
import com.masterplus.notex.roomdb.services.ContentNoteDao
import javax.inject.Inject

class ContentNoteRepo @Inject constructor(private val contentNoteDao:ContentNoteDao):IContentNoteRepo {
    override suspend fun insertContentNotes(contentNotes: List<ContentNote>): List<Long> {
        return contentNoteDao.insertContentNotes(contentNotes)
    }

    override suspend fun insertContentNote(contentNote: ContentNote): Long {
        return  contentNoteDao.insertContentNote(contentNote)
    }

    override suspend fun updateContent(contentNote: ContentNote):Int{
        return contentNoteDao.updateContentNote(contentNote)
    }

    override suspend fun updateContentNotes(contentNotes: List<ContentNote>): Int {
        return contentNoteDao.updateContentNotes(contentNotes)
    }

    override suspend fun deleteContentNotes(contentNotes: List<ContentNote>) {
        contentNoteDao.deleteContentNotes(contentNotes)
    }

    override suspend fun updateNoteIdWithUid(contentNoteId: Long, noteId: Long) {
        contentNoteDao.updateNoteIdWithUid(contentNoteId, noteId)
    }

    override suspend fun getContentNoteWithUid(contentNoteId: Long): ContentNote {
        return contentNoteDao.getContentNoteWithUid(contentNoteId)
    }
}