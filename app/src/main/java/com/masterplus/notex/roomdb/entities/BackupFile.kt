package com.masterplus.notex.roomdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.masterplus.notex.enums.BackupFileType

@Entity(tableName = "backupFiles")
data class BackupFile(
    var name:String,
    var fileId:String,
    var modifiedTime:String?,
    var backupFileType: BackupFileType,
    @PrimaryKey(autoGenerate = true)
    var uid:Long=0,
)
