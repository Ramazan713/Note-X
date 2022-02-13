package com.masterplus.notex.roomdb.services

import androidx.room.*
import com.masterplus.notex.roomdb.entities.ContentNote
@Dao
interface ContentNoteDao {
    @Insert
    suspend fun insertContentNotes(contentNotes:List<ContentNote>):List<Long>

    @Insert
    suspend fun insertContentNote(contentNote: ContentNote):Long

    @Update
    suspend fun updateContentNote(contentNote: ContentNote):Int

    @Update
    suspend fun updateContentNotes(contentNotes: List<ContentNote>):Int

    @Delete
    suspend fun deleteContentNotes(contentNotes: List<ContentNote>)

    @Query("delete from contentNotes where noteId=:noteId")
    suspend fun deleteContentNotesWithNoteId(noteId:Long)


    @Query("update contentNotes set noteId=:noteId where uid=:contentNoteId")
    suspend fun updateNoteIdWithUid(contentNoteId:Long,noteId: Long)

    @Query("select * from contentNotes where uid=:contentNoteId")
    suspend fun getContentNoteWithUid(contentNoteId: Long):ContentNote



}