package com.masterplus.notex.viewmodels.items

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masterplus.notex.viewmodels.BaseViewModel
import kotlinx.coroutines.launch

abstract class SetLiveItemViewModel<T>:BaseViewModel() {
    private val mutableItem= MutableLiveData<T>()
    val liveItem: LiveData<T> get() = mutableItem

    fun setItem(item:T){
        viewModelScope.launch {
            mutableItem.value=item
        }
    }
}