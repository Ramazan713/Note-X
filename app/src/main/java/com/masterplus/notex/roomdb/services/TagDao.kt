package com.masterplus.notex.roomdb.services

import androidx.lifecycle.LiveData
import androidx.room.*
import com.masterplus.notex.roomdb.entities.Tag
import com.masterplus.notex.roomdb.entities.TagNoteCrossRef
import com.masterplus.notex.roomdb.models.NoteWithTags
import com.masterplus.notex.roomdb.views.TagCountView

@Dao
interface TagDao {
    @Insert
    suspend fun insertTag(tag:Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Update
    suspend fun updateTag(tag: Tag)

    @Query("select * from tags order by tagId desc")
    fun getAllLiveTags():LiveData<List<Tag>>

    @Transaction
    @Query(""" with Nwt as (select tagId from noteWithTags where noteId in (:noteIds) group by tagId having count(tagId)=:noteIdSize)
            select t.name,t.tagId from tags t inner join Nwt on Nwt.tagId=t.tagId order by t.tagId desc""")
    suspend fun getCommonTags(noteIds:List<Long>,noteIdSize:Int):List<Tag>

    @Transaction
    @Query("""with Nwt as (select tagId from noteWithTags where noteId in (:noteIds) group by tagId having count(tagId)<:noteIdSize)
            select t.name,t.tagId from tags t inner join Nwt on Nwt.tagId=t.tagId order by t.tagId desc""")
    suspend fun getIndeterminateTags(noteIds:List<Long>,noteIdSize:Int):List<Tag>

    @Delete
    suspend fun deleteTagWithNote(tagNoteCrossRef: TagNoteCrossRef)

    @Insert
    suspend fun insertTagWithNote(tagNoteCrossRef: TagNoteCrossRef)

    @Query("select * from notes where noteId=:noteId")
    suspend fun getAllTagsWithNote(noteId:Long):NoteWithTags

    @Query("select * from notes where noteId=:noteId")
    fun getAllLiveTagsWithNote(noteId:Long):LiveData<NoteWithTags>

    @Query("select * from tagCounts order by tagId desc")
    fun getAllLiveTagCountViews():LiveData<List<TagCountView>>

    @Query("update tags set name=:newName where tagId=:tagId")
    suspend fun renameTag(newName: String, tagId: Long)

    @Query("delete from tags where tagId=:tagId")
    suspend fun deleteTagWithId(tagId: Long)

    @Query("delete from noteWithTags where tagId=:tagId")
    suspend fun deleteTagWithNoteWithTagId(tagId: Long)

    @Query("delete from noteWithTags where noteId=:noteId")
    suspend fun deleteTagWithNoteWithNoteId(noteId: Long)

}