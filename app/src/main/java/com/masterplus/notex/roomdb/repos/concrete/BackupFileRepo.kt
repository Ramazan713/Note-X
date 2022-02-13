package com.masterplus.notex.roomdb.repos.concrete

import com.masterplus.notex.roomdb.entities.BackupFile
import com.masterplus.notex.roomdb.repos.abstraction.IBackupFileRepo
import com.masterplus.notex.roomdb.services.BackupFileDao
import javax.inject.Inject

class BackupFileRepo @Inject constructor(private val backupFileDao: BackupFileDao) : IBackupFileRepo {
    override suspend fun insertBackupFile(backupFile: BackupFile): Long {
        return backupFileDao.insertBackupFile(backupFile)
    }

    override suspend fun deleteBackupFolder() {
        backupFileDao.deleteBackupFolder()
    }

    override suspend fun deleteBackupFiles() {
        backupFileDao.deleteAllBackupFiles()
    }

    override suspend fun getBackupFolder(): BackupFile? {
        return backupFileDao.getBackupFolder()
    }

    override suspend fun isBackupFolderExists(): Boolean {
        return backupFileDao.isBackupFolderExists()
    }

    override suspend fun deleteAllBackupFiles() {
        backupFileDao.deleteAllBackups()
    }

    override suspend fun updateFileIdWithId(uid: Long, fileId: String) {
        backupFileDao.updateFileIdWithId(uid, fileId)
    }

    override suspend fun updateBackupFile(backupFile: BackupFile) {
        backupFileDao.updateBackupFile(backupFile)
    }

    override suspend fun deleteBackupFileWithId(uid: Long) {
        backupFileDao.deleteBackupFileWithId(uid)
    }

    override suspend fun deleteBackupFile(backupFile: BackupFile) {
        backupFileDao.deleteBackupFile(backupFile)
    }

    override suspend fun deleteBackupFileWithFileId(fileId: String) {
        backupFileDao.deleteBackupFileWithFileId(fileId)
    }


    override suspend fun getClassicBackupFiles(): List<BackupFile> {
        return backupFileDao.getClassicBackupFiles()
    }

    override suspend fun getAllBackupFiles(): List<BackupFile> {
        return backupFileDao.getAllBackupFiles()
    }

    override suspend fun getClassicBackupFileSize(): Int {
        return backupFileDao.getClassicBackupFileSize()
    }

    override suspend fun getAllBackupFileSize(): Int {
        return backupFileDao.getAllBackupFileSize()
    }

    override suspend fun getAutoBackupFiles(): List<BackupFile> {
        return backupFileDao.getAutoBackupFiles()
    }
}