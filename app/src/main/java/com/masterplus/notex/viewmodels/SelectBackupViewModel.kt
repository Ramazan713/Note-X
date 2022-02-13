package com.masterplus.notex.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterplus.notex.R
import com.masterplus.notex.managers.GDriveManager
import com.masterplus.notex.models.coroutinescopes.IOCoroutineScope
import com.masterplus.notex.roomdb.entities.BackupFile
import com.masterplus.notex.roomdb.repos.abstraction.IBackupFileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SelectBackupViewModel @Inject constructor(app:Application,
                                                private val iOScope:IOCoroutineScope,
                                                private val backupFileRepo: IBackupFileRepo,
                                                private val gDriveManager: GDriveManager)
    :AndroidViewModelBase(app) {

    init {
        iOScope.addObserver { observable, any ->
            viewModelScope.launch {
                _isError.emit(getApplication<Application>().getString(R.string.unexpected_error_text))
                setIsLoading(false)
            }
        }
    }

    val stateSelectedTextObject = MutableLiveData<BackupFile?>()
    private val _backupFiles=MutableLiveData<List<BackupFile>>()
    val backupFiles:LiveData<List<BackupFile>> get() = _backupFiles

    private val _isLoading= MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError= MutableSharedFlow<String?>()
    val isError: SharedFlow<String?> get() = _isError

    private val _timeOutForRefreshItems=MutableLiveData<Long>()
    val timeOutForRefreshItems:LiveData<Long> get() = _timeOutForRefreshItems

    private fun setIsLoading(isLoading:Boolean){
        viewModelScope.launch {
            _isLoading.value=isLoading
        }
    }
    fun checkTimeOutForRefresh(){
        viewModelScope.launch {
            val interval:Long = 1000*30L
            val countMilSec= kotlin.math.max(gDriveManager.getLastSavedBackupFilesTime()+interval-Date().time,-1L)
            _timeOutForRefreshItems.value=countMilSec
        }
    }

    fun loadBackupMetaFilesFromCloud(){
        iOScope.launch {
            setIsLoading(true)
            gDriveManager.downloadBackupMetaFilesWithSafe().let { result ->
                if(result.isSuccess){
                    withContext(Dispatchers.Main){
                        _backupFiles.value=backupFileRepo.getAllBackupFiles()
                    }
                    gDriveManager.setBackupFilesLastDownloadTime()
                }else{
                    _isError.emit(result.error)
                }
            }
            checkTimeOutForRefresh()
            setIsLoading(false)
        }
    }
    fun loadBackupMetaFiles(){
        viewModelScope.launch {
            if(gDriveManager.isTimeoutForBackupFiles(getApplication()))
                loadBackupMetaFilesFromCloud()
            else
                _backupFiles.value=backupFileRepo.getAllBackupFiles()
        }
    }
}