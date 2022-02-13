package com.masterplus.notex.views.view

import android.content.DialogInterface
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment


open class CustomDialogFragment:DialogFragment() {
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}