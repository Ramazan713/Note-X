package com.masterplus.notex.roomdb.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.enums.NoteType
import java.io.Serializable

@Entity(tableName = "notes")
data class Note(
    var title:String="",
    var typeContent:NoteType,//text or checklist
    var kindNote:NoteKinds=NoteKinds.ALL_KIND,//All note, Archive, Trash
    var weight:Int=0,
    var isCheck:Boolean=false,
    var color:String="#FFFFFF",
    var updateDate:String="",
    var allTypeVisible:Boolean=true,
    var bookId:Long=0
): Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "noteId")
    var uid:Long=0
}