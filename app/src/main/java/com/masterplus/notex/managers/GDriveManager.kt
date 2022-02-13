package com.masterplus.notex.managers

import android.content.Context
import android.content.SharedPreferences
import com.masterplus.notex.R
import com.masterplus.notex.api.GDriveRepo
import com.masterplus.notex.enums.BackupFileType
import com.masterplus.notex.models.Result
import com.masterplus.notex.models.api.DriveFileResponse
import com.masterplus.notex.roomdb.entities.BackupFile
import com.masterplus.notex.roomdb.models.backups.UnitedBackup
import com.masterplus.notex.roomdb.repos.abstraction.IBackupFileRepo
import com.masterplus.notex.utils.Resource
import com.masterplus.notex.utils.Utils
import java.util.*
import javax.inject.Inject

class GDriveManager @Inject constructor(private val backupFileRepo: IBackupFileRepo,
                                        private val gDriveRepo: GDriveRepo,
                                        private val sharedPreferences: SharedPreferences,
                                        private val context: Context
) {
    fun isTimeoutForBackupFiles(context: Context):Boolean{
        val interval:Long=context.resources.getInteger(R.integer.default_backup_files_fetch_interval).toLong()
        val current=Date().time
        val prevSaved=sharedPreferences.getLong("last_backupFiles_download", current)
        return prevSaved+interval < current
    }
    fun isTimeoutForBackupFiles(context: Context,interval:Long):Boolean{
        val current=Date().time
        val prevSaved=sharedPreferences.getLong("last_backupFiles_download", current)
        return prevSaved+interval < current
    }
    fun getLastSavedBackupFilesTime():Long{
        return sharedPreferences.getLong("last_backupFiles_download",0L)
    }
    fun setBackupFilesLastDownloadTime(){
        sharedPreferences.edit().putLong("last_backupFiles_download",Date().time).apply()
    }

    suspend fun downloadBackupFilesForFirstTime(){
        gDriveRepo.getAppDataFiles().let { resource ->
            when(resource){
                is Resource.Success->{
                    resource.data.files.forEach { file->
                        file.appProperties?.let {properties->
                            try{
                                val fileType=BackupFileType.valueOf(properties["fileType"] ?:BackupFileType.BACKUP_FILE.name)
                                insertBackupFile(file,fileType)
                            }catch (ex:Exception){}
                        }
                    }
                    setBackupFilesLastDownloadTime()
                }
            }
        }
    }

    suspend fun downloadBackupFile(fileId: String): Resource<UnitedBackup> {
        return gDriveRepo.getFileWithContent(fileId)
    }

    suspend fun downloadBackupMetaFilesWithSafe():Result{
        if(!backupFileRepo.isBackupFolderExists()){
            getOrFormBackupFolder().let { result ->
                if(!result.isSuccess)return result
            }
        }
        val folderId: String = backupFileRepo.getBackupFolder()?.fileId ?: return Result(false,context.getString(R.string.error_text))
        gDriveRepo.searchFilesInFolder(folderId).let { resource ->
            return when(resource){
                is Resource.Success->{
                    backupFileRepo.deleteBackupFiles()
                    resource.data.files.forEach {data->
                        val fileType=BackupFileType.valueOf(data.appProperties?.get("fileType") ?:BackupFileType.BACKUP_FILE.name)
                        insertBackupFile(data,fileType)
                    }
                    setBackupFilesLastDownloadTime()
                    Result(true,null)
                }
                is Resource.Error->{
                    Result(false,resource.error)

                }
                Resource.Loading -> Result(false,context.getString(R.string.error_text))
            }

        }
    }



    suspend fun formBackupToCloud(parents:ArrayList<String>,data:UnitedBackup,prefixName:String,
                                  backupFileType: BackupFileType=BackupFileType.BACKUP_FILE):Result{
        val date=Date()
        val fileName="${prefixName}-${Utils.getFormatDateFile(date)}"
        gDriveRepo.createFileWithMultiPart(fileName,data,parents,Utils.getRFC3339FormatDate(date),backupFileType).let { response->
            return when(response){
                is Resource.Success->{
                    response.data.let { data->
                        insertBackupFile(data,backupFileType)
                        Result(true,null)
                    }
                }
                is Resource.Error->{
                    Result(false,response.error)
                }
                else -> {Result(false,context.getString(R.string.something_went_wrong_text))}
            }
        }
    }

    suspend fun updateBackupToCloud(backupFile: BackupFile,data:UnitedBackup,prefixName: String):Result{
        val date=Date()
        val rfcDate=Utils.getRFC3339FormatDate(date)
        val fileName="""${prefixName}-${Utils.getFormatDateFile(date)}"""

        gDriveRepo.updateFileWithMultiPart(backupFile.fileId,data,fileName,rfcDate).let { response ->
            return when(response){
                is Resource.Success->{
                    backupFile.modifiedTime=Utils.getFormatDate(date)
                    backupFile.name=response.data.name
                    backupFileRepo.updateBackupFile(backupFile)
                    Result(true,null)
                }
                is Resource.Error->{
                    Result(false,context.getString(R.string.something_went_wrong_text))
                }
                else -> {Result(false,context.getString(R.string.something_went_wrong_text))}
            }
        }
    }
    suspend fun getOrFormBackupFolder():Result{
        return gDriveRepo.getAppDataFolders().let { resource ->
            when(resource){
                is Resource.Success->{
                    var result=Result(true,null)
                    for(file in resource.data.files){
                        result = if(file.appProperties?.containsKey("fileType")!=null){
                            try{
                                val fileType=BackupFileType.valueOf(file.appProperties["fileType"]!!)
                                if(fileType==BackupFileType.BACKUP_FOLDER){
                                    backupFileRepo.deleteBackupFolder()
                                    insertBackupFile(file,BackupFileType.BACKUP_FOLDER)
                                    break
                                }else{
                                    formCloudBackupFolder()
                                }
                            }catch (ex:Exception){
                                Result(false,context.getString(R.string.error_text))
                            }
                        }else{
                            Result(false,context.getString(R.string.error_text))
                        }
                    }
                    if(resource.data.files.isEmpty()){
                        result=formCloudBackupFolder()
                    }
                    result
                }
                is Resource.Error->{
                    Result(false,resource.error)
                }
                else -> { Result(false,context.getString(R.string.error_text)) }
            }
        }
    }

    private suspend fun formCloudBackupFolder():Result{
        gDriveRepo.createBackupsFolder().let { resource ->
            return when(resource){
                is Resource.Success->{
                    resource.data.let {data->
                        backupFileRepo.deleteBackupFolder()
                        insertBackupFile(data,BackupFileType.BACKUP_FOLDER)
                        Result(true,null)
                    }
                }
                is Resource.Error->{
                    Result(false,resource.error)
                }
                else -> { Result(false,context.getString(R.string.error_text)) }
            }
        }
    }




    suspend fun deleteBackupFile(fileId:String){
        gDriveRepo.deleteFile(fileId)
        backupFileRepo.deleteBackupFileWithFileId(fileId)
    }
    private suspend fun insertBackupFile(fileResponse: DriveFileResponse,backupFileType: BackupFileType){
        val date=if(fileResponse.modifiedTime!=null) Utils.getFormatDate(fileResponse.modifiedTime) else ""
        val backupFile= BackupFile(fileResponse.name,fileResponse.id,date, backupFileType)
        backupFileRepo.insertBackupFile(backupFile)
    }
}