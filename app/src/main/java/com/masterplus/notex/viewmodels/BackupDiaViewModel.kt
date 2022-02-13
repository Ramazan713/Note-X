package com.masterplus.notex.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterplus.notex.R
import com.masterplus.notex.managers.BackupManager
import com.masterplus.notex.models.coroutinescopes.IOCoroutineScope
import com.masterplus.notex.models.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.Observer
import javax.inject.Inject

@HiltViewModel
class BackupDiaViewModel @Inject constructor(app:Application,
                                             private val iOScope: IOCoroutineScope,
                                             private val backupManager: BackupManager
)
    : AndroidViewModelBase(app) {

    init {
        iOScope.addObserver(Observer { observable, any ->
            viewModelScope.launch {
                mutableIsMessage.emit(getApplication<Application>().getString(R.string.unexpected_error_text))
            }
            setIsLoading(false)
            setIsBackupDownloading(false)
        })
    }


    private val mutableIsLoading= MutableLiveData<Boolean>(false)
    val isLoading:LiveData<Boolean> get() = mutableIsLoading

    private val mutableIsBackupDownloading= MutableLiveData<Boolean>()
    val isBackupDownloading:LiveData<Boolean> get() = mutableIsBackupDownloading

    private val mutableIsNavigateToActivity= MutableSharedFlow<Boolean>()
    val isNavigateToActivity:SharedFlow<Boolean> get() = mutableIsNavigateToActivity

    private val mutableIsMessage= MutableSharedFlow<String?>()
    val isMessage:SharedFlow<String?> get() = mutableIsMessage


    private fun setIsLoading(isLoading:Boolean){
        viewModelScope.launch {
            mutableIsLoading.value=isLoading
        }
    }
    private fun setIsBackupDownloading(isLoading: Boolean){
        viewModelScope.launch {
            mutableIsBackupDownloading.value=isLoading
        }
    }

    fun downloadCloudBackup(fileId:String){
        iOScope.launch {
            setIsBackupDownloading(true)
            setIsLoading(true)
            backupManager.loadBackupFromCloud(fileId).let { result ->
                if(result.isSuccess){
                    mutableIsNavigateToActivity.emit(true)
                }else
                    mutableIsMessage.emit(result.error)
            }
            setIsLoading(false)
            setIsBackupDownloading(false)
        }
    }
    fun uploadCloudBackup(){
        iOScope.launch {
            setIsLoading(true)
            showMessageResult(backupManager.formCloudBackup(true))
            setIsLoading(false)
        }
    }

    fun loadLocalBackup(uri: Uri){
        iOScope.launch {
            setIsBackupDownloading(true)
            setIsLoading(true)
            backupManager.loadLocalBackup(uri).let { result ->
                if(result.isSuccess)
                    mutableIsNavigateToActivity.emit(true)
                else
                    mutableIsMessage.emit(result.error)
            }
            setIsLoading(false)
            setIsBackupDownloading(false)
        }
    }

    fun formLocalBackup(){
        iOScope.launch {
            backupManager.formLocalBackup().let { result ->
                if(result.isSuccess)
                    mutableIsMessage.emit(getApplication<Application>().getString(R.string.successfully_formed_text))
            }
        }
    }
    private suspend fun showMessageResult(result:Result){
        mutableIsMessage.emit(if(result.isSuccess)getApplication<Application>().getString(R.string.success_text) else
            result.error)
    }
}