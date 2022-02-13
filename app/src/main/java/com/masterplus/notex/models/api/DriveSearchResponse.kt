package com.masterplus.notex.models.api


data class DriveSearchResponse(val incompleteSearch:Boolean,val kind:String,val files:List<DriveFileResponse>)
