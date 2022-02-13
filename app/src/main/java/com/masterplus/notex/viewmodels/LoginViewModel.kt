package com.masterplus.notex.viewmodels

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterplus.notex.dependencyinjection.SettingPreferences
import com.masterplus.notex.enums.BackupFileType
import com.masterplus.notex.managers.AuthHelper
import com.masterplus.notex.managers.BackupManager
import com.masterplus.notex.managers.GDriveManager
import com.masterplus.notex.models.LoginObject
import com.masterplus.notex.models.User
import com.masterplus.notex.models.coroutinescopes.IOCoroutineScope
import com.masterplus.notex.roomdb.repos.abstraction.IBackupFileRepo
import com.masterplus.notex.roomdb.repos.abstraction.IBackupRepo
import com.masterplus.notex.roomdb.repos.abstraction.INoteRepo
import com.masterplus.notex.utils.Resource
import com.masterplus.notex.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.masterplus.notex.R
import com.masterplus.notex.models.LoadingTitleObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class LoginViewModel @Inject constructor(app:Application,
                                         private val iOScope: IOCoroutineScope,
                                         private val backupRepo: IBackupRepo,
                                         private val backupFileRepo: IBackupFileRepo,
                                         private val backupManager: BackupManager,
                                         private val gDriveManager: GDriveManager,
                                         private val noteRepo: INoteRepo,
                                         @SettingPreferences private val settingPreferences: SharedPreferences,
                                         private val authHelper: AuthHelper
)
    :AndroidViewModelBase(app) {

    private val firebaseAuth=FirebaseAuth.getInstance()

    init {
        iOScope.addObserver { observable, any ->
            setIsLoading(false,null)
        }
        setLiveUser()
    }

    private val mutableLoginSuccess= MutableSharedFlow<LoginObject>()
    val loginSuccess:SharedFlow<LoginObject> get() = mutableLoginSuccess

    private val mutableIsLoading= MutableLiveData<LoadingTitleObject>()
    val isLoading:LiveData<LoadingTitleObject> get() = mutableIsLoading

    private val mutableLiveUser=MutableLiveData<User?>()
    val liveUser:LiveData<User?> get() = mutableLiveUser

    private val mutableIsError= MutableSharedFlow<String?>()
    val isError:SharedFlow<String?> get() = mutableIsError

    private val mutableIsNavigateToActivity= MutableLiveData<Boolean>()
    val isNavigateToActivity:LiveData<Boolean> get() = mutableIsNavigateToActivity

    private fun setLoginSuccess(){
        iOScope.launch {
            val title=getApplication<Application>().getString(R.string.logging_in_text)+"..."
            setIsLoading(true,title)

            gDriveManager.downloadBackupFilesForFirstTime()
            val loginObject:LoginObject =LoginObject(backupFileRepo.getAllBackupFileSize(),
                noteRepo.isAnyNoteItemExists())

            mutableLoginSuccess.emit(loginObject)
            setIsLoading(false,null)
        }
    }
    private fun setIsLoading(isLoading:Boolean,title:String?){
        viewModelScope.launch {
            mutableIsLoading.value=LoadingTitleObject(isLoading,title)
        }
    }
    private fun setIsNavigateToActivity(isNav:Boolean){
        viewModelScope.launch {
            mutableIsNavigateToActivity.value=isNav
        }
    }

    fun logIn(intent: Intent){
        iOScope.launch {
            exchangeToken(intent)
        }
    }
    fun authorize(launcher: ActivityResultLauncher<Intent>){
        authHelper.authorize(launcher)
    }

    fun signOut(progressBarTitle:String){
        iOScope.launch {
            setIsLoading(true,progressBarTitle)
            doBackupForSignOut()
            authHelper.resetAuthState()
            backupFileRepo.deleteAllBackupFiles()
            backupRepo.deleteContentTables()
            authHelper.signOut()
            setIsLoading(false,null)
        }
    }

    private suspend fun doBackupForSignOut(){
        if(settingPreferences.getBoolean("logOutBackupCloudAuto",true)&&noteRepo.isAnyNoteItemExists()){
            try {
                if(Utils.isInternetConnectionAvailable(getApplication()))
                    backupManager.formCloudBackup(backupFileType = BackupFileType.AUTO_BACKUP_FILE)
            }catch (ex:Exception){}
        }
        if(settingPreferences.getBoolean("logOutBackupLocalAuto",true))
            backupManager.formLocalBackup("note-sign-out-backup-${Utils.getFormatDateFile()}.plus")
    }


    fun deleteContentTables(){
        iOScope.launch {
            backupRepo.deleteContentTables()
        }
    }

    fun loadTopOfBackupFromCloud(progressBarTitle:String){
        iOScope.launch {
            backupFileRepo.getAllBackupFiles().let {
                if (it.isNotEmpty()){
                    setIsLoading(true,progressBarTitle)
                    gDriveManager.downloadBackupFile(it[0].fileId).let { resource ->
                        when(resource){
                            is Resource.Success->{
                                backupRepo.loadUnitedBackup(resource.data)
                                setIsNavigateToActivity(true)
                            }
                        }
                    }
                    setIsLoading(false,null)
                }
            }
        }
    }

    private fun exchangeToken(intent: Intent){
        val authResponse = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        val errorMessage=getApplication<Application>().getString(R.string.something_went_wrong_text)
        if(authResponse!=null){
            authHelper.authService.performTokenRequest(authResponse.createTokenExchangeRequest()){tokenResponse, exx ->
                authHelper.updateAndSaveAuthState(tokenResponse,ex)
                if(tokenResponse!=null){
                    try {
                        val credentials = GoogleAuthProvider.getCredential(tokenResponse.idToken, null)
                        authHelper.signIn(credentials)
                        authHelper.updateAndSaveAuthState(tokenResponse,ex)
                        setLoginSuccess()
                    }catch (e: Exception){
                        sendErrorMessage(errorMessage)
                    }
                }else
                    sendErrorMessage(errorMessage)
            }
        }else
            sendErrorMessage(errorMessage)
    }
    private fun sendErrorMessage(error:String){
        viewModelScope.launch {
            mutableIsError.emit(error)
        }
    }

    private fun setLiveUser(){
        firebaseAuth.addAuthStateListener {
            it.currentUser.let { firebaseUser ->
                val user=if(firebaseUser!=null)User(firebaseUser.email,firebaseUser.displayName) else null
                mutableLiveUser.value=user
            }
        }
    }

}