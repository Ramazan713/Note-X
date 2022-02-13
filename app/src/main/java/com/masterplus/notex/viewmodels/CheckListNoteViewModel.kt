package com.masterplus.notex.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.masterplus.notex.models.NullToNoteObject
import com.masterplus.notex.roomdb.entities.ContentNote
import com.masterplus.notex.roomdb.models.UnitedNote
import com.masterplus.notex.roomdb.repos.abstraction.*
import com.masterplus.notex.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckListNoteViewModel @Inject constructor(private val noteRepo: INoteRepo,
                                                 application: Application,
                                                 private val externalScope: CoroutineScope,
                                                 private val trashRepo: ITrashRepo, tagRepo: ITagRepo,
                                                 private val contentNoteRepo: IContentNoteRepo,
                                                 private val reminderRepo: IReminderRepo, bookRepo: IBookRepo
):NoteBaseViewModel(application, externalScope, trashRepo,noteRepo, tagRepo, reminderRepo, bookRepo) {


    var stateSelectedContentNotes=MutableLiveData<MutableList<ContentNote>?>()


    fun checkAndSetDefaultTitleToNote(note: UnitedNote):String{
        if(note.note.title==""&&note.contents.isNotEmpty())
            note.note.title=Utils.getOnlyDateFormat()
        return note.note.title
    }

    override fun saveNote(note: UnitedNote, lastSavedNote: UnitedNote) {
        externalScope.launch {
            val isNotEmptyNote=note.note.title.trim()!="" || note.contents.isNotEmpty()
            if(note.note.uid!=0L){
                if(isNotEmptyNote){
                    checkAndSetDefaultTitleToNote(note)
                    var isChangeExists:Boolean = note.note.title!=lastSavedNote.note.title
                    if(note.contents!=lastSavedNote.contents){
                        isChangeExists=true
                        note.contents.forEachIndexed { index, contentNote ->
                            contentNote.weight=index
                            if(contentNote.uid==0L){
                                val newUid=contentNoteRepo.insertContentNote(contentNote)
                                contentNote.uid=newUid
                            }else{
                                contentNoteRepo.updateContent(contentNote)
                            }
                        }
                    }
                    if(isChangeExists){
                        note.note.updateDate = Utils.getFormatDate()
                        noteRepo.updateNote(note.note)
                        mutableIsNoteSaved.emit(true)
                    }
                }
            }else{
                if(!isNullToNoteHappened){
                    if(isNotEmptyNote){
                        isNullToNoteHappened=true
                        checkAndSetDefaultTitleToNote(note)
                        note.note.updateDate= Utils.getFormatDate()
                        val noteId=noteRepo.insertNote(note.note)
                        if(note.contents.isNotEmpty())
                            contentNoteRepo.insertContentNotes(note.contents.map { it.noteId=noteId;it }).forEachIndexed { index, l ->
                                note.contents[index].uid=l
                            }
                        loadCurrentNote(noteId)
                        mutableIsNoteSaved.emit(true)
                    }
                }

            }
        }
    }
    fun removeContentItem(item: ContentNote){
        externalScope.launch {
            contentNoteRepo.deleteContentNotes(listOf(item))
        }
    }
    fun removeContentItems(items:List<ContentNote>){
        externalScope.launch {
            contentNoteRepo.deleteContentNotes(items)
        }
    }
    override fun lastCheckNote(note: UnitedNote) {
        externalScope.launch {
            if(note.note.uid!=0L&&note.note.title.trim()==""&&note.contents.isEmpty()){
                reminderRepo.deleteReminderWithNoteId(note.note.uid)
                trashRepo.removeNoteForEver(note.note.uid)
            }
        }
    }


}