package com.masterplus.notex.roomdb.models

import androidx.room.Embedded
import androidx.room.Relation
import com.masterplus.notex.roomdb.entities.ContentNote
import com.masterplus.notex.roomdb.entities.Note
import java.io.Serializable

data class UnitedNote(
    @Embedded
    val note: Note,
    @Relation(parentColumn = "noteId",entityColumn = "noteId")
    val contents:MutableList<ContentNote>
):Serializable