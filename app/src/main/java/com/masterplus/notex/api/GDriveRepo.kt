package com.masterplus.notex.api

import com.masterplus.notex.api.services.GDriveService
import com.masterplus.notex.enums.BackupFileType
import com.masterplus.notex.managers.AuthManager
import com.masterplus.notex.models.api.DriveBody
import com.masterplus.notex.models.api.DriveFileResponse
import com.masterplus.notex.models.api.DriveSearchResponse
import com.masterplus.notex.roomdb.models.backups.UnitedBackup
import com.masterplus.notex.utils.Resource
import com.masterplus.notex.utils.Utils
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class GDriveRepo @Inject constructor(private val authManager: AuthManager,
                                     private val gDriveService: GDriveService
){

    private val gson=Gson()
    private val fieldsPath="files(id,name,modifiedTime,parents,appProperties)"

    private suspend fun getHeaderMap():Map<String,String>{
        authManager.checkExpirationAndRefresh()
        val headerMap=HashMap<String,String>()
        headerMap["Accept"]="application/json"
        headerMap["Authorization"]="Bearer ${authManager.getAccessToken()}"
        return headerMap
    }

    suspend fun createBackupsFolder():Resource<DriveFileResponse>{

        val appProperties= mapOf("fileType" to BackupFileType.BACKUP_FOLDER.name)
        val driveBody= DriveBody("application/vnd.google-apps.folder","BackupsX",arrayListOf("appDataFolder"),appProperties
            ,Utils.getRFC3339FormatDate())

        gDriveService.createFolder(getHeaderMap(),driveBody).let { response->
            return transformResponseToResource(response)
        }
    }
    suspend fun searchFilesInFolder(folderId:String):Resource<DriveSearchResponse>{
        val q="'${folderId}' in parents"

        gDriveService.searchFilesInFolder(getHeaderMap(),q,fieldsPath).let { response ->
            return transformResponseToResource(response)
        }
    }

    suspend fun getAppDataFiles():Resource<DriveSearchResponse>{
        val q=""
        gDriveService.getAppDataFolders(getHeaderMap(),q,fieldsPath).let { response ->
            return transformResponseToResource(response)
        }
    }
    suspend fun getAppDataFolders():Resource<DriveSearchResponse>{
        val q="mimeType = 'application/vnd.google-apps.folder'"
        gDriveService.getAppDataFolders(getHeaderMap(),q,fieldsPath).let { response ->
            return transformResponseToResource(response)
        }
    }
    suspend fun deleteFile(fileId:String):Resource<ResponseBody> {
        gDriveService.deleteFile(getHeaderMap(),fileId).let { response ->
            return transformResponseToResource(response)
        }
    }
    suspend fun getFileWithContent(fileId: String):Resource<UnitedBackup>{

        gDriveService.getFileWithContent(getHeaderMap(), fileId).let { response ->
            return transformResponseToResource(response)
        }
    }

    suspend fun updateFileWithMultiPart(fileId: String,newData:Any,fileName: String,
                                        modifiedTime:String):Resource<DriveFileResponse>{

        val boundary="foo_bar_baz"
        val driveBody=DriveBody("application/json",fileName,arrayListOf(), mapOf(),modifiedTime)
        val headerMap=getHeaderMap().toMutableMap()
        headerMap["Content-Type"]="multipart/related; boundary=${boundary}"

        val requestBody= RequestBody.create(MediaType.parse("application/json"),gson.toJson(driveBody))
        val body= MultipartBody.Builder(boundary)
            .addPart(requestBody)
            .addPart(RequestBody.create(MediaType.parse("application/json"),gson.toJson(newData)))
            .build()

        gDriveService.updateFileWithMultiPart(headerMap,fileId,body).let { response ->
            return transformResponseToResource(response)
        }
    }

    suspend fun createFileWithMultiPart(fileName:String,data:Any,parents:ArrayList<String> = arrayListOf(),modifiedTime:String
                                        ,backupFileType: BackupFileType )
    :Resource<DriveFileResponse>{
        val boundary="foo_bar_baz"
        val appProperties= mapOf("fileType" to backupFileType.name)

        val driveBody=DriveBody("application/json",fileName,parents,appProperties,modifiedTime)

        val headerMap=getHeaderMap().toMutableMap()
        headerMap["Content-Type"]="multipart/related; boundary=${boundary}"

        val requestBody= RequestBody.create(MediaType.parse("application/json"),gson.toJson(driveBody))
        val body= MultipartBody.Builder(boundary)
            .addPart(requestBody)
            .addPart(RequestBody.create(MediaType.parse("application/json"),gson.toJson(data)))
            .build()

        gDriveService.uploadMultiPartFile(headerMap,body).let { response ->
            return transformResponseToResource(response)
        }
    }

    private fun <T> transformResponseToResource(response: Response<T>):Resource<T>{
        return try {
            if(response.isSuccessful)
                response.body()?.let {
                    Resource.Success(it,response.code())
                }?:Resource.Error(response.errorBody()?.string()?:"Error",response.code())
            else
                Resource.Error(response.errorBody()?.string()?:"Error",response.code())
        }catch (ex:Exception){
            Resource.Error(ex.localizedMessage?:"Error",response.code())
        }
    }


}