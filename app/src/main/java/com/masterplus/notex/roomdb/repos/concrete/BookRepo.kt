package com.masterplus.notex.roomdb.repos.concrete

import androidx.lifecycle.LiveData
import com.masterplus.notex.models.copymove.CopyMoveItem
import com.masterplus.notex.roomdb.entities.Book
import com.masterplus.notex.roomdb.repos.abstraction.IBookRepo
import com.masterplus.notex.roomdb.services.BookDao
import com.masterplus.notex.roomdb.views.BookCountView
import javax.inject.Inject

class BookRepo @Inject constructor(private val bookDao: BookDao):IBookRepo {
    override fun getLiveBookCountViews(): LiveData<List<BookCountView>> {
        return bookDao.getLiveBookCountViews()
    }

    override suspend fun getBookCountView(bookId: Long): BookCountView {
        return bookDao.getBookCountView(bookId)
    }

    override suspend fun insertBook(book: Book):Long{
        return bookDao.insertBook(book)
    }
    override suspend fun updateNoteBookId(noteId: Long, bookId: Long) {
        bookDao.updateNoteBookId(bookId, noteId)
    }

    override suspend fun getBook(bookId: Long): Book {
        return bookDao.getBook(bookId)
    }

    override suspend fun renameBook(newName: String, bookId: Long) {
        bookDao.renameBook(newName, bookId)
    }

    override suspend fun deleteBookWithId(bookId: Long) {
        bookDao.deleteBookWithId(bookId)
        bookDao.deleteBookIdFromNote(bookId)
    }

    override suspend fun changeBookAttrVisibility(bookId: Long, isAllTypeVisible: Boolean) {
        bookDao.changeBookAttrVisibility(bookId, isAllTypeVisible)
        bookDao.changeBookAttrVisibilityForNote(bookId, isAllTypeVisible)
    }

    override suspend fun getUnSelectedNoteForBook(): Int {
        return bookDao.getUnSelectedNoteForBook()
    }

    override fun getAllLiveCopyMoveItemsFromBooks(): LiveData<List<CopyMoveItem>> {
        return bookDao.getAllLiveCopyMoveItemsFromBooks()
    }

    override fun getNullableLiveBook(bookId: Long): LiveData<Book?> {
        return bookDao.getNullableLiveBook(bookId)
    }


}