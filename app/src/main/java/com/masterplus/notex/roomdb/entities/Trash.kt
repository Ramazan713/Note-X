package com.masterplus.notex.roomdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trashes")
data class Trash(
    @PrimaryKey(autoGenerate = false)
    var noteId:Long,
    var date:String
)