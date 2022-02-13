package com.masterplus.notex.roomdb.views

import androidx.room.DatabaseView
import java.io.Serializable

@DatabaseView("""select bk.*,(select count(noteId) from notes 
    where notes.bookId=bk.bookId and notes.kindNote not in ("TRASH_KIND"))size from books as bk  """
    ,viewName = "bookCounts")
data class BookCountView(
    var name:String,
    var bookId:Long,
    var size:Int,
    var isVisibleItems:Boolean=true
):Serializable