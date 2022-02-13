package com.masterplus.notex.viewmodels.items

import androidx.lifecycle.viewModelScope
import com.masterplus.notex.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

open class DestroyListenerViewModel:BaseViewModel(){

    private val mutableIsClosed= MutableSharedFlow<Boolean>()
    val isClosed:SharedFlow<Boolean> get() = mutableIsClosed

    fun setIsClosed(isClosed:Boolean){
        viewModelScope.launch {
            mutableIsClosed.emit(isClosed)
        }
    }
}