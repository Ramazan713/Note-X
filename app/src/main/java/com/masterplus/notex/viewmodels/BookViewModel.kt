package com.masterplus.notex.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterplus.notex.roomdb.entities.Book
import com.masterplus.notex.roomdb.repos.concrete.BookRepo
import com.masterplus.notex.roomdb.repos.concrete.TrashRepo
import com.masterplus.notex.roomdb.views.BookCountView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(app:Application,
                                        private val externalScope:CoroutineScope,
                                        private val bookRepo: BookRepo,
                                        private val trashRepo: TrashRepo):AndroidViewModelBase(app) {

    val liveBookCountView:LiveData<List<BookCountView>> = bookRepo.getLiveBookCountViews()

    private val _unSelectedNotesCount=MutableLiveData<Int>()
    val unSelectedNotesCount:LiveData<Int> = _unSelectedNotesCount

    fun insertBook(text:String){
        externalScope.launch {
            bookRepo.insertBook(Book(text))
        }
    }

    fun renameBook(newName:String,bookId: Long){
        externalScope.launch {
            bookRepo.renameBook(newName, bookId)
        }
    }
    fun deleteBookWithId(bookId: Long){
        externalScope.launch {
            trashRepo.sendNotesToTrashWithBookId(bookId)
            bookRepo.deleteBookWithId(bookId)
        }
    }

    fun changeBookAttrVisibility(bookId: Long,isAllTypeVisible:Boolean){
        externalScope.launch {
            bookRepo.changeBookAttrVisibility(bookId, isAllTypeVisible)
        }
    }

    fun getUnSelectedNoteForBook(){
        viewModelScope.launch {
            _unSelectedNotesCount.value=bookRepo.getUnSelectedNoteForBook()
        }
    }

}