package com.masterplus.notex.roomdb.models.backups

import java.io.Serializable

data class BackupContentNote(
    var text:String,
    var weight:Int,
    var isCheck:Boolean
):Serializable