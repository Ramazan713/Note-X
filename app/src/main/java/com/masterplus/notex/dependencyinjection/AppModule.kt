package com.masterplus.notex.dependencyinjection

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.masterplus.notex.api.services.OAuthService
import com.masterplus.notex.managers.*
import com.masterplus.notex.models.coroutinescopes.IOCoroutineScope
import com.masterplus.notex.roomdb.repos.abstraction.IBackupFileRepo
import com.masterplus.notex.roomdb.repos.abstraction.IBackupRepo
import com.masterplus.notex.utils.CustomAlerts
import com.masterplus.notex.utils.CustomGetDialogs
import com.masterplus.notex.utils.ShowMessage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import net.openid.appauth.AuthState
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideCoroutinesScope():CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Singleton
    @Provides
    fun provideIOCoroutineScope(): IOCoroutineScope = IOCoroutineScope()

    @Singleton
    @Provides
    fun injectSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(context.packageName,MODE_PRIVATE)

    @SettingPreferences
    @Singleton
    @Provides
    fun injectSettingPreferences(@ApplicationContext context: Context):SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Singleton
    @Provides
    fun injectInputMethod(@ApplicationContext context: Context):InputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    @Singleton
    @Provides
    fun injectAlarmManager(@ApplicationContext context: Context):AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Singleton
    @Provides
    fun injectShowMessage(@ApplicationContext context: Context):ShowMessage = ShowMessage(context)

    @Provides
    fun injectCustomAlerts() = CustomAlerts()

    @Provides
    fun injectCustomGetDialogs() = CustomGetDialogs()

    @Provides
    fun injectContext(@ApplicationContext context: Context) = context


    @Singleton
    @Provides
    fun injectAuthManager(context: Context,@EncryptedPreferences encryptedSharedPreferences: SharedPreferences,
                          oAuthApi: OAuthService,authState: AuthState) =
        AuthManager(context,encryptedSharedPreferences,oAuthApi,authState)

    @Singleton
    @Provides
    fun injectAuthHelper(context: Context, oAuthApi: OAuthService, authState: AuthState,
                           @EncryptedPreferences encryptedSharedPreferences: SharedPreferences) =
        AuthHelper(context,encryptedSharedPreferences,oAuthApi,authState)

    @Provides
    fun injectFileManager() = FileManager()

    @Provides
    fun injectBackupManager(app: Context, backupRepo: IBackupRepo, fileManager: FileManager,
                            backupFileRepo: IBackupFileRepo, gDriveManager: GDriveManager
    ) =
        BackupManager(app as Application,backupRepo,fileManager,backupFileRepo, gDriveManager)

}