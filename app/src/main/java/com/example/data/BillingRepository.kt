package com.example.data

import android.app.Activity
import com.example.model.CoinPack
import com.example.model.GiftItem
import com.example.model.SenTransaction
import com.example.model.StoreItem
import com.example.model.StoreItemCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val coinPacks: List<CoinPack>
    val coinPacksFlow: StateFlow<List<CoinPack>>
    val premiumStoreItems: List<StoreItem>
    val giftItems: List<GiftItem>

    val balanceFlow: Flow<Int>
    val unlockedItemsFlow: Flow<Set<String>>
    val activeStickerFlow: Flow<String>
    val activeFontFlow: Flow<String>
    val hapticEnabledFlow: Flow<Boolean>
    val transactionsFlow: Flow<List<SenTransaction>>
    val billingMessageFlow: Flow<String>

    fun launchPurchaseFlow(activity: Activity, pack: CoinPack)
    suspend fun unlockStoreItem(item: StoreItem): Result<Int>
    suspend fun sendGift(gift: GiftItem): Result<Int>
    suspend fun setActiveSticker(stickerEmoji: String)
    suspend fun setActiveFont(fontKey: String)
    suspend fun setHapticEnabled(enabled: Boolean)
}

val PREM_STORE_ITEMS: List<StoreItem> = listOf(
    // Stickers
    StoreItem(id = "sticker_fox", name = "Fox", category = StoreItemCategory.STICKER, preview = "🦊", costSen = 100),
    StoreItem(id = "sticker_panda", name = "Panda", category = StoreItemCategory.STICKER, preview = "🐼", costSen = 100),
    StoreItem(id = "sticker_rocket", name = "Rocket", category = StoreItemCategory.STICKER, preview = "🚀", costSen = 200),
    StoreItem(id = "sticker_moon", name = "Moon", category = StoreItemCategory.STICKER, preview = "🌙", costSen = 200),
    StoreItem(id = "sticker_crown", name = "Crown", category = StoreItemCategory.STICKER, preview = "👑", costSen = 300),
    StoreItem(id = "sticker_dragon", name = "Dragon", category = StoreItemCategory.STICKER, preview = "🐉", costSen = 300),

    // Fonts
    StoreItem(id = "font_serif", name = "Serif Classic", category = StoreItemCategory.FONT, preview = "Aa Elegant", fontKey = "serif", costSen = 200),
    StoreItem(id = "font_mono", name = "Mono Code", category = StoreItemCategory.FONT, preview = "01 Binary", fontKey = "mono", costSen = 200),
    StoreItem(id = "font_cursive", name = "Cursive Script", category = StoreItemCategory.FONT, preview = "Ss Calligraphy", fontKey = "cursive", costSen = 300),
    StoreItem(id = "font_condensed", name = "Condensed Sans", category = StoreItemCategory.FONT, preview = "Tt Modern", fontKey = "condensed", costSen = 200)
)

val STORE_GIFT_ITEMS: List<GiftItem> = listOf(
    GiftItem(id = "gift_coffee", name = "Coffee", emoji = "☕", costSen = 100),
    GiftItem(id = "gift_cake", name = "Cake", emoji = "🍰", costSen = 200),
    GiftItem(id = "gift_pizza", name = "Pizza", emoji = "🍕", costSen = 300),
    GiftItem(id = "gift_giftbox", name = "Gift box", emoji = "🎁", costSen = 500)
)
