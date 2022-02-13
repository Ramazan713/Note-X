package com.masterplus.notex.viewmodels

import androidx.lifecycle.LiveData
import com.masterplus.notex.roomdb.entities.Tag
import com.masterplus.notex.roomdb.repos.concrete.TagRepo
import com.masterplus.notex.roomdb.views.TagCountView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor(private val tagRepo: TagRepo,
                                       private val externalScope: CoroutineScope) :BaseViewModel() {

    val liveTagCountViews:LiveData<List<TagCountView>> = tagRepo.getAllLiveTagCountViews()

    fun renameTag(newName: String, tagId: Long) {
        externalScope.launch {
            tagRepo.renameTag(newName,tagId)
        }
    }
    fun deleteTagWithId(tagId: Long) {
        externalScope.launch {
            tagRepo.deleteTagWithId(tagId)
            tagRepo.deleteTagWithNoteWithTagId(tagId)
        }
    }
    fun insertTag(name:String){
        externalScope.launch {
            tagRepo.insertTag(Tag(name))
        }
    }
}
