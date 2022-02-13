package com.masterplus.notex.roomdb.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "contentNotes")
data class ContentNote(
    var noteId:Long=0L,
    @ColumnInfo(name = "text")
    private var _text:String="",
    var weight:Int=0,
    var isCheck:Boolean=false,
    @PrimaryKey(autoGenerate = true)
    var uid:Long=0
):Serializable{
    @Ignore
    var textSize:Int=_text.length

    var text:String
        get() = _text
        set(value) {_text=value;textSize=value.length}
}