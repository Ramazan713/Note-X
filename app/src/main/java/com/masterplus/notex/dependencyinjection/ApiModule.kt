package com.masterplus.notex.dependencyinjection

import android.content.SharedPreferences
import com.masterplus.notex.api.services.GDriveService
import com.masterplus.notex.api.services.OAuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthState
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Singleton
    @Provides
    fun injectAuthState(@EncryptedPreferences sharedPreferences: SharedPreferences):AuthState =
        sharedPreferences.getString("authStateJson",null).let { if(it!=null)AuthState.jsonDeserialize(it) else AuthState() }

    @Provides
    fun getOAuthService():OAuthService = Retrofit.Builder()
        .baseUrl("https://oauth2.googleapis.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OAuthService::class.java)

    @Provides
    fun getGDriveService():GDriveService = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GDriveService::class.java)

}