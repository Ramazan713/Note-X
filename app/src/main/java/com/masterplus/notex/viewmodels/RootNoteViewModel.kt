package com.masterplus.notex.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.enums.OrderNote
import com.masterplus.notex.models.ParameterRootNote
import com.masterplus.notex.roomdb.models.UltimateNote
import com.masterplus.notex.roomdb.repos.abstraction.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootNoteViewModel @Inject constructor(application: Application,
                                            private val noteRepo:INoteRepo,
                                            private val externalScope:CoroutineScope,
                                            private val ultiNoteRepo:IUltimateNoteRepo,
                                            private val trashRepo: ITrashRepo
):AndroidViewModelBase(application) {
    private val mutableLiveSortingChange= MutableLiveData<Pair<OrderNote,Boolean>>()
    private val mutableLiveSearchText = MutableLiveData<String>()

    var stateSelectedNotes=MutableLiveData<MutableList<UltimateNote>?>()

    fun changeNoteColors(color:String,noteIds:List<Long>){
        externalScope.launch {
            noteIds.forEach { noteId->
                noteRepo.changeColor(color, noteId)
            }
        }
    }

    public fun setSortingChange(orderNote: OrderNote,isDescending:Boolean){
        mutableLiveSortingChange.value=Pair(orderNote,isDescending)
    }
    public fun setSearchText(searchText:String){
        mutableLiveSearchText.value=searchText
    }

    private fun getPagingConfig() = PagingConfig(pageSize = 10,enablePlaceholders = false)

    fun getSearchedUltimateNotes():LiveData<PagingData<UltimateNote>>?{
        return Transformations.switchMap(mutableLiveSearchText){searchText->
            if(searchText.trim()!="")Pager(getPagingConfig(),null)
            {ultiNoteRepo.getUltimatePageNotesWithSearch(searchText)}.liveData
            else null
        }
    }

    public fun getUltimateNotes(parameterRootNote: ParameterRootNote):LiveData<PagingData<UltimateNote>>{
        return Transformations.switchMap(mutableLiveSortingChange) { pair ->
            Pager(getPagingConfig(),null){getPagingSourceNoteData(pair, parameterRootNote)}
                .liveData
        }
    }

    private fun getPagingSourceNoteData(pair: Pair<OrderNote,Boolean>,parameterRootNote: ParameterRootNote):
            PagingSource<Int,UltimateNote>{
        return when(pair.second){
            true->{// descending
                ultiNoteRepo.getUltimateNoteDESCWithParameter(pair.first,parameterRootNote)
            }
            false->{// ascending
                ultiNoteRepo.getUltimateNoteASCWithParameter(pair.first,parameterRootNote)
            }
        }
    }
     fun sendNotesToTrashWithId(noteIds: List<Long>){
        externalScope.launch {
            noteIds.forEach { noteId->
                trashRepo.sendNoteToTrashWithNoteId(noteId)
            }
        }
    }
    fun changeNotesKindsWithNoteIds(noteIds: List<Long>,noteKinds: NoteKinds){
        externalScope.launch {
            noteIds.forEach { noteId->
                noteRepo.changeNoteKinds(noteId, noteKinds)
            }
        }
    }
    fun deleteNotesForEver(noteIds: List<Long>){
        externalScope.launch {
            noteIds.forEach { noteId->
                trashRepo.removeNoteForEver(noteId)
            }
        }
    }
    fun recoverNotes(noteIds: List<Long>){
        externalScope.launch {
            noteIds.forEach { noteId->
                trashRepo.recoverNote(noteId)
            }
        }
    }
    fun setPinNotes(noteIds: List<Long>, isPin:Boolean){
        externalScope.launch {
            noteIds.forEach {noteId->
                if(isPin){
                    noteRepo.setPinnedNote(noteId)
                }else{
                    noteRepo.resetPinnedNote(noteId)
                }
            }
        }
    }
}