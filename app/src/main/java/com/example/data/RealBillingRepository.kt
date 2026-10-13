package com.example.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.model.COIN_PACKS
import com.example.model.CoinPack
import com.example.model.GiftItem
import com.example.model.SenTransaction
import com.example.model.StoreItem
import com.example.model.TransactionType
import com.example.model.formatSen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RealBillingRepository(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) : BillingRepository, PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Keep showing pack prices from our list when Google Play prices are not available
    private val _coinPacksFlow = MutableStateFlow(COIN_PACKS)
    override val coinPacksFlow: StateFlow<List<CoinPack>> = _coinPacksFlow.asStateFlow()
    override val coinPacks: List<CoinPack> get() = _coinPacksFlow.value

    override val premiumStoreItems: List<StoreItem> get() = PREM_STORE_ITEMS
    override val giftItems: List<GiftItem> get() = STORE_GIFT_ITEMS

    override val balanceFlow: Flow<Int> = dataStoreManager.walletBalanceFlow
    override val unlockedItemsFlow: Flow<Set<String>> = dataStoreManager.unlockedItemsFlow
    override val activeStickerFlow: Flow<String> = dataStoreManager.activeStickerFlow
    override val activeFontFlow: Flow<String> = dataStoreManager.activeFontFlow
    override val hapticEnabledFlow: Flow<Boolean> = dataStoreManager.hapticEnabledFlow
    override val transactionsFlow: Flow<List<SenTransaction>> = dataStoreManager.transactionsFlow

    private val _billingMessageFlow = MutableSharedFlow<String>(extraBufferCapacity = 15)
    override val billingMessageFlow: Flow<String> = _billingMessageFlow.asSharedFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryAndProcessExistingPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Automatically reconnect after a delay
                scope.launch {
                    delay(3000)
                    startBillingConnection()
                }
            }
        })
    }

    private fun queryProductDetails() {
        val productList = COIN_PACKS.map { pack ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(pack.id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, detailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !detailsList.isNullOrEmpty()) {
                synchronized(productDetailsMap) {
                    detailsList.forEach { details ->
                        productDetailsMap[details.productId] = details
                    }
                }

                // Update coin packs with real localized prices from Google Play
                val updatedPacks = COIN_PACKS.map { pack ->
                    val details = detailsList.find { it.productId == pack.id }
                    val formattedPrice = details?.oneTimePurchaseOfferDetails?.formattedPrice
                    if (formattedPrice != null) {
                        pack.copy(displayPrice = formattedPrice)
                    } else {
                        pack
                    }
                }
                _coinPacksFlow.value = updatedPacks
            }
        }
    }

    private fun queryAndProcessExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                scope.launch {
                    purchases.forEach { purchase ->
                        processPurchase(purchase)
                    }
                }
            }
        }
    }

    override fun launchPurchaseFlow(activity: Activity, pack: CoinPack) {
        if (!billingClient.isReady) {
            _billingMessageFlow.tryEmit("Purchases need Google Play on a real phone. Please try again there.")
            startBillingConnection()
            return
        }

        val details = synchronized(productDetailsMap) { productDetailsMap[pack.id] }
        if (details == null) {
            _billingMessageFlow.tryEmit("Purchases need Google Play on a real phone. Please try again there.")
            queryProductDetails()
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            _billingMessageFlow.tryEmit("Purchases need Google Play on a real phone. Please try again there.")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    scope.launch {
                        purchases.forEach { purchase ->
                            processPurchase(purchase)
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingMessageFlow.tryEmit("Purchase was cancelled")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingMessageFlow.tryEmit("Item already owned. Consuming...")
                queryAndProcessExistingPurchases()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                _billingMessageFlow.tryEmit("Purchases need Google Play on a real phone. Please try again there.")
            }
            else -> {
                _billingMessageFlow.tryEmit("Purchases need Google Play on a real phone. Please try again there.")
            }
        }
    }

    private suspend fun processPurchase(purchase: Purchase) {
        // Do not grant anything for PENDING purchases
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                _billingMessageFlow.tryEmit("Payment is pending. Sen will be added once approved.")
            }
            return
        }

        val token = purchase.purchaseToken
        // Never grant sen twice for the same purchase token
        val isAlreadyProcessed = dataStoreManager.isPurchaseTokenProcessed(token)
        if (isAlreadyProcessed) {
            consumePurchase(purchase)
            return
        }

        val productId = purchase.products.firstOrNull() ?: return
        val pack = COIN_PACKS.find { it.id == productId } ?: return

        // Store processed token locally before grant
        dataStoreManager.addProcessedPurchaseToken(token)

        // Grant sen ONLY after a real purchase reaches PURCHASED
        val currentBalance = dataStoreManager.walletBalanceFlow.first()
        val newBalance = currentBalance + pack.senAmount
        dataStoreManager.setWalletBalance(newBalance)

        // Add to history
        val tx = SenTransaction(
            id = "tx_${purchase.orderId ?: token.take(12)}",
            title = "Bought ${formatSen(pack.senAmount)}",
            description = "Google Play (${pack.displayPrice})",
            amountSen = pack.senAmount,
            type = TransactionType.PURCHASE,
            timestamp = purchase.purchaseTime,
            formattedDate = formatDate(purchase.purchaseTime)
        )
        dataStoreManager.addTransaction(tx)
        _billingMessageFlow.tryEmit("${formatSen(pack.senAmount)} added to your wallet!")

        // CONSUME the purchase so the pack can be bought again
        consumePurchase(purchase)
    }

    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumeAsync(consumeParams) { _, _ ->
            // Consumption completed
        }
    }

    override suspend fun unlockStoreItem(item: StoreItem): Result<Int> {
        val currentBalance = balanceFlow.first()
        if (currentBalance < item.costSen) {
            return Result.failure(IllegalStateException("Not enough sen"))
        }

        val newBalance = currentBalance - item.costSen
        dataStoreManager.setWalletBalance(newBalance)
        dataStoreManager.unlockItem(item.id)

        val tx = SenTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = "Unlocked ${item.name}",
            description = "Spent ${formatSen(item.costSen)}",
            amountSen = -item.costSen,
            type = TransactionType.UNLOCK,
            timestamp = System.currentTimeMillis(),
            formattedDate = formatDate(System.currentTimeMillis())
        )
        dataStoreManager.addTransaction(tx)
        return Result.success(newBalance)
    }

    override suspend fun sendGift(gift: GiftItem): Result<Int> {
        val currentBalance = balanceFlow.first()
        if (currentBalance < gift.costSen) {
            return Result.failure(IllegalStateException("Not enough sen"))
        }

        val newBalance = currentBalance - gift.costSen
        dataStoreManager.setWalletBalance(newBalance)

        val tx = SenTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = "Sent ${gift.emoji} ${gift.name}",
            description = "Creator tip (-${formatSen(gift.costSen)})",
            amountSen = -gift.costSen,
            type = TransactionType.GIFT,
            timestamp = System.currentTimeMillis(),
            formattedDate = formatDate(System.currentTimeMillis())
        )
        dataStoreManager.addTransaction(tx)
        return Result.success(newBalance)
    }

    override suspend fun setActiveSticker(stickerEmoji: String) {
        dataStoreManager.setActiveSticker(stickerEmoji)
    }

    override suspend fun setActiveFont(fontKey: String) {
        dataStoreManager.setActiveFont(fontKey)
    }

    override suspend fun setHapticEnabled(enabled: Boolean) {
        dataStoreManager.setHapticEnabled(enabled)
    }

    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }
}
