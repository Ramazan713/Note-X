package com.masterplus.notex.models.api

data class OAuthRefreshTokenResponse(val access_token:String,
                                     val expires_in:Int,
                                     val scope:String,
                                     val token_type:String)
