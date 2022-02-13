package com.masterplus.notex.viewmodels

import com.masterplus.notex.enums.NoteType
import com.masterplus.notex.enums.PopUpAddNoteTypes
import com.masterplus.notex.roomdb.entities.ContentNote
import com.masterplus.notex.roomdb.entities.Note
import com.masterplus.notex.roomdb.repos.abstraction.IContentNoteRepo
import com.masterplus.notex.roomdb.repos.abstraction.INoteRepo
import com.masterplus.notex.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopUpAddNoteViewModel @Inject constructor(
    private val noteRepo: INoteRepo,
    private val contentNoteRepo: IContentNoteRepo,
    private val externalScope:CoroutineScope
) : BaseViewModel() {
    private val _isExecuted = MutableSharedFlow<Boolean>()
    val isExecuted:SharedFlow<Boolean> get() = _isExecuted

    fun saveNote(contentNote: ContentNote,title:String,type:PopUpAddNoteTypes){
        externalScope.launch {
            val newTitle=if(title.trim()=="")Utils.getOnlyDateFormat() else title
            if(type==PopUpAddNoteTypes.NEW_TEXT_NOTE){
                val note= Note(newTitle,NoteType.Text,updateDate = Utils.getFormatDate())
                val uid=noteRepo.insertNote(note)
                contentNote.noteId=uid
                contentNoteRepo.insertContentNote(contentNote)
                _isExecuted.emit(true)
            }
            if(type==PopUpAddNoteTypes.NEW_CHECK_NOTE){
                val note= Note(newTitle,NoteType.CheckList,updateDate = Utils.getFormatDate())
                val uid=noteRepo.insertNote(note)
                contentNote.noteId=uid
                contentNoteRepo.insertContentNote(contentNote)
                _isExecuted.emit(true)
            }
        }

    }

}