package com.masterplus.notex.api.services

import com.masterplus.notex.models.api.DriveBody
import com.masterplus.notex.models.api.DriveFileResponse
import com.masterplus.notex.models.api.DriveSearchResponse
import com.masterplus.notex.roomdb.models.backups.UnitedBackup
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GDriveService {
    @POST("upload/drive/v3/files?uploadType=multipart")
    suspend fun uploadMultiPartFile(@HeaderMap headerMap: Map<String,String>, @Body multipartBody: MultipartBody):
            Response<DriveFileResponse>

    @GET("/drive/v3/files/{fileId}?alt=media")
    suspend fun  getFileWithContent(@HeaderMap headerMap: Map<String,String>, @Path("fileId")fileId:String):
            Response<UnitedBackup>

    @PATCH("/upload/drive/v3/files/{fileId}?uploadType=multipart")
    suspend fun updateFileWithMultiPart(@HeaderMap headerMap: Map<String,String>, @Path("fileId")fileId:String,
                                        @Body multipartBody: MultipartBody): Response<DriveFileResponse>

    @POST("drive/v3/files")
    suspend fun createFolder(@HeaderMap headerMap: Map<String,String>, @Body bodyMap: DriveBody):
            Response<DriveFileResponse>

    @GET("drive/v3/files?orderBy=modifiedTime desc&spaces=appDataFolder")
    suspend fun searchFilesInFolder(@HeaderMap headerMap: Map<String,String>, @Query("q")q:String,
                                    @Query("fields")fields:String): Response<DriveSearchResponse>

    @GET("drive/v3/files?spaces=appDataFolder")
    suspend fun getAppDataFolders(@HeaderMap headerMap: Map<String,String>, @Query("q")q:String,
                                  @Query("fields")fields:String): Response<DriveSearchResponse>


    @DELETE("/drive/v3/files/{fileId}")
    suspend fun deleteFile(@HeaderMap headerMap: Map<String,String>, @Path("fileId")fileId:String): Response<ResponseBody>
}