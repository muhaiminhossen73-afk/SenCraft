package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.Note
import com.example.model.NoteCategory
import com.example.model.SenTransaction
import com.example.model.ShoppingItem
import com.example.model.TransactionType
import com.example.model.User
import com.example.model.formatSen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Context.senDataStore: DataStore<Preferences> by preferencesDataStore(name = "sencraft_preferences")

class DataStoreManager(private val context: Context) {

    private val KEY_USER_JSON = stringPreferencesKey("user_json")
    private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
    private val KEY_SAVED_EMAIL = stringPreferencesKey("saved_email")

    private val KEY_WALLET_BALANCE = intPreferencesKey("wallet_balance")
    private val KEY_DATA_VERSION = intPreferencesKey("data_version")
    private val KEY_UNLOCKED_ITEMS = stringPreferencesKey("unlocked_items")
    private val KEY_ACTIVE_STICKER = stringPreferencesKey("active_sticker")
    private val KEY_ACTIVE_FONT = stringPreferencesKey("active_font")
    private val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")

    private val KEY_TRANSACTIONS_JSON = stringPreferencesKey("transactions_json")
    private val KEY_NOTES_JSON = stringPreferencesKey("notes_json")
    private val KEY_PROCESSED_PURCHASE_TOKENS = stringSetPreferencesKey("processed_purchase_tokens")

    init {
        CoroutineScope(Dispatchers.IO).launch {
            migrateDataIfNeeded()
        }
    }

    suspend fun migrateDataIfNeeded() {
        context.senDataStore.edit { prefs ->
            val version = prefs[KEY_DATA_VERSION] ?: 1
            if (version < 2) {
                // 1. Multiply existing balance by 100
                val existingBalance = prefs[KEY_WALLET_BALANCE]
                if (existingBalance != null) {
                    prefs[KEY_WALLET_BALANCE] = existingBalance * 100
                } else {
                    prefs[KEY_WALLET_BALANCE] = 1000 // 10.00 sen in hundredths
                }

                // 2. Multiply history amounts by 100
                val rawTx = prefs[KEY_TRANSACTIONS_JSON]
                if (!rawTx.isNullOrBlank()) {
                    val txList = parseTransactions(rawTx)
                    val updatedTxList = txList.map { tx ->
                        val updatedAmount = tx.amountSen * 100
                        val updatedDesc = if (tx.description.contains("Spent ") && tx.description.endsWith(" sen")) {
                            "Spent ${formatSen(kotlin.math.abs(updatedAmount))}"
                        } else if (tx.description.contains("Creator tip (-") && tx.description.endsWith(" sen)")) {
                            "Creator tip (-${formatSen(kotlin.math.abs(updatedAmount))})"
                        } else {
                            tx.description
                        }
                        val updatedTitle = if (tx.title.startsWith("Bought ") && tx.title.endsWith(" sen")) {
                            "Bought ${formatSen(updatedAmount)}"
                        } else {
                            tx.title
                        }
                        tx.copy(
                            amountSen = updatedAmount,
                            title = updatedTitle,
                            description = updatedDesc
                        )
                    }
                    prefs[KEY_TRANSACTIONS_JSON] = transactionsToJson(updatedTxList)
                }

                prefs[KEY_DATA_VERSION] = 2
            }
        }
    }

    // Flow for current user
    val userFlow: Flow<User?> = context.senDataStore.data.map { prefs ->
        val raw = prefs[KEY_USER_JSON]
        if (raw.isNullOrBlank()) null else parseUser(raw)
    }

    val rememberMeFlow: Flow<Pair<Boolean, String>> = context.senDataStore.data.map { prefs ->
        Pair(prefs[KEY_REMEMBER_ME] ?: false, prefs[KEY_SAVED_EMAIL] ?: "")
    }

    val walletBalanceFlow: Flow<Int> = context.senDataStore.data.map { prefs ->
        prefs[KEY_WALLET_BALANCE] ?: 1000 // Start with 10 sen welcome bonus (1000 hundredths)
    }

    val unlockedItemsFlow: Flow<Set<String>> = context.senDataStore.data.map { prefs ->
        val raw = prefs[KEY_UNLOCKED_ITEMS] ?: ""
        if (raw.isBlank()) setOf("default_notebook", "default_font")
        else raw.split(",").filter { it.isNotBlank() }.toSet() + setOf("default_notebook", "default_font")
    }

