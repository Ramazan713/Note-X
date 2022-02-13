package com.masterplus.notex.models

import com.android.billingclient.api.SkuDetails

data class PremiumObject(val id:Int,val title:String,val price:String,
                         val description:String,val skuDetails: SkuDetails)
