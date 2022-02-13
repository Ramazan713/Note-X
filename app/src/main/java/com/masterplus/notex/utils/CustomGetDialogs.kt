package com.masterplus.notex.utils

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.masterplus.notex.models.ParameterAddCheckItem
import com.masterplus.notex.models.ParameterAddTextItem
import com.masterplus.notex.models.ParameterNote
import com.masterplus.notex.models.copymove.CopyMoveObject
import com.masterplus.notex.roomdb.models.UnitedNote
import com.masterplus.notex.views.bottomSheetDialogFragments.AddTextBottomSheetDiaFragment
import com.masterplus.notex.views.bottomSheetDialogFragments.CopyMoveBottomSheetDia
import com.masterplus.notex.views.dialogFragments.*

class CustomGetDialogs {

    fun getSelectColorDia(selectedColor:String?):DialogFragment{
        val selectColorDia= SelectColorDiaFragment()
        selectColorDia.arguments = Bundle().also { it.putString("selectedColor",selectedColor) }
        return selectColorDia
    }
    fun getSelectReminderDia(noteId:Long):DialogFragment{
        val reminderDiaFragment = ReminderDiaFragment()
        reminderDiaFragment.arguments = Bundle().also { it.putLong("noteId",noteId) }
        return reminderDiaFragment
    }
    fun getSelectTagsDia(noteIds:List<Long>):DialogFragment{
        val selectTagsDialogFragment = SelectTagsDialogFragment()
        selectTagsDialogFragment.arguments = Bundle().also { it.putLongArray("noteIds",noteIds.toLongArray()) }
        return selectTagsDialogFragment
    }
    fun getSelectBookDia(note:UnitedNote):DialogFragment{
        val selectBookDialogFragment = SelectBookDialogFragment()
        selectBookDialogFragment.arguments = Bundle().also { it.putSerializable("note",note) }
        return selectBookDialogFragment
    }
    fun getAddCheckNoteItemDia(item: ParameterAddCheckItem):DialogFragment{
        val addCheckNoteItemDia = DiaFragmentAddCheckNoteItem()
        addCheckNoteItemDia.arguments = Bundle().also { it.putSerializable("parameterCheckItem",item)}
        return addCheckNoteItemDia
    }

    fun getCopyMoveDia(copyMoveObject:CopyMoveObject):DialogFragment{
        val copyMoveBottomSheetDia = CopyMoveBottomSheetDia()
        copyMoveBottomSheetDia.arguments = Bundle().also{it.putSerializable("copyMoveObject",copyMoveObject)}
        return copyMoveBottomSheetDia
    }
    fun getSelectNoteTypeDia(parameterNote: ParameterNote):DialogFragment{
        val selectTypeForNoteDiaFragment= SelectTypeForNoteDiaFragment()
        selectTypeForNoteDiaFragment.arguments = Bundle().also { it.putSerializable("noteParameter",parameterNote) }
        return selectTypeForNoteDiaFragment
    }

    fun getAddTextDia(parameterAddTextItem: ParameterAddTextItem):DialogFragment{
        val addTextDialogFragment = AddTextBottomSheetDiaFragment()
        addTextDialogFragment.arguments = Bundle().also { it.putSerializable("parameterAddText",parameterAddTextItem) }
        return addTextDialogFragment
    }

    fun getSelectBackupDia():DialogFragment{
        return SelectBackupDiaFragment()
    }

    fun getProgressBarDia(title:String):DialogFragment{
        val progressBarDiaFragment = ProgressBarDiaFragment()
        progressBarDiaFragment.arguments=Bundle().also { bundle->bundle.putString("title",title) }
        return progressBarDiaFragment
    }

    fun getBackupDia():DialogFragment = BackupDiaFragment()
}