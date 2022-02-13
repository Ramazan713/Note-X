package com.masterplus.notex.roomdb.models.backups

import com.masterplus.notex.enums.NoteKinds
import com.masterplus.notex.enums.NoteType
import java.io.Serializable

data class BackupNote(
    val title:String,
    val typeContent: NoteType,
    val kindNote: NoteKinds,
    val weight:Int,
    val isCheck:Boolean,
    val color:String,
    val updateDate:String,
    val allTypeVisible:Boolean,
    val contentNotes:List<BackupContentNote>,
    val tags:List<String>,
    val bookName:String?,
    val reminder:BackupReminder?,
    val trash: BackupTrash?
):Serializable