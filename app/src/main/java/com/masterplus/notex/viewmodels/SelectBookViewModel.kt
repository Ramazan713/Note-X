package com.masterplus.notex.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterplus.notex.roomdb.entities.Book
import com.masterplus.notex.roomdb.repos.abstraction.IBookRepo
import com.masterplus.notex.roomdb.repos.abstraction.INoteRepo
import com.masterplus.notex.roomdb.views.BookCountView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectBookViewModel @Inject constructor(private val bookRepo: IBookRepo,
                                              private val noteRepo: INoteRepo,
                                              private val externalScope:CoroutineScope)
    : BaseViewModel() {

    val stateSelectedBookCountView=MutableLiveData<BookCountView?>()



    val liveBookCountViews:LiveData<List<BookCountView>> = bookRepo.getLiveBookCountViews()

    private val _parentBookCountView = MutableLiveData<BookCountView>()
    val parentBookCountView:LiveData<BookCountView> get() = _parentBookCountView

    fun getBookCountView(bookId: Long){
        viewModelScope.launch {
            _parentBookCountView.value=bookRepo.getBookCountView(bookId)
        }
    }

    fun updateNoteBookId(noteId: Long, bookId: Long,isAllTypeVisible:Boolean){
        externalScope.launch {
            noteRepo.updateBookIdAndIsAllVisible(noteId, bookId, isAllTypeVisible)
        }
    }
    fun insertBook(name:String){
        externalScope.launch {
            bookRepo.insertBook(Book(name))
        }
    }

}