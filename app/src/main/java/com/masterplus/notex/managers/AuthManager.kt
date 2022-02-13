package com.masterplus.notex.managers

import android.content.Context
import android.content.SharedPreferences
import com.masterplus.notex.api.services.OAuthService
import com.masterplus.notex.utils.UtilsApi
import net.openid.appauth.*
import javax.inject.Inject

class AuthManager @Inject constructor(context: Context,
                                      encryptedSharedPreferences: SharedPreferences,
                                      authRetrofitService: OAuthService,
                                      authState: AuthState
): AuthBase(context, encryptedSharedPreferences, authRetrofitService, authState) {

    fun getAccessToken():String? = authState.accessToken
    public suspend fun checkExpirationAndRefresh(){
        if(isLogin()&&authState.needsTokenRefresh){
            refreshTokenRetro()
        }
    }
    private suspend fun refreshTokenRetro(){
        val refreshToken=authState.refreshToken
        if(refreshToken!=null){
            authRetrofitService.refreshToken(UtilsApi.CLIENT_ID,refreshToken).let { response ->
                val responseItem=response.body()
                if(response.isSuccessful&&responseItem!=null){

                    val req= TokenRequest.Builder(config, UtilsApi.CLIENT_ID)
                        .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .build()

                    val responseToken= TokenResponse.Builder(req)
                        .setAccessToken(responseItem.access_token)
                        .setAccessTokenExpiresIn(responseItem.expires_in.toLong())
                        .setTokenType(responseItem.token_type)
                        .setScope(responseItem.scope)
                        .build()

                    authState.update(responseToken,null)
                    writeAuthState(authState)
                }
            }
        }
    }
}