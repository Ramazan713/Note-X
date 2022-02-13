package com.masterplus.notex.roomdb.views

import androidx.room.DatabaseView
import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.enums.NoteType
import com.masterplus.notex.enums.ReminderTypes

@DatabaseView("""select n.*,tg.tagText,rm.nextDate reminderDate,rm.reminderType,rm.isCompleted reminderDone,
    (select count(cn.noteId) from contentNotes cn where cn.noteId=n.noteId) contentSize 
    from notes n 
    left join tagsText tg on tg.noteId=n.noteId 
    left join reminders rm on rm.noteId=n.noteId
        """,viewName = "noteView")
data class CompletedNoteView(
    var noteId:Long,
    var title:String,
    var typeContent:NoteType,
    var kindNote:NoteKinds,
    var weight:Int=0,
    var isCheck:Boolean=false,
    var color:String="#ffffff",
    var updateDate:String="",
    var bookId:Long,
    var tagText:String?,
    var contentSize:Int?,
    var reminderDate:String?,
    var reminderType:ReminderTypes?,
    var reminderDone:Boolean?,
    var allTypeVisible:Boolean=true
){

}
