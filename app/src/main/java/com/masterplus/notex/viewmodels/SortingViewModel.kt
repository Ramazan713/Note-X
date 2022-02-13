package com.masterplus.notex.viewmodels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class SortingViewModel:BaseViewModel() {

    private val mutableIsSortingChange=MutableSharedFlow<Boolean>()
    val isSortingChange:SharedFlow<Boolean> get() = mutableIsSortingChange

    private val mutableIsAppearanceChange = MutableSharedFlow<Boolean>()
    val isAppearanceChange:SharedFlow<Boolean> get() = mutableIsAppearanceChange

    fun setIsSortingChange(isChange:Boolean){
        viewModelScope.launch {
            mutableIsSortingChange.emit(isChange)
        }
    }
    fun setIsAppearanceChange(isChange: Boolean){
        viewModelScope.launch {
            mutableIsAppearanceChange.emit(isChange)
        }
    }
}