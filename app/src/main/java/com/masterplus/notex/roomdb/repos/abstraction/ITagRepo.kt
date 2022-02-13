package com.masterplus.notex.roomdb.repos.abstraction

import androidx.lifecycle.LiveData
import com.masterplus.notex.roomdb.entities.Tag
import com.masterplus.notex.roomdb.models.NoteWithTags
import com.masterplus.notex.roomdb.views.TagCountView

interface ITagRepo {
    suspend fun insertTag(tag:Tag)
    fun getAllLiveTags():LiveData<List<Tag>>
    fun getAllLiveTagCountViews():LiveData<List<TagCountView>>
    suspend fun deleteTag(tag: Tag)
    suspend fun updateTag(tag: Tag)
    suspend fun getCommonTags(noteIds:List<Long>,noteIdSize:Int):List<Tag>
    suspend fun getIndeterminateTags(noteIds:List<Long>,noteIdSize:Int):List<Tag>
    suspend fun addRelationTagWithNotes(tag: Tag,noteIds: List<Long>)
    suspend fun addRelationTagWithNote(tagId: Long,noteId: Long)
    suspend fun removeRelationTagWithNotes(tag: Tag,noteIds: List<Long>)
    suspend fun removeRelationWithNote(tagId: Long,noteId: Long)

    fun getLiveTagsForNote(noteId: Long):LiveData<NoteWithTags>
    suspend fun getTagsForNote(noteId:Long):List<Tag>
    suspend fun renameTag(newName: String, tagId: Long)
    suspend fun deleteTagWithId(tagId: Long)
    suspend fun deleteTagWithNoteWithTagId(tagId: Long)

}