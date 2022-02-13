package com.masterplus.notex.roomdb.models.backups

import java.io.Serializable

data class UnitedBackup(
    val notes:List<BackupNote>,
    val tags:List<BackupTag>,
    val books:List<BackupBook>,
    val settings:Map<String,Any>
):Serializable
