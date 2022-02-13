package com.masterplus.notex.roomdb.services

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.masterplus.notex.models.copymove.CopyMoveItem
import com.masterplus.notex.roomdb.entities.Book
import com.masterplus.notex.roomdb.views.BookCountView

@Dao
interface BookDao {

    @Insert
    suspend fun insertBook(book: Book):Long

    @Query("select * from bookCounts order by bookId desc")
    fun getLiveBookCountViews():LiveData<List<BookCountView>>

    @Query("select * from bookCounts where bookId=:bookId")
    suspend fun getBookCountView(bookId:Long):BookCountView

    @Query("update notes set bookId=:bookId where noteId=:noteId")
    suspend fun updateNoteBookId(bookId: Long,noteId:Long)

    @Query("select * from books where bookId=:bookId")
    suspend fun getBook(bookId: Long):Book

    @Query("select * from books where bookId=:bookId")
    fun getNullableLiveBook(bookId: Long):LiveData<Book?>


    @Query("update books set name=:newName where bookId=:bookId")
    suspend fun renameBook(newName:String,bookId: Long)

    @Query("delete from books where bookId=:bookId")
    suspend fun deleteBookWithId(bookId: Long)

    @Query("update notes set allTypeVisible=1,bookId=0 where bookId=:bookId")
    suspend fun deleteBookIdFromNote(bookId: Long)

    @Query("update notes set allTypeVisible=:isAllTypeVisible where bookId=:bookId")
    suspend fun changeBookAttrVisibilityForNote(bookId: Long,isAllTypeVisible:Boolean)

    @Query("update books set isVisibleItems=:isAllTypeVisible where bookId=:bookId")
    suspend fun changeBookAttrVisibility(bookId: Long,isAllTypeVisible:Boolean)

    @Query("select count(*) from notes where bookId=0 and kindNote!='TRASH_KIND'")
    suspend fun getUnSelectedNoteForBook():Int

    @Query("select name,bookId uid,size count,not isVisibleItems isLastImage from bookCounts order by bookId desc")
    fun getAllLiveCopyMoveItemsFromBooks():LiveData<List<CopyMoveItem>>
}