package com.masterplus.notex.managers

import android.content.Context
import android.content.SharedPreferences
import com.masterplus.notex.api.services.OAuthService
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.TokenResponse
import javax.inject.Inject

class AuthHelper @Inject constructor(context: Context,
                                     encryptedSharedPreferences: SharedPreferences,
                                     authRetrofitService: OAuthService,
                                     authState: AuthState
) : AuthBase(context, encryptedSharedPreferences, authRetrofitService, authState) {

    public fun authService() = authService

    fun updateAndSaveAuthState(tokenResponse: TokenResponse?,authEx: AuthorizationException?){
        authState.update(tokenResponse,authEx)
        writeAuthState(authState)
    }

    fun resetAuthState(){
        clearAuthState()
    }

}