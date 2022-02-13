package com.masterplus.notex.roomdb.services

import androidx.room.*
import com.masterplus.notex.roomdb.entities.Trash

@Dao
interface TrashDao {
    @Query("update notes set kindNote='TRASH_KIND' where bookId=:bookId")
    suspend fun sendNotesToTrashWithBookId(bookId:Long)

    @Query("update notes set kindNote='TRASH_KIND' where noteId=:noteId")
    suspend fun sendNoteToTrashWithNoteId(noteId:Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrash(trash: Trash)

    @Delete
    suspend fun deleteTrash(trash: Trash)

    @Query("delete from trashes where noteId=:noteId")
    suspend fun deleteTrashWithNoteId(noteId: Long)
}