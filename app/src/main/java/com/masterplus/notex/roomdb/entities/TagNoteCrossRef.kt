package com.masterplus.notex.roomdb.entities

import androidx.room.Entity

@Entity(primaryKeys = ["noteId","tagId"],tableName = "noteWithTags")
data class TagNoteCrossRef(
    val noteId:Long,
    val tagId:Long
)