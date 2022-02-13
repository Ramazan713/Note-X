package com.masterplus.notex.roomdb.repos.concrete

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.models.copymove.CopyMoveItem
import com.masterplus.notex.roomdb.entities.Note
import com.masterplus.notex.roomdb.models.UltimateNote
import com.masterplus.notex.roomdb.models.UnitedNote
import com.masterplus.notex.roomdb.repos.abstraction.INoteRepo
import com.masterplus.notex.roomdb.services.NoteDao
import javax.inject.Inject

class NoteRepo @Inject constructor(private val noteDao: NoteDao):INoteRepo {
    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    override suspend fun deleteNote(note: Note){
        noteDao.deleteNote(note)
    }

    override suspend fun updateNotes(notes: List<Note>) {
        noteDao.updateNotes(notes)
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    override suspend fun getUnitedNote(noteId: Long): UnitedNote {
        return noteDao.getUnitedNoteById(noteId)
    }

    override fun getUltimateNotes(): PagingSource<Int, UltimateNote> {
        return noteDao.getUltimateNotes()
    }

    override suspend fun changeColor(color: String, noteId: Long) {
        noteDao.changeColor(color, noteId)
    }

    override suspend fun changeNoteKinds(noteId: Long, noteKinds: NoteKinds) {
        noteDao.changeNoteKinds(noteId, noteKinds)
    }

    override suspend fun setBookId(noteId: Long, bookId: Long) {
        noteDao.updateBookId(noteId, bookId)
    }

    override suspend fun updateBookIdAndIsAllVisible(noteId: Long, bookId: Long, isAllTypeVisible: Boolean) {
        noteDao.updateBookIdAndIsAllVisible(noteId,bookId, isAllTypeVisible)
    }

    override suspend fun updateBookId(noteId: Long, bookId: Long) {
        noteDao.updateBookId(noteId, bookId)
    }


    override suspend fun updateAllTypeVisibleWithNoteId(noteId: Long, isAllTypeVisible: Boolean) {
        noteDao.updateAllTypeVisibleWithNoteId(noteId, isAllTypeVisible)
    }

    override fun getLiveCopyMoveItemsFromCheckListNotes(): LiveData<List<CopyMoveItem>> {
        return noteDao.getLiveCopyMoveItemsFromCheckListNotes()
    }

    override suspend fun updateDateWithNoteId(noteId: Long, updateDate: String) {
        noteDao.updateDateWithNoteId(noteId, updateDate)
    }

    override fun getLiveBookId(noteId: Long): LiveData<Long> {
        return noteDao.getLiveBookId(noteId)
    }


    override suspend fun setPinnedNote(noteId: Long) {
        noteDao.setPinnedNote(noteId)
    }

    override suspend fun resetPinnedNote(noteId: Long) {
        noteDao.resetPinnedNote(noteId)
    }

    override suspend fun isAnyNoteItemExists(): Boolean {
        return noteDao.isAnyNoteItemExists()
    }
}