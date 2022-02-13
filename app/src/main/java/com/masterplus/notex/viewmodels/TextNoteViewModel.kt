package com.masterplus.notex.viewmodels

import android.app.Application
import android.util.Log
import com.masterplus.notex.models.NullToNoteObject
import com.masterplus.notex.roomdb.models.UnitedNote
import com.masterplus.notex.roomdb.repos.abstraction.*
import com.masterplus.notex.roomdb.repos.concrete.*
import com.masterplus.notex.utils.Utils
import com.masterplus.notex.utils.Utils.getFormatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class TextNoteViewModel @Inject constructor(
    app: Application,
    private val noteRepo: INoteRepo,
    private val contentNoteRepo: IContentNoteRepo,
    private val externalScope: CoroutineScope,
    private val tagRepo: ITagRepo,
    private val trashRepo: ITrashRepo,
    private val bookRepo: BookRepo,
    private val reminderRepo: IReminderRepo
):NoteBaseViewModel(app,externalScope,trashRepo,noteRepo,tagRepo,reminderRepo,bookRepo) {

    fun checkAndSetDefaultTitleToNote(note: UnitedNote):String{
        if(note.note.title==""&&note.contents[0].text.trim()!="")
            note.note.title=Utils.getOnlyDateFormat()
        return note.note.title
    }

    override fun saveNote(note: UnitedNote,lastSavedNote: UnitedNote) {
        externalScope.launch {
            var noteTitle=note.note.title.trim()
            val isNotEmptyNote=noteTitle!="" || note.contents[0].text.trim()!=""
            if(note.note.uid!=0L){
                if(isNotEmptyNote){
                    noteTitle=checkAndSetDefaultTitleToNote(note)
                    if(noteTitle!=lastSavedNote.note.title.trim()||note.contents!=lastSavedNote.contents){
                        note.note.updateDate = getFormatDate()
                        noteRepo.updateNote(note.note)
                        contentNoteRepo.updateContentNotes(note.contents)
                        mutableIsNoteSaved.emit(true)
                    }
                }
            }else{
                if(!isNullToNoteHappened){
                    if(isNotEmptyNote){
                        isNullToNoteHappened=true
                        note.note.updateDate=getFormatDate()
                        checkAndSetDefaultTitleToNote(note)
                        val noteId=noteRepo.insertNote(note.note)
                        contentNoteRepo.insertContentNotes(note.contents.map { it.noteId=noteId;it })
                        loadCurrentNote(noteId)

                        mutableIsNoteSaved.emit(true)
                    }
                }
            }
        }
    }

    override fun lastCheckNote(note: UnitedNote) {
        externalScope.launch {
            if(note.note.uid!=0L&&note.note.title.trim()==""&&note.contents[0].text.trim()==""){
                reminderRepo.deleteReminderWithNoteId(note.note.uid)
                trashRepo.removeNoteForEver(note.note.uid)
            }
        }
    }
}