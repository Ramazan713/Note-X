package com.masterplus.notex.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.masterplus.notex.models.copymove.CopyMoveItem
import com.masterplus.notex.roomdb.entities.Book
import com.masterplus.notex.roomdb.entities.ContentNote
import com.masterplus.notex.roomdb.repos.abstraction.IBookRepo
import com.masterplus.notex.roomdb.repos.abstraction.IContentNoteRepo
import com.masterplus.notex.roomdb.repos.abstraction.INoteRepo
import com.masterplus.notex.roomdb.repos.abstraction.ITagRepo
import com.masterplus.notex.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CopyMoveViewModel @Inject constructor(private val noteRepo: INoteRepo,
                                            private val bookRepo: IBookRepo,
                                            private val contentNoteRepo: IContentNoteRepo,
                                            private val tagRepo: ITagRepo,
                                            private val externalScope:CoroutineScope):BaseViewModel() {

    val stateSelectedCopMoveItem=MutableLiveData<CopyMoveItem?>()


    private val mutableItems=MutableLiveData<List<CopyMoveItem>>()
    val liveItems:LiveData<List<CopyMoveItem>> get() = mutableItems

    private val mutableIsCompleted=MutableLiveData<Boolean>()
    val isCompleted:LiveData<Boolean> get() = mutableIsCompleted

    val liveItemsFromBooks:LiveData<List<CopyMoveItem>> = bookRepo.getAllLiveCopyMoveItemsFromBooks()

    val liveItemsFromCheckNotes:LiveData<List<CopyMoveItem>> = noteRepo.getLiveCopyMoveItemsFromCheckListNotes()


    fun setCopyMoveItems(items:List<CopyMoveItem>){
        mutableItems.value=items
    }

    fun insertBook(text:String){
        externalScope.launch {
            bookRepo.insertBook(Book(text))
        }
    }
    fun updateBook(isMove:Boolean,selectedItem: CopyMoveItem,noteIds:List<Long>){
        externalScope.launch {
            if(isMove){
                noteIds.forEach { noteId->
                    bookRepo.updateNoteBookId(noteId,selectedItem.uid)
                    noteRepo.updateAllTypeVisibleWithNoteId(noteId,!selectedItem.isLastImage)
                }
            }else{
                noteIds.forEach { noteId->
                    val newUnitedNote=noteRepo.getUnitedNote(noteId)
                    newUnitedNote.note.uid=0
                    newUnitedNote.note.bookId=selectedItem.uid
                    newUnitedNote.note.allTypeVisible = !selectedItem.isLastImage
                    newUnitedNote.note.updateDate= Utils.getFormatDate()
                    val newUid=noteRepo.insertNote(newUnitedNote.note)
                    newUnitedNote.note.uid=newUid
                    newUnitedNote.contents.map { it.noteId=newUid;it.uid=0 }
                    contentNoteRepo.insertContentNotes(newUnitedNote.contents)
                    tagRepo.getTagsForNote(noteId).forEach { tag->
                        tagRepo.addRelationTagWithNote(tag.uid,newUid)
                    }
                }
            }
            withContext(Dispatchers.Main){
                mutableIsCompleted.value=true
            }
        }
    }
    fun updateCheckNote(isMove: Boolean,selectedItem: CopyMoveItem,contentNotes:List<ContentNote>,
                        parentCheckNoteId:Long){
        externalScope.launch {
            var weight=selectedItem.count
            if(isMove){
                contentNotes.forEach { contentNote ->
                    contentNote.weight=weight++
                    if(contentNote.uid!=0L){
                        contentNote.noteId=selectedItem.uid
                        contentNoteRepo.updateContent(contentNote)
                    }else{
                        contentNote.noteId=selectedItem.uid
                        contentNoteRepo.insertContentNote(contentNote)
                    }
                }
                noteRepo.updateDateWithNoteId(parentCheckNoteId,Utils.getFormatDate())
            }else{
                contentNotes.forEach { contentNote ->
                    contentNote.weight=weight++
                    contentNote.noteId=selectedItem.uid
                    selectedItem.count
                    contentNote.uid=0L
                    contentNoteRepo.insertContentNote(contentNote)
                }
            }
            noteRepo.updateDateWithNoteId(selectedItem.uid,Utils.getFormatDate())
            withContext(Dispatchers.Main){
                mutableIsCompleted.value=true
            }
        }
    }
}