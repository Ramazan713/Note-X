package com.masterplus.notex.roomdb.repos.abstraction

import com.masterplus.notex.roomdb.entities.BackupFile

interface IBackupFileRepo {

    suspend fun insertBackupFile(backupFile: BackupFile):Long
    suspend fun deleteBackupFolder()
    suspend fun deleteBackupFiles()
    suspend fun getBackupFolder():BackupFile?
    suspend fun isBackupFolderExists():Boolean
    suspend fun deleteAllBackupFiles()
    suspend fun updateFileIdWithId(uid:Long,fileId:String)
    suspend fun updateBackupFile(backupFile: BackupFile)
    suspend fun deleteBackupFileWithId(uid: Long)
    suspend fun deleteBackupFile(backupFile: BackupFile)
    suspend fun deleteBackupFileWithFileId(fileId: String)
    suspend fun getClassicBackupFiles():List<BackupFile>
    suspend fun getAllBackupFiles():List<BackupFile>
    suspend fun getClassicBackupFileSize():Int
    suspend fun getAllBackupFileSize():Int
    suspend fun getAutoBackupFiles():List<BackupFile>



}