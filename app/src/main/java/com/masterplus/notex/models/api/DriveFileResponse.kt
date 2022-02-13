package com.masterplus.notex.models.api

import java.util.*

data class DriveFileResponse(val mimeType:String?, val kind:String?, val id:String, val name:String,
                             val parents:List<String>?, val modifiedTime: Date?, val appProperties:Map<String,String>?)