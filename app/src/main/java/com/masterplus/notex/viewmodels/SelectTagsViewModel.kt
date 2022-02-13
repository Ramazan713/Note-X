package com.masterplus.notex.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterplus.notex.roomdb.entities.Tag
import com.masterplus.notex.roomdb.repos.abstraction.ITagRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectTagsViewModel @Inject constructor(private val tagRepo: ITagRepo,
                                              private val externalScope:CoroutineScope):BaseViewModel() {



    val liveTags=tagRepo.getAllLiveTags()

    private val mutableCommonTags=MutableLiveData<List<Tag>>()
    val commonTags:LiveData<List<Tag>> = mutableCommonTags

    private val mutableIndeterminateTags=MutableLiveData<List<Tag>>()
    val indeterminateTags:LiveData<List<Tag>> = mutableIndeterminateTags

    public fun loadCommonAndIndeterminateTags(noteIds:List<Long>){
        viewModelScope.launch {
            val noteIdsSize=noteIds.size
            mutableCommonTags.value=tagRepo.getCommonTags(noteIds,noteIdsSize)
            mutableIndeterminateTags.value=tagRepo.getIndeterminateTags(noteIds,noteIdsSize)
        }
    }


    public fun addRelationTagWithNotes(tag: Tag, noteIds: List<Long>){
        externalScope.launch {
            tagRepo.addRelationTagWithNotes(tag, noteIds)
        }
    }

    public fun removeRelationTagWithNotes(tag: Tag, noteIds: List<Long>){
        externalScope.launch {
            tagRepo.removeRelationTagWithNotes(tag, noteIds)
        }
    }

    fun insertTag(tagName:String){
        viewModelScope.launch {
            tagRepo.insertTag(Tag(tagName))
        }
    }

}