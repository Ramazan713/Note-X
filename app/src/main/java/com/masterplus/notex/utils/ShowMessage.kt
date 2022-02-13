package com.masterplus.notex.utils

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ShowMessage @Inject constructor(@ApplicationContext  val context: Context) {

    private var toast: Toast? = null
    fun showLong(text:String){
        toast?.cancel()
        toast = Toast.makeText(context, text, Toast.LENGTH_LONG)
        toast?.show()
    }
    fun showShort(text: String){
        toast?.cancel()
        toast = Toast.makeText(context, text, Toast.LENGTH_LONG)
        toast?.show()
    }
}