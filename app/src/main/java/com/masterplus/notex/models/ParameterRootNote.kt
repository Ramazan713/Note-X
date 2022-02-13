package com.masterplus.notex.models

import com.masterplus.notex.designpatterns.strategy.RootNoteItemAbstract
import com.masterplus.notex.enums.RootNoteFrom
import java.io.Serializable

data class ParameterRootNote(
    val parentId:Long?=null,
    val rooNoteItem:RootNoteItemAbstract,
    val rootNoteFrom: RootNoteFrom
):Serializable