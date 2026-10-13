package com.example.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BillingRepository
import com.example.model.CoinPack
import com.example.model.GiftItem
import com.example.model.SenTransaction
import com.example.model.StoreItem
import com.example.model.formatSen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WalletViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {

    val coinPacks: StateFlow<List<CoinPack>> = billingRepository.coinPacksFlow
    val premiumStoreItems: List<StoreItem> = billingRepository.premiumStoreItems
    val giftItems: List<GiftItem> = billingRepository.giftItems

    init {
        viewModelScope.launch {
            billingRepository.billingMessageFlow.collect { message ->
                _snackbarEvent.emit(message)
                if (message.contains("added to your wallet", ignoreCase = true)) {
                    _showCoinBurst.value = true
                }
            }
        }
    }

    val balance: StateFlow<Int> = billingRepository.balanceFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 10)

    val unlockedItems: StateFlow<Set<String>> = billingRepository.unlockedItemsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, setOf("default_notebook", "default_font"))

    val activeSticker: StateFlow<String> = billingRepository.activeStickerFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "📓")

    val activeFont: StateFlow<String> = billingRepository.activeFontFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "default")

    val hapticEnabled: StateFlow<Boolean> = billingRepository.hapticEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val transactions: StateFlow<List<SenTransaction>> = billingRepository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Purchase Dialog & Animation states
    private val _selectedPackForPurchase = MutableStateFlow<CoinPack?>(null)
    val selectedPackForPurchase: StateFlow<CoinPack?> = _selectedPackForPurchase.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _purchaseSuccess = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess.asStateFlow()

    private val _showCoinBurst = MutableStateFlow(false)
    val showCoinBurst: StateFlow<Boolean> = _showCoinBurst.asStateFlow()

    private val _requestCoinStoreTab = MutableStateFlow(false)
    val requestCoinStoreTab: StateFlow<Boolean> = _requestCoinStoreTab.asStateFlow()

    fun openCoinStoreTab() {
        _requestCoinStoreTab.value = true
    }

    fun clearCoinStoreTabRequest() {
        _requestCoinStoreTab.value = false
    }

    // Gift Dialog & Animation states
    private val _selectedGift = MutableStateFlow<GiftItem?>(null)
    val selectedGift: StateFlow<GiftItem?> = _selectedGift.asStateFlow()

    private val _showGiftBurst = MutableStateFlow(false)
    val showGiftBurst: StateFlow<Boolean> = _showGiftBurst.asStateFlow()

    // Unlock Store Item dialog
    private val _selectedItemForUnlock = MutableStateFlow<StoreItem?>(null)
    val selectedItemForUnlock: StateFlow<StoreItem?> = _selectedItemForUnlock.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun openPurchaseSheet(pack: CoinPack) {
        _selectedPackForPurchase.value = pack
        _purchaseSuccess.value = false
        _isPurchasing.value = false
    }

    fun closePurchaseSheet() {
        if (!_isPurchasing.value) {
            _selectedPackForPurchase.value = null
            _purchaseSuccess.value = false
        }
    }

    fun confirmPurchase(activity: android.app.Activity? = null, onComplete: () -> Unit = {}) {
        val pack = _selectedPackForPurchase.value ?: return
        if (activity != null) {
            _isPurchasing.value = true
            billingRepository.launchPurchaseFlow(activity, pack)
            viewModelScope.launch {
                delay(600)
                _isPurchasing.value = false
                _selectedPackForPurchase.value = null
            }
        } else {
            viewModelScope.launch {
                _isPurchasing.value = false
                _selectedPackForPurchase.value = null
                _snackbarEvent.emit("Purchases need Google Play on a real phone. Please try again there.")
            }
        }
    }

    fun dismissCoinBurst() {
        _showCoinBurst.value = false
    }

    fun openGiftSheet() {
        _selectedGift.value = null
    }

    fun selectGiftItem(gift: GiftItem) {
        _selectedGift.value = gift
    }

    fun closeGiftSheet() {
        _selectedGift.value = null
    }

    fun confirmGift(gift: GiftItem, onInsufficientFunds: () -> Unit) {
        if (balance.value < gift.costSen) {
            viewModelScope.launch {
                _snackbarEvent.emit("Not enough sen! Buy more in the Coin Store.")
            }
            onInsufficientFunds()
            return
        }

        viewModelScope.launch {
            val res = billingRepository.sendGift(gift)
            if (res.isSuccess) {
                _selectedGift.value = null
                _showGiftBurst.value = true
                _snackbarEvent.emit("Thank you for sending ${gift.emoji} ${gift.name}!")
            }
        }
    }

    fun dismissGiftBurst() {
        _showGiftBurst.value = false
    }

    fun openUnlockSheet(item: StoreItem) {
        _selectedItemForUnlock.value = item
    }

    fun closeUnlockSheet() {
        _selectedItemForUnlock.value = null
    }

    fun confirmUnlock(item: StoreItem, onInsufficientFunds: () -> Unit) {
        if (balance.value < item.costSen) {
            viewModelScope.launch {
                _snackbarEvent.emit("Not enough sen! Need ${formatSen(item.costSen)}.")
            }
            onInsufficientFunds()
            return
        }

        viewModelScope.launch {
            val res = billingRepository.unlockStoreItem(item)
            if (res.isSuccess) {
                _selectedItemForUnlock.value = null
                // Auto use the unlocked item!
                if (item.category == com.example.model.StoreItemCategory.STICKER) {
                    billingRepository.setActiveSticker(item.preview)
                    _snackbarEvent.emit("Unlocked & equipped ${item.name} sticker!")
                } else if (item.fontKey != null) {
                    billingRepository.setActiveFont(item.fontKey)
                    _snackbarEvent.emit("Unlocked & applied ${item.name} font!")
                }
            } else {
                _snackbarEvent.emit("Failed to unlock item")
            }
        }
    }

    fun useSticker(emoji: String) {
        viewModelScope.launch {
            billingRepository.setActiveSticker(emoji)
            _snackbarEvent.emit("Avatar updated!")
        }
    }

    fun useFont(fontKey: String) {
        viewModelScope.launch {
            billingRepository.setActiveFont(fontKey)
            _snackbarEvent.emit("Default note font changed!")
        }
    }

    fun toggleHaptic(enabled: Boolean) {
        viewModelScope.launch {
            billingRepository.setHapticEnabled(enabled)
        }
    }
}
