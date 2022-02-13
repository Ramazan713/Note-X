package com.masterplus.notex.dependencyinjection

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @MasterKey
    @Provides
    fun injectMasterKey():String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    @EncryptedPreferences
    @Singleton
    @Provides
    fun injectEncryptedSharedPreferences(@MasterKey masterKey:String,context:Context):SharedPreferences =
        EncryptedSharedPreferences.create("sharedPreferences",masterKey,context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

}

