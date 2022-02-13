package com.masterplus.notex.views.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText

class CustomEditText: TextInputEditText {
    constructor(context: Context):super(context)
    constructor(context: Context,attributeSet: AttributeSet?):super(context,attributeSet)
    constructor(context: Context,attributeSet: AttributeSet?,defStyle:Int):super(context,attributeSet,defStyle)

    private var listener:((isActive:Boolean)->Unit)?=null

    fun setKeyBackListener(listener:((isActive:Boolean)->Unit)){
        this.listener=listener
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action==MotionEvent.ACTION_UP){
            listener?.invoke(true)
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK&&event?.action == KeyEvent.ACTION_UP) {
            clearFocus()
            listener?.invoke(false)
        }
        return super.onKeyPreIme(keyCode, event)

    }

}