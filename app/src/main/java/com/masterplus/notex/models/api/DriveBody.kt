package com.masterplus.notex.models.api

data class DriveBody(val mimeType:String,val name:String,val parents:List<String>,
                     val appProperties:Map<String,String> = mapOf(),val modifiedTime:String)
