package com.masterplus.notex.roomdb.repos.concrete

import androidx.lifecycle.LiveData
import com.masterplus.notex.roomdb.entities.Tag
import com.masterplus.notex.roomdb.entities.TagNoteCrossRef
import com.masterplus.notex.roomdb.models.NoteWithTags
import com.masterplus.notex.roomdb.repos.abstraction.ITagRepo
import com.masterplus.notex.roomdb.services.TagDao
import com.masterplus.notex.roomdb.views.TagCountView
import javax.inject.Inject

class TagRepo @Inject constructor(private val tagDao: TagDao):ITagRepo {
    override suspend fun insertTag(tag: Tag) {
        tagDao.insertTag(tag)
    }

    override fun getAllLiveTags(): LiveData<List<Tag>> {
        return tagDao.getAllLiveTags()
    }

    override fun getAllLiveTagCountViews(): LiveData<List<TagCountView>> {
        return tagDao.getAllLiveTagCountViews()
    }

    override suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(tag)
    }

    override suspend fun updateTag(tag: Tag) {
        tagDao.deleteTag(tag)
    }

    override suspend fun getCommonTags(noteIds:List<Long>,noteIdSize:Int): List<Tag> {
        return tagDao.getCommonTags(noteIds,noteIdSize)
    }

    override suspend fun getIndeterminateTags(noteIds:List<Long>,noteIdSize:Int): List<Tag> {
        return tagDao.getIndeterminateTags(noteIds,noteIdSize)
    }

    override suspend fun addRelationTagWithNotes(tag: Tag, noteIds: List<Long>) {
        noteIds.forEach { noteId->
            val tagWithNote=TagNoteCrossRef(noteId,tag.uid)
            tagDao.deleteTagWithNote(tagWithNote)
            tagDao.insertTagWithNote(tagWithNote)
        }
    }

    override suspend fun addRelationTagWithNote(tagId: Long, noteId: Long) {
        val tagWithNote=TagNoteCrossRef(noteId,tagId)
        tagDao.deleteTagWithNote(tagWithNote)
        tagDao.insertTagWithNote(tagWithNote)
    }

    override suspend fun removeRelationTagWithNotes(tag: Tag, noteIds: List<Long>) {
        noteIds.forEach { noteId->
            val tagWithNote=TagNoteCrossRef(noteId,tag.uid)
            tagDao.deleteTagWithNote(tagWithNote)
        }
    }

    override suspend fun removeRelationWithNote(tagId: Long, noteId: Long) {
        val tagWithNote=TagNoteCrossRef(noteId,tagId)
        tagDao.deleteTagWithNote(tagWithNote)
    }

    override fun getLiveTagsForNote(noteId: Long): LiveData<NoteWithTags> {
        return tagDao.getAllLiveTagsWithNote(noteId)
    }


    override suspend fun getTagsForNote(noteId: Long): List<Tag> {
        return tagDao.getAllTagsWithNote(noteId).tags
    }

    override suspend fun renameTag(newName: String, tagId: Long) {
        tagDao.renameTag(newName, tagId)
    }

    override suspend fun deleteTagWithId(tagId: Long) {
        tagDao.deleteTagWithId(tagId)
    }

    override suspend fun deleteTagWithNoteWithTagId(tagId: Long) {
        tagDao.deleteTagWithNoteWithTagId(tagId)
    }

}