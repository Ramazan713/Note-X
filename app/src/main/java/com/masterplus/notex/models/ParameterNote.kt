package com.masterplus.notex.models

import com.masterplus.notex.enums.NoteFlags
import com.masterplus.notex.enums.NoteKinds
import java.io.Serializable

data class ParameterNote(
    var parentId:Long?=null,
    val color:String="#FFFFFF",
    val noteFlags: NoteFlags=NoteFlags.DEFAULT_NOTE,
    var isEmptyNote:Boolean=true,
    var searchText:String?=null,
    var noteKinds: NoteKinds=NoteKinds.ALL_KIND,
    var sharedNote:SharedNote?=null
):Serializable