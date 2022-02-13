package com.masterplus.notex.roomdb.repos.abstraction

import com.masterplus.notex.roomdb.entities.ContentNote

interface IContentNoteRepo {
    suspend fun insertContentNotes(contentNotes:List<ContentNote>):List<Long>
    suspend fun insertContentNote(contentNote: ContentNote):Long
    suspend fun updateContent(contentNote: ContentNote):Int
    suspend fun updateContentNotes(contentNotes: List<ContentNote>):Int
    suspend fun deleteContentNotes(contentNotes: List<ContentNote>)

    suspend fun updateNoteIdWithUid(contentNoteId:Long,noteId: Long)
    suspend fun getContentNoteWithUid(contentNoteId: Long):ContentNote

}