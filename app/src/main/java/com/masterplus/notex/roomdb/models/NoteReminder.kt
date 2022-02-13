package com.masterplus.notex.roomdb.models

import androidx.room.Embedded
import androidx.room.Relation
import com.masterplus.notex.roomdb.entities.Note
import com.masterplus.notex.roomdb.entities.Reminder

data class NoteReminder(
    @Embedded
    val reminder: Reminder,
    @Relation(parentColumn = "noteId",entityColumn = "noteId")
    val note: Note
)
