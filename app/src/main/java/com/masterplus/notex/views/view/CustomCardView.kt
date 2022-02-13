package com.masterplus.notex.views.view

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.masterplus.notex.R


class CustomCardView :CardView {
    constructor(context: Context):super(context)
    constructor(context: Context, attributeSet: AttributeSet?):super(context,attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle:Int):super(context,attributeSet,defStyle)

    private val STATE_COLOR_RESET = intArrayOf(R.attr.state_color_reset)
    private var isColorReset:Boolean=false

    fun setStateColorReset(isColorResetState:Boolean){
        this.isColorReset=isColorResetState
        refreshDrawableState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState=super.onCreateDrawableState(extraSpace+1)
        if(isColorReset)
            return mergeDrawableStates(drawableState,STATE_COLOR_RESET)

        return drawableState
    }

}