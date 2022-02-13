package com.masterplus.notex.viewmodels.items

import androidx.lifecycle.viewModelScope
import com.masterplus.notex.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

abstract class SetItemViewModel<T>: BaseViewModel(){

    private val mutableItem=MutableSharedFlow<T>()
    val liveItem:SharedFlow<T> get() = mutableItem

    fun setItem(item:T){
        viewModelScope.launch {
            mutableItem.emit(item)
        }
    }
}