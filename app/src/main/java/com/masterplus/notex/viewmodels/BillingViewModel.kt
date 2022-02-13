package com.masterplus.notex.viewmodels

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.masterplus.notex.models.coroutinescopes.IOCoroutineScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(app:Application,
                                           private val ioCoroutineScope: IOCoroutineScope,
                                           private val externalScope:CoroutineScope

) : AndroidViewModelBase(app){

    private val mutableIsPremiumLive=MutableLiveData<Boolean>()
    val isPremiumLive:LiveData<Boolean> get() = mutableIsPremiumLive

    private var isBillingClientConnected:Boolean=false

    private val mutableIsPurchase=MutableSharedFlow<Boolean>()
    val isPurchaseFlow:SharedFlow<Boolean> get() = mutableIsPurchase

    private val mutableIsLoading=MutableLiveData<Boolean>()
    val isLoading:LiveData<Boolean> get() = mutableIsLoading

    private val mutableSkuDetails=MutableLiveData<List<SkuDetails>?>(null)
    val skuDetailsLive:LiveData<List<SkuDetails>?> get() = mutableSkuDetails

    companion object{
        var mutableIsPremium:Boolean=false
    }

    private fun setIsPremium(isPremium:Boolean){
        viewModelScope.launch {
            mutableIsPremium=isPremium
            mutableIsPremiumLive.value = isPremium
        }
    }
    private fun setIsLoading(isLoading:Boolean){
        viewModelScope.launch {
            mutableIsLoading.value=isLoading
        }
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private var billingClient:BillingClient = BillingClient.newBuilder(app)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails()
                    isBillingClientConnected=true
                    checkSubs()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun querySkuDetails() {
        externalScope.launch {
            setIsLoading(true)
            val skuList = ArrayList<String>()
            skuList.add("premium_monthly")
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
            billingClient.querySkuDetails(params.build()).let {
                viewModelScope.launch {
                    mutableSkuDetails.value=it.skuDetailsList
                    setIsLoading(false)
                }
            }
        }
    }
    private fun handlePurchase(purchase: Purchase) {
        ioCoroutineScope.launch {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build(),acknowledgePurchaseResponseListener)
                }else{
                    setIsPremium(true)
                }
            }
        }
    }

    private val acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener {
        ioCoroutineScope.launch {
            if(it.responseCode == BillingClient.BillingResponseCode.OK){
                setIsPremium(true)
                mutableIsPurchase.emit(true)
            }else{
                setIsPremium(false)
            }
        }
    }

    fun checkSubs() {
        if(isBillingClientConnected){
            ioCoroutineScope.launch {
                val purchasesResult=billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS)

                var isPurchased=false
                for (purchase in purchasesResult.purchasesList) {
                    if (purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        isPurchased=true
                        break
                    }
                }
                setIsPremium(isPurchased)
            }
        }
    }

    fun setPurchase(skuDetails: SkuDetails,activity:Activity) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
    }
}