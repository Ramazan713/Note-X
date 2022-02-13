package com.masterplus.notex.models

import java.io.Serializable

data class AddTextItem(val approvedText:String,val isEdit:Boolean=false,val tag:Int=0,val editedId:Long?=null):Serializable
