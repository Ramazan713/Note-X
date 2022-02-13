package com.masterplus.notex.roomdb.repos.abstraction

import androidx.lifecycle.LiveData
import com.masterplus.notex.models.copymove.CopyMoveItem
import com.masterplus.notex.roomdb.entities.Book
import com.masterplus.notex.roomdb.views.BookCountView

interface IBookRepo {
    fun getLiveBookCountViews():LiveData<List<BookCountView>>
    suspend fun getBookCountView(bookId:Long):BookCountView
    suspend fun insertBook(book:Book):Long
    suspend fun updateNoteBookId(noteId: Long,bookId:Long)
    suspend fun getBook(bookId: Long):Book
    suspend fun renameBook(newName:String,bookId: Long)
    suspend fun deleteBookWithId(bookId: Long)
    suspend fun changeBookAttrVisibility(bookId: Long,isAllTypeVisible:Boolean)
    suspend fun getUnSelectedNoteForBook():Int

    fun getAllLiveCopyMoveItemsFromBooks():LiveData<List<CopyMoveItem>>
    fun getNullableLiveBook(bookId: Long):LiveData<Book?>

}