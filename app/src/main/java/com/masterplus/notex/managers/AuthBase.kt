package com.masterplus.notex.managers

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.masterplus.notex.api.services.OAuthService
import com.masterplus.notex.dependencyinjection.EncryptedPreferences
import com.masterplus.notex.utils.UtilsApi
import com.google.android.gms.common.Scopes
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import net.openid.appauth.*

abstract class AuthBase(protected val context: Context,
                        @EncryptedPreferences private val  encryptedSharedPreferences: SharedPreferences,
                        protected val authRetrofitService:OAuthService,
                        protected var authState: AuthState
){

    protected val config= AuthorizationServiceConfiguration(
        Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
        Uri.parse("https://oauth2.googleapis.com/token"),
    )
    var authService: AuthorizationService = AuthorizationService(context)

    private val firebaseAuth = FirebaseAuth.getInstance()

    fun isLogin():Boolean{
        return firebaseAuth.currentUser!=null
    }

    fun signIn(credential: AuthCredential): Task<AuthResult> {
        return firebaseAuth.signInWithCredential(credential)
    }
    fun signOut(){
        firebaseAuth.signOut()
    }

    protected fun readAuthState():AuthState{
        return encryptedSharedPreferences.getString("authStateJson",null)
            .let { if(it!=null)AuthState.jsonDeserialize(it) else AuthState() }
    }

    protected fun writeAuthState(state: AuthState) {
        encryptedSharedPreferences.edit()
            .putString("authStateJson", state.jsonSerializeString())
            .apply()
    }
    protected fun clearAuthState(){
        encryptedSharedPreferences.edit()
            .putString("authStateJson", null)
            .apply()
        authState.needsTokenRefresh=true
    }

    fun authorize(launcher: ActivityResultLauncher<Intent>){
        val req: AuthorizationRequest = AuthorizationRequest.Builder(
            config,
            UtilsApi.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(UtilsApi.REDIRECT_URI))
            .setScopes(Scopes.DRIVE_APPFOLDER, Scopes.OPEN_ID, Scopes.DRIVE_FILE,Scopes.EMAIL,Scopes.PROFILE)
            .build()

        val authIntent = authService.getAuthorizationRequestIntent(req)
        launcher.launch(authIntent)
    }
}