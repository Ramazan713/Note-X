package com.masterplus.notex.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.masterplus.notex.R


class CustomAlerts{

    private fun adjustButtonsAppearance(alert:AlertDialog,context: Context){
        alert.getButton(DialogInterface.BUTTON_POSITIVE).let {
            it.setTextColor(context.getColor(R.color.default_button_color))
            it.isAllCaps=false
            it.textSize=17f
        }
        alert.getButton(DialogInterface.BUTTON_NEGATIVE).let {
            it.setTextColor(context.getColor(R.color.red_button_color))
            it.isAllCaps=false
            it.textSize=17f
        }

    }

    fun showDeleteNoteForEverAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete_note_forever_text))
            .setPositiveButton(R.string.approve_text,positiveListener)
            .setNegativeButton(R.string.cancel_text){ p0, p1->}
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }
    fun showDeleteSelectedNotesForEverAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete_notes_forever_text))
            .setPositiveButton(R.string.approve_text,positiveListener)
            .setNegativeButton(R.string.cancel_text){ p0, p1->}
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }
    fun showRestoreNoteAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.restore_note_message_text))
            .setPositiveButton(R.string.approve_text,positiveListener)
            .setNegativeButton(R.string.cancel_text){ p0, p1->}
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }
    fun showRestoreSelectedNotesAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.restore_notes_message_text))
            .setPositiveButton(R.string.approve_text,positiveListener)
            .setNegativeButton(R.string.cancel_text){ p0, p1->}
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }
    fun showDeleteNoteToTrashAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete_note_title_text))
            .setMessage(R.string.delete_note_message_text)
            .setPositiveButton(R.string.approve_text,positiveListener)
            .setNegativeButton(R.string.cancel_text){ p0, p1->}
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }
    fun showDeleteSelectedNotesToTrashAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete_notes_title_text))
            .setMessage(R.string.delete_notes_message_text)
            .setPositiveButton(R.string.approve_text,positiveListener)
            .setNegativeButton(R.string.cancel_text){ p0, p1->}
            .show().let {
                adjustButtonsAppearance(it,context)
            }

    }

    fun showDeleteContentItemsAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete_checknoteitem_message_text))
            .setPositiveButton(R.string.approve_text,positiveListener)
            .setNegativeButton(R.string.cancel_text){ p0, p1->}
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }
    fun showDeleteBookWithNotesAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete_from_title_text))
            .setMessage(context.getString(R.string.book_to_trash_message_text))
            .setPositiveButton(context.getText(R.string.approve_text),positiveListener)
            .setNegativeButton(context.getText(R.string.cancel_text)){ dialog, which -> }
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }

    fun showDeleteTagWithoutNotesAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getText(R.string.delete_from_title_text))
            .setMessage(context.getText(R.string.tag_inside_notes_not_deleted_text))
            .setPositiveButton(context.getText(R.string.approve_text),positiveListener)
            .setNegativeButton(context.getText(R.string.cancel_text)){ dialog, which -> }
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }

    fun showFormNewCloudBackupAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.form_new_cloud_backup_title))
            .setMessage(R.string.some_backupfile_may_change_text)
            .setPositiveButton(context.getText(R.string.approve_text),positiveListener)
            .setNegativeButton(context.getText(R.string.cancel_text)){ dialog, which -> }
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }

    fun showSetVisibilityNotesInsideBook(context: Context,isVisible:Boolean,positiveListener:DialogInterface.OnClickListener){
        val title= context.getString(if(isVisible)R.string.want_to_continue_text else R.string.want_to_continue_text)
        val message=context.getString(if(isVisible)R.string.appear_all_note_text else R.string.not_appear_all_notes_text)
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(context.getText(R.string.approve_text),positiveListener)
            .setNegativeButton(context.getText(R.string.cancel_text)){ dialog, which -> }
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }

    fun showLoadBackupFromCloudFirstTime(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.ask_last_cloud_backup_download_text))
            .setPositiveButton(context.getText(R.string.approve_text),positiveListener)
            .setNegativeButton(context.getText(R.string.cancel_text)){ dialog, which -> }
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }

    fun showBackupOverrideExistingDataAlert(context: Context, positiveListener:DialogInterface.OnClickListener,
                                            negativeListener:DialogInterface.OnClickListener?=null){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.ask_overrite_backup_text))
            .setMessage(context.getString(R.string.info_some_data_may_loss_text))
            .setPositiveButton(context.getText(R.string.approve_text),positiveListener)
            .setNegativeButton(context.getText(R.string.cancel_text),negativeListener?:DialogInterface.OnClickListener{ p0, p1 -> })
            .setCancelable(false)
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }


    fun showSignOutAlert(context: Context,positiveListener:DialogInterface.OnClickListener){
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.ask_sign_out_text))
            .setMessage(context.getString(R.string.ask_delete_notes_after_sign_out_text))
            .setPositiveButton(context.getText(R.string.approve_text),positiveListener)
            .setNegativeButton(context.getText(R.string.cancel_text)){ dialog, which -> }
            .show().let {
                adjustButtonsAppearance(it,context)
            }
    }

    fun showSelectFontSizeAlert(context: Context,sharedPreferences: SharedPreferences,listener:((pos:Int,textSize:Int)->Unit)?=null){
        val items = context.resources.getStringArray(R.array.font_size_texts)
        val textSizeArr = context.resources.getIntArray(R.array.font_size_values)

        var checkPos=textSizeArr.indexOf(Utils.getFontSize(sharedPreferences))
        checkPos=if(checkPos==-1) (textSizeArr.size/2) else checkPos
        val textView= TextView(context).apply {
            text="Example Text"
            val params=
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(3,19,3,0)
            this.layoutParams=params
            gravity= Gravity.CENTER
            textSize=textSizeArr[checkPos].toFloat()
        }
        val alert=AlertDialog.Builder(context)
            .setSingleChoiceItems(items,checkPos) { p0, p1 ->
                val textSize=textSizeArr[p1]
                textView.textSize= textSize.toFloat()
                sharedPreferences.edit().putInt("font_size_text",textSize).apply()
                listener?.invoke(p1,textSize)
            }.setTitle(context.getString(R.string.choice_font_text))
            .setNegativeButton(context.getText(R.string.cancel_text)){x,y-> }
            .setView(textView)
        alert.show()
    }
}