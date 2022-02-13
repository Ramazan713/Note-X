package com.masterplus.notex.roomdb.views

import androidx.room.DatabaseView

//
@DatabaseView("""select nwt.noteId,(select group_concat(case when length(t.name)>7 then substr(t.name,0,7)||"..." else t.name end)) tagText 
    from noteWithTags nwt inner join tags t on t.tagId=nwt.tagId group by nwt.noteId"""
    ,viewName = "tagsText")
data class TagsTextView(
    var tagText:String,
    var noteId:Long
)
