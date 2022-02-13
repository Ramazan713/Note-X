package com.masterplus.notex.roomdb.repos.abstraction

interface ITrashRepo {
    suspend fun sendNotesToTrashWithBookId(bookId:Long)
    suspend fun sendNoteToTrashWithNoteId(noteId:Long)
    suspend fun removeNoteForEver(noteId: Long)
    suspend fun recoverNote(noteId: Long)

}