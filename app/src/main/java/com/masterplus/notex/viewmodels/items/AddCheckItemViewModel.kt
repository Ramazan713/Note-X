package com.masterplus.notex.viewmodels.items

import androidx.lifecycle.viewModelScope
import com.masterplus.notex.models.AddedCheckNoteItem
import com.masterplus.notex.models.EditedCheckNoteItem
import com.masterplus.notex.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AddCheckItemViewModel: BaseViewModel() {
    private val mutableSentAddedCheckItem = MutableSharedFlow<AddedCheckNoteItem>()
    val sentAddedCheckItem: SharedFlow<AddedCheckNoteItem> get() = mutableSentAddedCheckItem

    private val mutableSentEditedCheckItem = MutableSharedFlow<EditedCheckNoteItem>()
    val sentEditedCheckItem: SharedFlow<EditedCheckNoteItem> get() = mutableSentEditedCheckItem

    fun setSendAddedCheckItem(addedItem: AddedCheckNoteItem){
        viewModelScope.launch {
            mutableSentAddedCheckItem.emit(addedItem)
        }
    }
    fun setSentEditedCheckItem(editedItem: EditedCheckNoteItem){
        viewModelScope.launch {
            mutableSentEditedCheckItem.emit(editedItem)
        }
    }
}