package com.masterplus.notex.roomdb.models

import androidx.room.Embedded
import androidx.room.Relation
import com.masterplus.notex.roomdb.views.CompletedNoteView
import com.masterplus.notex.roomdb.views.ContentBriefView

data class UltimateNote(
    @Embedded
    val noteView:CompletedNoteView,
    @Relation(parentColumn = "noteId",entityColumn = "noteId")
    val contentBriefs:List<ContentBriefView>
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UltimateNote

        if (noteView.noteId != other.noteView.noteId) return false

        return true
    }

    override fun hashCode(): Int {
        return noteView.noteId.hashCode()
    }
}