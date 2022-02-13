package com.masterplus.notex.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.models.NullToNoteObject
import com.masterplus.notex.models.ParameterNote
import com.masterplus.notex.roomdb.entities.Book
import com.masterplus.notex.roomdb.entities.Reminder
import com.masterplus.notex.roomdb.entities.Tag
import com.masterplus.notex.roomdb.models.UnitedNote
import com.masterplus.notex.roomdb.repos.abstraction.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch


abstract class NoteBaseViewModel(application: Application,
                                 private val externalScope: CoroutineScope,
                                 private val trashRepo: ITrashRepo,
                                 private val noteRepo: INoteRepo,
                                 private val tagRepo: ITagRepo,
                                 private val reminderRepo: IReminderRepo,private val bookRepo: IBookRepo)
    : AndroidViewModelBase(application) {

    var stateParameterNote: ParameterNote?=null

    protected var isNullToNoteHappened=false

    fun getLiveReminder(noteId: Long):LiveData<Reminder?> = reminderRepo.getLiveReminderWithNoteId(noteId)

    abstract fun saveNote(note:UnitedNote,lastSavedNote: UnitedNote)

    abstract fun lastCheckNote(note:UnitedNote)

    private val mutableIsRecoverNote= MutableSharedFlow<Boolean>()
    val isRecoverNote:SharedFlow<Boolean> get() = mutableIsRecoverNote

    private val mutableNote=MutableLiveData<UnitedNote>()
    val note:LiveData<UnitedNote> get() = mutableNote

    private val mutableBook=MutableLiveData<Book>()
    val book:LiveData<Book> get() = mutableBook

    private val mutableTags=MutableLiveData<List<Tag>>()
    val tags:LiveData<List<Tag>> get() = mutableTags

    protected val mutableIsNoteSaved = MutableSharedFlow<Boolean>()
    val isNoteSaved:SharedFlow<Boolean> get() = mutableIsNoteSaved

    fun signalToLoadCurrentNote(noteId: Long) {
        viewModelScope.launch {
            mutableNote.value= noteRepo.getUnitedNote(noteId).apply {
                contents.sortBy { it.weight }
            }
        }
    }
    protected suspend fun loadCurrentNote(noteId: Long) {
        viewModelScope.launch {
            mutableNote.value= noteRepo.getUnitedNote(noteId).apply {
                contents.sortBy { it.weight }
            }
        }
    }
    fun getLiveBook(noteId: Long):LiveData<Book?>{
        return Transformations.switchMap(noteRepo.getLiveBookId(noteId)){
            bookRepo.getNullableLiveBook(it)
        }
    }

    fun getLiveTags(noteId: Long) = tagRepo.getLiveTagsForNote(noteId)

    fun setBookId(noteId: Long,bookId: Long){
        externalScope.launch {
            noteRepo.setBookId(noteId, bookId)
        }
    }
    fun setTagWithIds(tagId:Long,noteId: Long){
        externalScope.launch {
            tagRepo.addRelationTagWithNote(tagId, noteId)
        }
    }

    fun changeColor(color: String, noteId: Long) {
        externalScope.launch {
            noteRepo.changeColor(color, noteId)
        }
    }

    fun sendNoteToTrashWithNoteId(noteId:Long){
        externalScope.launch {
            trashRepo.sendNoteToTrashWithNoteId(noteId)
        }
    }
    fun changeNoteKindsWithNoteId(noteId: Long,noteKinds: NoteKinds){
        externalScope.launch {
            noteRepo.changeNoteKinds(noteId, noteKinds)
        }
    }

    fun deleteNoteForEver(noteId: Long){
        externalScope.launch {
            trashRepo.removeNoteForEver(noteId)
        }
    }
    fun recoverNote(noteId: Long){
        externalScope.launch {
            trashRepo.recoverNote(noteId)
            mutableIsRecoverNote.emit(true)
        }
    }


}