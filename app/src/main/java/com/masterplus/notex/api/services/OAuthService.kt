package com.masterplus.notex.api.services

import com.masterplus.notex.models.api.OAuthRefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface OAuthService {
    @Headers("Content-Type:application/x-www-form-urlencoded")
    @POST("/token?grant_type=refresh_token")
    suspend fun refreshToken(@Query("clientId") clientId:String, @Query("refreshToken")refreshToken:String)
            : Response<OAuthRefreshTokenResponse>
}