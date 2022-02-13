package com.masterplus.notex.models.copymove

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.masterplus.notex.R
import com.masterplus.notex.viewmodels.CopyMoveViewModel
import java.io.Serializable

abstract class CopyMoveObject(val isMove: Boolean,val isAddItemsAllowed:Boolean,val context: Context):Serializable {

    abstract fun getTitle():String
    abstract fun getEmptyListMessage():String
    abstract fun getFirstImageId():Int

    abstract fun loadItems(viewModel:CopyMoveViewModel,viewLifecycleOwner:LifecycleOwner)

    abstract fun insertItem(viewModel: CopyMoveViewModel,text:String)

    abstract fun update(viewModel: CopyMoveViewModel,selectedItem: CopyMoveItem)

    abstract fun getLastImageId():Int?

    open fun getAddTextTitle():String=context.getString(R.string.enter_item_name_text)

}