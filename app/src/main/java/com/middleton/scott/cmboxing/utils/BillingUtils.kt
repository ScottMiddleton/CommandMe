package com.middleton.scott.cmboxing.utils

import android.app.Activity
import android.util.Log
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.middleton.scott.cmboxing.other.Constants

lateinit var billingClient: BillingClient

fun Activity.setUpBillingClient(purchaseUpdateListener: PurchasesUpdatedListener) {
    billingClient = BillingClient.newBuilder(this)
        .setListener(purchaseUpdateListener)
        .enablePendingPurchases()
        .build()
}

fun startConnectionForProducts(onSkuDetailsListResponse: ((List<SkuDetails>)) -> Unit) {
    billingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.v("TAG_INAPP", "Setup Billing Done")
                // The BillingClient is ready. You can query purchases here.
                queryAvailableProducts(onSkuDetailsListResponse)
            }
        }

        override fun onBillingServiceDisconnected() {
            Log.v("TAG_INAPP", "Billing client Disconnected")
            // Try to restart the connection on the next request to
            // Google Play by calling the startConnection() method.
        }
    })
}

fun startConnectionPurchaseHistory(onPurchaseListResponse: ((List<PurchaseHistoryRecord>)) -> Unit) {
    billingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.v("TAG_INAPP", "Setup Billing Done")
                // The BillingClient is ready. You can query purchases here.
                queryPurchaseHistory(onPurchaseListResponse)
            }
        }

        override fun onBillingServiceDisconnected() {
            Log.v("TAG_INAPP", "Billing client Disconnected")
            // Try to restart the connection on the next request to
            // Google Play by calling the startConnection() method.
        }
    })
}

private fun queryAvailableProducts(onSkuDetailsListResponse: ((List<SkuDetails>)) -> Unit) {
    val skuList = ArrayList<String>()
    skuList.add(Constants.PRODUCT_UNLIMITED_COMMANDS)
    val params = SkuDetailsParams.newBuilder()
    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

    billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
        // Process the result.
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
            && !skuDetailsList.isNullOrEmpty()

        ) {
            onSkuDetailsListResponse(skuDetailsList)
        }
    }
}

fun Fragment.launchBillingFlow(skuDetails: SkuDetails) {
    val billingFlowParams = BillingFlowParams.newBuilder()
        .setSkuDetails(skuDetails)
        .build()
    this.activity?.let {
        billingClient.launchBillingFlow(
            it,
            billingFlowParams
        ).responseCode

        billingClient.launchBillingFlow(it, billingFlowParams)
    }
}

fun Activity.launchBillingFlow(skuDetails: SkuDetails) {
    val billingFlowParams = BillingFlowParams.newBuilder()
        .setSkuDetails(skuDetails)
        .build()
    this.let {
        billingClient.launchBillingFlow(
            it,
            billingFlowParams
        ).responseCode

        billingClient.launchBillingFlow(it, billingFlowParams)
    }
}

private fun queryPurchaseHistory(onPurchaseListResponse: ((List<PurchaseHistoryRecord>)) -> Unit) {
    billingClient.queryPurchaseHistoryAsync(
        BillingClient.SkuType.INAPP
    ) { billingResult, purchasesList ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
            && purchasesList != null
        ) {
            onPurchaseListResponse(purchasesList)
        }
    }
}


