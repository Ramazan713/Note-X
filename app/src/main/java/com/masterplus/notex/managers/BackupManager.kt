package com.masterplus.notex.managers

import android.app.Application
import android.net.Uri
import com.masterplus.notex.R
import com.masterplus.notex.enums.BackupFileType
import com.masterplus.notex.models.Result
import com.masterplus.notex.roomdb.entities.BackupFile
import com.masterplus.notex.roomdb.models.backups.UnitedBackup
import com.masterplus.notex.roomdb.repos.abstraction.IBackupFileRepo
import com.masterplus.notex.roomdb.repos.abstraction.IBackupRepo
import com.masterplus.notex.utils.Resource
import com.masterplus.notex.utils.Utils
import java.io.File
import javax.inject.Inject

class BackupManager  @Inject constructor(
    private val application: Application,
    private val backupRepo: IBackupRepo,
    private val fileManager: FileManager,
    private val backupFileRepo: IBackupFileRepo,
    private val gDriveManager: GDriveManager
) {
    private val firstBackupFile: File?= File(application.externalMediaDirs.firstOrNull(),"backups").apply {
        if(!exists())
            mkdirs()
    }

    suspend fun loadBackupFromCloud(fileId:String):Result{
        gDriveManager.downloadBackupFile(fileId).let { resource ->
            return when(resource){
                is Resource.Success->{
                    backupRepo.deleteContentTables()
                    backupRepo.loadUnitedBackup(resource.data)
                    Result(true,null)
                }
                is Resource.Error->{
                    Result(false,resource.error)
                }
                else -> Result(false,"")
            }
        }
    }

    suspend fun formCloudBackup(data: UnitedBackup,checkTimeOutFiles:Boolean=false,
                                backupFileType: BackupFileType = BackupFileType.BACKUP_FILE):Result{
        val backupFolder=backupFileRepo.getBackupFolder()
        if(backupFolder==null){
            gDriveManager.getOrFormBackupFolder()
            return Result(false,application.getString(R.string.something_went_wrong_text))
        }
        val backupFiles=getBackupFiles(checkTimeOutFiles,backupFileType)
        val maxBackupSize=getMaxBackupSize(backupFileType)
        val prefixName=getPrefixName(backupFileType)
        return executeFormCloudBackup(data,backupFolder.fileId,backupFileType,maxBackupSize,backupFiles, prefixName)
    }
    suspend fun formCloudBackup(checkTimeOutFiles:Boolean=false,
                                backupFileType: BackupFileType=BackupFileType.BACKUP_FILE):Result{
        return formCloudBackup(backupRepo.formBackupUnitedBackup(),checkTimeOutFiles,backupFileType)
    }

    private fun getPrefixName(backupType:BackupFileType)=if(backupType==BackupFileType.AUTO_BACKUP_FILE)"Backup-Auto" else "Backup"

    private fun getMaxBackupSize(backupType:BackupFileType)=application.resources.getInteger(
        if(backupType==BackupFileType.AUTO_BACKUP_FILE)R.integer.max_auto_backup_size else R.integer.max_backup_size )

    private suspend fun executeFormCloudBackup(data: UnitedBackup,backupFolderId:String,backupType:BackupFileType
                                               ,maxBackupSize:Int,backupFiles:List<BackupFile>,prefixName:String):Result{
        return when{
            backupFiles.size<maxBackupSize->{
                gDriveManager.formBackupToCloud(arrayListOf(backupFolderId),data, prefixName,backupType)
            }
            backupFiles.size==maxBackupSize->{
                gDriveManager.updateBackupToCloud(backupFiles[maxBackupSize-1],data, prefixName)
            }
            else->{
                val rs = gDriveManager.updateBackupToCloud(backupFiles[maxBackupSize-1],data, prefixName)
                for(i in maxBackupSize until backupFiles.size)
                    gDriveManager.deleteBackupFile(backupFiles[i].fileId)
                rs
            }
        }
    }

    private suspend fun getBackupFiles(checkTimeOutFiles:Boolean,
                                       backupFileType: BackupFileType):List<BackupFile>{
        return when(backupFileType){
            BackupFileType.AUTO_BACKUP_FILE->{
                return backupFileRepo.getAutoBackupFiles()
            }
            BackupFileType.BACKUP_FILE->{
                if(checkTimeOutFiles&&gDriveManager.isTimeoutForBackupFiles(application))
                    gDriveManager.downloadBackupMetaFilesWithSafe()
                backupFileRepo.getClassicBackupFiles()
            }
            else-> listOf<BackupFile>()
        }

    }

    suspend fun formLocalBackup(data: UnitedBackup, path: File, fileName:String){
        fileManager.writeFile(data, path, fileName)
    }

    suspend fun formLocalBackup(fileName: String="note-backup-${Utils.getFormatDateFile()}.plus"): Result {
        val data=backupRepo.formBackupUnitedBackup()
        firstBackupFile?.let {
            fileManager.writeFile(data,firstBackupFile,fileName)
            return Result(true,null)
        }
        return Result(false,application.getString(R.string.something_went_wrong_text))
    }

    suspend fun loadLocalBackup(uri: Uri): Result {
        fileManager.readFile(uri,application)?.let { data->
            backupRepo.deleteContentTables()
            backupRepo.loadUnitedBackup(data)
            return Result(true,null)
        }
        return Result(false,application.getString(R.string.something_went_wrong_text))
    }


}