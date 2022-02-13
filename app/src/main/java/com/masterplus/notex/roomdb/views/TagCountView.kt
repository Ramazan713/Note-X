package com.masterplus.notex.roomdb.views

import androidx.room.DatabaseView

@DatabaseView("""select t.*,
    (select count(nwt.tagId) from noteWithTags nwt inner join notes as n on n.noteId=nwt.noteId 
    where nwt.tagId=t.tagId and n.kindNote not in("TRASH_KIND"))size 
    from tags as t """,viewName = "tagCounts")
data class TagCountView(
    var name:String,
    var tagId:Long,
    var size:Int
)