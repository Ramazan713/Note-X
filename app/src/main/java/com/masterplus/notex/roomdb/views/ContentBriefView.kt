package com.masterplus.notex.roomdb.views

import androidx.room.DatabaseView


@DatabaseView( """select cn.isCheck,cn.noteId,case when n.typeContent="CheckList" then substr(cn.text,0,100) else substr(cn.text,0,300) end text
    ,length(text)textSize from contentNotes cn inner join notes n on n.noteId=cn.noteId order by cn.weight""",
    viewName = "briefContents")
data class ContentBriefView(
    var text:String,
    var isCheck:Boolean,
    var noteId:Long,
    var textSize:Int
)
