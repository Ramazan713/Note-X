package com.masterplus.notex.models.copymove

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.masterplus.notex.R
import com.masterplus.notex.roomdb.entities.ContentNote
import com.masterplus.notex.utils.deepCopy
import com.masterplus.notex.viewmodels.CopyMoveViewModel

class CheckItemCopyMoveObject(isMove:Boolean, private val contentNotes:List<ContentNote>,
                              private val parentCheckNoteId:Long, context: Context)
    :CopyMoveObject(isMove,isAddItemsAllowed = false,context) {
    override fun getTitle(): String {
        if(isMove)
            return context.getString(R.string.select_checklist_for_moving)
        return context.getString(R.string.select_checklist_for_copying)
    }

    override fun getEmptyListMessage(): String = context.getString(R.string.no_checklist_for_your_trans_text)

    override fun getFirstImageId(): Int = R.drawable.ic_baseline_check_24

    override fun loadItems(viewModel: CopyMoveViewModel, viewLifecycleOwner: LifecycleOwner) {
        viewModel.liveItemsFromCheckNotes.observe(viewLifecycleOwner, Observer {
            viewModel.setCopyMoveItems(it.filter { it.uid!=parentCheckNoteId })
        })
    }

    override fun insertItem(viewModel: CopyMoveViewModel, text: String) {}

    override fun update(viewModel: CopyMoveViewModel, selectedItem: CopyMoveItem) {
        viewModel.updateCheckNote(isMove,selectedItem,contentNotes.deepCopy(),parentCheckNoteId)
    }

    override fun getLastImageId(): Int? = null
}