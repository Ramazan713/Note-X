package com.masterplus.notex.models.copymove

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.masterplus.notex.R
import com.masterplus.notex.viewmodels.CopyMoveViewModel

class NoteCopyMoveObject(isMove: Boolean,private val noteIds:List<Long>,context: Context)
    :CopyMoveObject(isMove,isAddItemsAllowed = true,context) {

    override fun loadItems(viewModel: CopyMoveViewModel,viewLifecycleOwner: LifecycleOwner) {
        viewModel.liveItemsFromBooks.observe(viewLifecycleOwner, Observer {
            viewModel.setCopyMoveItems(it)
        })
    }

    override fun insertItem(viewModel: CopyMoveViewModel, text:String) {
        viewModel.insertBook(text)
    }

    override fun update(viewModel: CopyMoveViewModel, selectedItem: CopyMoveItem) {
        viewModel.updateBook(isMove,selectedItem, noteIds)
    }

    override fun getLastImageId(): Int = R.drawable.ic_baseline_visibility_off_24


    override fun getTitle(): String {
        if(isMove)
            return context.getString(R.string.select_book_for_moving_text)
        return  context.getString(R.string.select_notebook_for_copying_text)
    }

    override fun getEmptyListMessage(): String = context.getString(R.string.no_book_for_your_trans_text)

    override fun getFirstImageId(): Int = R.drawable.ic_baseline_library_books_24

    override fun getAddTextTitle(): String = context.getString(R.string.enter_notebook_name_text)
}