package com.masterplus.notex.roomdb.repos.abstraction

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.models.copymove.CopyMoveItem
import com.masterplus.notex.roomdb.entities.Note
import com.masterplus.notex.roomdb.models.UltimateNote
import com.masterplus.notex.roomdb.models.UnitedNote

interface INoteRepo {
    suspend fun insertNote(note: Note):Long
    suspend fun deleteNote(note:Note)
    suspend fun updateNotes(notes:List<Note>)
    suspend fun updateNote(note:Note)
    suspend fun getUnitedNote(noteId:Long):UnitedNote
    fun getUltimateNotes():PagingSource<Int,UltimateNote>
    suspend fun changeColor(color:String,noteId:Long)
    suspend fun changeNoteKinds(noteId: Long,noteKinds: NoteKinds)
    suspend fun setBookId(noteId: Long,bookId:Long)
    suspend fun updateBookIdAndIsAllVisible(noteId: Long,bookId: Long,isAllTypeVisible:Boolean)
    suspend fun updateBookId(noteId: Long,bookId:Long)
    suspend fun updateAllTypeVisibleWithNoteId(noteId: Long,isAllTypeVisible:Boolean)

    fun getLiveCopyMoveItemsFromCheckListNotes():LiveData<List<CopyMoveItem>>
    suspend fun updateDateWithNoteId(noteId: Long,updateDate:String)
    fun getLiveBookId(noteId: Long):LiveData<Long>


    suspend fun setPinnedNote(noteId: Long)
    suspend fun resetPinnedNote(noteId: Long)
    suspend fun isAnyNoteItemExists():Boolean

}