    val activeStickerFlow: Flow<String> = context.senDataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_STICKER] ?: "📓"
    }

    val activeFontFlow: Flow<String> = context.senDataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_FONT] ?: "default"
    }

    val hapticEnabledFlow: Flow<Boolean> = context.senDataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_ENABLED] ?: true
    }

    val transactionsFlow: Flow<List<SenTransaction>> = context.senDataStore.data.map { prefs ->
        val raw = prefs[KEY_TRANSACTIONS_JSON] ?: "[]"
        parseTransactions(raw)
    }

    val notesFlow: Flow<List<Note>> = context.senDataStore.data.map { prefs ->
        val raw = prefs[KEY_NOTES_JSON]
        if (raw.isNullOrBlank()) getInitialNotes()
        else parseNotes(raw)
    }

    suspend fun saveUser(user: User?, rememberMe: Boolean, emailToRemember: String) {
        context.senDataStore.edit { prefs ->
            if (user == null) {
                prefs.remove(KEY_USER_JSON)
            } else {
                prefs[KEY_USER_JSON] = userToJson(user)
            }
            prefs[KEY_REMEMBER_ME] = rememberMe
            if (rememberMe) {
                prefs[KEY_SAVED_EMAIL] = emailToRemember
            } else {
                prefs.remove(KEY_SAVED_EMAIL)
            }
        }
    }

    suspend fun updateUserName(name: String) {
        context.senDataStore.edit { prefs ->
            val raw = prefs[KEY_USER_JSON]
            if (!raw.isNullOrBlank()) {
                val user = parseUser(raw)
                val updated = user.copy(name = name)
                prefs[KEY_USER_JSON] = userToJson(updated)
            }
        }
    }

    suspend fun setWalletBalance(balance: Int) {
        context.senDataStore.edit { prefs ->
            prefs[KEY_WALLET_BALANCE] = balance
        }
    }

    suspend fun unlockItem(itemId: String) {
        context.senDataStore.edit { prefs ->
            val current = prefs[KEY_UNLOCKED_ITEMS] ?: ""
            val list = current.split(",").filter { it.isNotBlank() }.toMutableSet()
            list.add(itemId)
            prefs[KEY_UNLOCKED_ITEMS] = list.joinToString(",")
        }
    }

    suspend fun setActiveSticker(stickerEmoji: String) {
        context.senDataStore.edit { prefs ->
            prefs[KEY_ACTIVE_STICKER] = stickerEmoji
            val raw = prefs[KEY_USER_JSON]
            if (!raw.isNullOrBlank()) {
                val user = parseUser(raw)
                prefs[KEY_USER_JSON] = userToJson(user.copy(avatarSticker = stickerEmoji))
            }
        }
    }

    suspend fun setActiveFont(fontKey: String) {
        context.senDataStore.edit { prefs ->
            prefs[KEY_ACTIVE_FONT] = fontKey
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.senDataStore.edit { prefs ->
            prefs[KEY_HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun addTransaction(transaction: SenTransaction) {
        context.senDataStore.edit { prefs ->
            val raw = prefs[KEY_TRANSACTIONS_JSON] ?: "[]"
            val list = parseTransactions(raw).toMutableList()
            list.add(0, transaction) // latest first
            prefs[KEY_TRANSACTIONS_JSON] = transactionsToJson(list)
        }
    }

    suspend fun saveNotes(notes: List<Note>) {
        context.senDataStore.edit { prefs ->
            prefs[KEY_NOTES_JSON] = notesToJson(notes)
        }
    }

    suspend fun clearAllNotes() {
        context.senDataStore.edit { prefs ->
            prefs[KEY_NOTES_JSON] = "[]"
        }
    }

    val processedPurchaseTokensFlow: Flow<Set<String>> = context.senDataStore.data.map { prefs ->
        prefs[KEY_PROCESSED_PURCHASE_TOKENS] ?: emptySet()
    }

    suspend fun isPurchaseTokenProcessed(token: String): Boolean {
        val prefs = context.senDataStore.data.first()
        return prefs[KEY_PROCESSED_PURCHASE_TOKENS]?.contains(token) ?: false
    }

    suspend fun addProcessedPurchaseToken(token: String) {
        context.senDataStore.edit { prefs ->
            val current = prefs[KEY_PROCESSED_PURCHASE_TOKENS] ?: emptySet()
            prefs[KEY_PROCESSED_PURCHASE_TOKENS] = current + token
        }
    }

    suspend fun wipeUserData() {
        context.senDataStore.edit { prefs ->
            prefs.clear()
            prefs[KEY_DATA_VERSION] = 2
            prefs[KEY_WALLET_BALANCE] = 1000
        }
        try {
            val root = context.filesDir.parentFile
            val sharedPrefsDir = java.io.File(root, "shared_prefs")
            if (sharedPrefsDir.exists()) {
                sharedPrefsDir.listFiles()?.forEach { it.delete() }
            }
        } catch (_: Exception) {}
    }

    // JSON Serializers & Deserializers
    private fun userToJson(u: User): String {
        return JSONObject().apply {
            put("id", u.id)
            put("name", u.name)
            put("email", u.email)
            put("joinedDate", u.joinedDate)
            put("avatarSticker", u.avatarSticker)
            put("isLoggedIn", u.isLoggedIn)
        }.toString()
    }

    private fun parseUser(jsonStr: String): User {
        val obj = JSONObject(jsonStr)
        return User(
            id = obj.optString("id", "user_1"),
            name = obj.optString("name", "User"),
            email = obj.optString("email", ""),
            joinedDate = obj.optString("joinedDate", "October 2026"),
            avatarSticker = obj.optString("avatarSticker", "📓"),
            isLoggedIn = obj.optBoolean("isLoggedIn", false)
        )
    }

    private fun transactionsToJson(list: List<SenTransaction>): String {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("description", item.description)
                put("amountSen", item.amountSen)
                put("type", item.type.name)
                put("timestamp", item.timestamp)
                put("formattedDate", item.formattedDate)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseTransactions(jsonStr: String): List<SenTransaction> {
        val list = mutableListOf<SenTransaction>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SenTransaction(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        amountSen = obj.getInt("amountSen"),
                        type = TransactionType.valueOf(obj.optString("type", TransactionType.PURCHASE.name)),
                        timestamp = obj.getLong("timestamp"),
                        formattedDate = obj.optString("formattedDate", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun notesToJson(notes: List<Note>): String {
        val array = JSONArray()
        for (n in notes) {
            val obj = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("body", n.body)
                put("category", n.category.id)
                put("isFavorite", n.isFavorite)
                put("subject", n.subject ?: "")
                put("fontKey", n.fontKey)
                put("createdAt", n.createdAt)
                put("updatedAt", n.updatedAt)

                val itemsArray = JSONArray()
                for (item in n.shoppingItems) {
                    val itemObj = JSONObject().apply {
                        put("id", item.id)
                        put("text", item.text)
                        put("isChecked", item.isChecked)
                    }
                    itemsArray.put(itemObj)
                }
                put("shoppingItems", itemsArray)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseNotes(jsonStr: String): List<Note> {
        val list = mutableListOf<Note>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val catStr = obj.optString("category", "normal")
                val category = NoteCategory.entries.find { it.id == catStr } ?: NoteCategory.NORMAL
                val items = mutableListOf<ShoppingItem>()
                val itemsArray = obj.optJSONArray("shoppingItems")
                if (itemsArray != null) {
                    for (j in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(j)
                        items.add(
                            ShoppingItem(
                                id = itemObj.getString("id"),
                                text = itemObj.getString("text"),
                                isChecked = itemObj.optBoolean("isChecked", false)
                            )
                        )
                    }
                }
                list.add(
                    Note(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        body = obj.optString("body", ""),
                        category = category,
                        isFavorite = obj.optBoolean("isFavorite", false),
                        subject = obj.optString("subject", "").takeIf { it.isNotBlank() },
                        shoppingItems = items,
                        fontKey = obj.optString("fontKey", "default"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun getInitialNotes(): List<Note> {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        return listOf(
            Note(
                id = "note_welcome",
                title = "Welcome to SenCraft",
                body = "SenCraft brings a dark luxury green aesthetic to your daily thoughts. Collect sen coins to customize your journal with unique stickers and classic typography.",
                category = NoteCategory.NORMAL,
                isFavorite = true,
                createdAt = now - 2 * oneDay,
                updatedAt = now - 2 * oneDay
            ),
            Note(
                id = "note_dinner",
                title = "Gourmet Dinner Ingredients",
                body = "Dinner party recipes and supplies",
                category = NoteCategory.SHOPPING,
                isFavorite = false,
                shoppingItems = listOf(
                    ShoppingItem("item_1", "Organic Matcha Green Tea", true),
                    ShoppingItem("item_2", "Madagascar Vanilla Beans", false),
                    ShoppingItem("item_3", "Artisan Sourdough Loaf", false),
                    ShoppingItem("item_4", "Fresh Mint Leaves", true)
                ),
                createdAt = now - oneDay,
                updatedAt = now - oneDay
            ),
            Note(
                id = "note_lecture",
                title = "Mobile Architecture Lecture",
                body = "Key takeaways: Clean Architecture separation, reactive UI with Compose, StateFlow emission, and local persistence for offline reliability.",
                category = NoteCategory.SCHOOL,
                subject = "CS 402 - Systems Architecture",
                isFavorite = true,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
