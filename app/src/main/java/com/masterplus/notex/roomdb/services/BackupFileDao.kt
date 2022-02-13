package com.masterplus.notex.roomdb.services

import androidx.room.*
import com.masterplus.notex.roomdb.entities.BackupFile

@Dao
interface BackupFileDao {

    @Insert
    suspend fun insertBackupFile(backupFile: BackupFile):Long

    @Delete
    suspend fun deleteBackupFile(backupFile: BackupFile)

    @Update
    suspend fun updateBackupFile(backupFile: BackupFile)

    @Query("delete from backupFiles where uid=:uid")
    suspend fun deleteBackupFileWithId(uid: Long)

    @Query("delete from backupFiles where fileId=:fileId")
    suspend fun deleteBackupFileWithFileId(fileId: String)

    @Query("delete from backupFiles where backupFileType='BACKUP_FOLDER'")
    suspend fun deleteBackupFolder()

    @Query("delete from backupFiles where backupFileType in ('BACKUP_FILE','AUTO_BACKUP_FILE')")
    suspend fun deleteAllBackupFiles()

    @Query("select * from backupFiles where backupFileType in ('BACKUP_FILE','AUTO_BACKUP_FILE') order by modifiedTime desc")
    suspend fun getAllBackupFiles():List<BackupFile>

    @Query("select * from backupFiles where backupFileType = 'BACKUP_FILE' order by modifiedTime desc")
    suspend fun getClassicBackupFiles():List<BackupFile>

    @Query("select * from backupFiles where backupFileType = 'AUTO_BACKUP_FILE' order by modifiedTime desc")
    suspend fun getAutoBackupFiles():List<BackupFile>

    @Query("select count(*) from backupFiles where backupFileType='BACKUP_FILE'")
    suspend fun getClassicBackupFileSize():Int

    @Query("select count(*) from backupFiles where backupFileType in ('BACKUP_FILE','AUTO_BACKUP_FILE')")
    suspend fun getAllBackupFileSize():Int

    @Query("select * from backupFiles where backupFileType='BACKUP_FOLDER' limit 1")
    suspend fun getBackupFolder():BackupFile?

    @Query("select exists(select 1 from backupFiles where backupFileType='BACKUP_FOLDER')")
    suspend fun isBackupFolderExists():Boolean

    @Query("delete from backupFiles")
    suspend fun deleteAllBackups()

    @Query("update backupFiles set fileId=:fileId where uid=:uid")
    suspend fun updateFileIdWithId(uid:Long,fileId:String)



}