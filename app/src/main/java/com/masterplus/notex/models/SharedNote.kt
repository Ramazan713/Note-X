package com.masterplus.notex.models

import com.masterplus.notex.roomdb.entities.ContentNote
import java.io.Serializable

data class SharedNote(
    val title:String?,
    val contentNotes:List<ContentNote>
):Serializable
