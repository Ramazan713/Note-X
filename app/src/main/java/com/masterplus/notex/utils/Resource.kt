package com.masterplus.notex.utils

sealed class Resource<out T>{
    data class Success<T>(val data:T,val status:Int):Resource<T>()
    data class Error(val error:String,val status:Int):Resource<Nothing>()
    object Loading:Resource<Nothing>()
}
