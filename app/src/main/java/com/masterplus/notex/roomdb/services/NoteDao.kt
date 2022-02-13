package com.masterplus.notex.roomdb.services

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.models.copymove.CopyMoveItem
import com.masterplus.notex.roomdb.entities.Note
import com.masterplus.notex.roomdb.models.UltimateNote
import com.masterplus.notex.roomdb.models.UnitedNote

@Dao
interface NoteDao {

    @Update
    suspend fun updateNote(note: Note):Int

    @Update
    suspend fun updateNotes(notes: List<Note>):Int

    @Insert
    suspend fun insertNote(note:Note):Long

    @Delete
    suspend fun deleteNote(note: Note)

    @Transaction
    @Query("select * from notes where noteId=:noteId")
    suspend fun getUnitedNoteById(noteId:Long):UnitedNote

    @Transaction
    @Query("select * from noteView")
    fun getUltimateNotes(): PagingSource<Int, UltimateNote>

    @Query("update notes set color=:color where noteId=:noteId")
    suspend fun changeColor(color: String, noteId: Long)

    @Query("update notes set kindNote=:noteKinds where noteId=:noteId")
    suspend fun changeNoteKinds(noteId: Long,noteKinds: NoteKinds)

    @Query("delete from notes where noteId=:noteId")
    suspend fun deleteNoteWithNoteId(noteId: Long)


    @Query("update notes set weight=(select max(weight) from notes)+1 where noteId=:noteId")
    suspend fun setPinnedNote(noteId: Long)

    @Query("update notes set weight=0 where noteId=:noteId")
    suspend fun resetPinnedNote(noteId: Long)

    @Query("update notes set bookId=:bookId where noteId=:noteId")
    suspend fun updateBookId(noteId: Long,bookId:Long)

    @Query("""update notes set allTypeVisible=:isAllTypeVisible,bookId=:bookId where noteId=:noteId""")
    suspend fun updateBookIdAndIsAllVisible(noteId: Long,bookId: Long,isAllTypeVisible:Boolean)

    @Query("update notes set allTypeVisible=:isAllTypeVisible where noteId=:noteId")
    suspend fun updateAllTypeVisibleWithNoteId(noteId: Long,isAllTypeVisible:Boolean)

    @Query("select noteId from notes where bookId=:bookId")
    suspend fun getNoteIdsFromBookId(bookId: Long):List<Long>

    @Query("""select n.noteId uid,n.title name,(select count(*) from contentNotes where noteId=n.noteId ) count,
        0 isLastImage from notes n  where n.typeContent='CheckList' and n.kindNote!='TRASH_KIND' order by n.noteId desc """)
    fun getLiveCopyMoveItemsFromCheckListNotes():LiveData<List<CopyMoveItem>>

    @Query("update notes set updateDate=:updateDate where noteId=:noteId")
    suspend fun updateDateWithNoteId(noteId: Long,updateDate:String)

    @Query("select bookId from notes where noteId=:noteId")
    fun getLiveBookId(noteId: Long):LiveData<Long>


    @Query("""select exists( select 1 where 1 in (select exists(select 1 from notes) 
        union all select exists(select 1 from books) 
        union all  select exists(select 1 from tags))
        )""")
    suspend fun isAnyNoteItemExists():Boolean
}