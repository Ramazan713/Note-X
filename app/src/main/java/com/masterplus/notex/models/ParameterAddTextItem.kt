package com.masterplus.notex.models

import java.io.Serializable

data class ParameterAddTextItem(val contentText:String="",
                                val title:String="",
                                val isEdit:Boolean=false,
                                val textListForSearching:List<String> = listOf(),
                                val tag:Int=0,
                                val editedId:Long?=null
): Serializable
