package com.masterplus.notex.roomdb.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.masterplus.notex.roomdb.entities.Note
import com.masterplus.notex.roomdb.entities.Tag
import com.masterplus.notex.roomdb.entities.TagNoteCrossRef

data class NoteWithTags(
    @Embedded
    val note: Note,
    @Relation(parentColumn = "noteId",entityColumn = "tagId",associateBy = Junction(TagNoteCrossRef::class))
    val tags:List<Tag>
)