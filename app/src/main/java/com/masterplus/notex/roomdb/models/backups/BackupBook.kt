package com.masterplus.notex.roomdb.models.backups

import java.io.Serializable

data class BackupBook(
    var name:String,
    var isVisibleItems:Boolean
):Serializable
