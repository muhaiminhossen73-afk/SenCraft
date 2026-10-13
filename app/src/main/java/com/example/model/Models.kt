package com.example.model

import androidx.compose.ui.text.font.FontFamily
import java.util.Locale

enum class NoteCategory(val id: String, val displayName: String, val iconEmoji: String) {
    NORMAL("normal", "Normal Notes", "📝"),
    SHOPPING("shopping", "Shopping List", "🛒"),
    SCHOOL("school", "School Notes", "🎓")
}

data class ShoppingItem(
    val id: String,
    val text: String,
    val isChecked: Boolean = false
)

data class Note(
    val id: String,
    val title: String,
    val body: String = "",
    val category: NoteCategory = NoteCategory.NORMAL,
    val isFavorite: Boolean = false,
    val subject: String? = null,
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val fontKey: String = "default",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val joinedDate: String = "October 2026",
    val avatarSticker: String = "📓",
    val isLoggedIn: Boolean = false
)

enum class TransactionType {
    PURCHASE,
    UNLOCK,
    GIFT
}

data class SenTransaction(
    val id: String,
    val title: String,
    val description: String,
    val amountSen: Int,
    val type: TransactionType,
    val timestamp: Long,
    val formattedDate: String
)

fun formatSenAmount(hundredths: Long): String {
    val isNeg = hundredths < 0
    val absVal = kotlin.math.abs(hundredths)
    val whole = absVal / 100
    val rem = (absVal % 100).toInt()
    val numStr = when {
        rem == 0 -> "$whole"
        rem % 10 == 0 -> "$whole.${rem / 10}"
        else -> String.format(Locale.US, "%d.%02d", whole, rem)
    }
    return if (isNeg) "-$numStr" else numStr
}

fun formatSenAmount(hundredths: Int): String = formatSenAmount(hundredths.toLong())

fun formatSen(hundredths: Long): String = "${formatSenAmount(hundredths)} sen"
fun formatSen(hundredths: Int): String = "${formatSenAmount(hundredths)} sen"

data class CoinPack(
    val id: String,
    val senAmount: Int, // in hundredths of a sen (51 = 0.51 sen, 100 = 1.00 sen)
    val priceCents: Int, // in USD cents (51 = $0.51, 100 = $1.00)
    val displayPrice: String
)

val COIN_PACKS: List<CoinPack> = listOf(
    CoinPack(id = "sen_pack_0051", senAmount = 51, priceCents = 51, displayPrice = "$0.51"),
    CoinPack(id = "sen_pack_0100", senAmount = 100, priceCents = 100, displayPrice = "$1.00"),
    CoinPack(id = "sen_pack_0101", senAmount = 101, priceCents = 101, displayPrice = "$1.01"),
    CoinPack(id = "sen_pack_0200", senAmount = 200, priceCents = 200, displayPrice = "$2.00"),
    CoinPack(id = "sen_pack_0201", senAmount = 201, priceCents = 201, displayPrice = "$2.01"),
    CoinPack(id = "sen_pack_0300", senAmount = 300, priceCents = 300, displayPrice = "$3.00"),
    CoinPack(id = "sen_pack_0301", senAmount = 301, priceCents = 301, displayPrice = "$3.01"),
    CoinPack(id = "sen_pack_0401", senAmount = 401, priceCents = 401, displayPrice = "$4.01"),
    CoinPack(id = "sen_pack_0501", senAmount = 501, priceCents = 501, displayPrice = "$5.01"),
    CoinPack(id = "sen_pack_1001", senAmount = 1001, priceCents = 1001, displayPrice = "$10.01"),
    CoinPack(id = "sen_pack_4005", senAmount = 4005, priceCents = 4005, displayPrice = "$40.05"),
    CoinPack(id = "sen_pack_5001", senAmount = 5001, priceCents = 5001, displayPrice = "$50.01")
)

enum class StoreItemCategory {
    STICKER,
    FONT
}

data class StoreItem(
    val id: String,
    val name: String,
    val category: StoreItemCategory,
    val preview: String,
    val fontKey: String? = null,
    val costSen: Int
)

data class GiftItem(
    val id: String,
    val name: String,
    val emoji: String,
    val costSen: Int
)

object FontRegistry {
    fun getFontFamily(fontKey: String): FontFamily {
        return when (fontKey) {
            "serif" -> FontFamily.Serif
            "mono" -> FontFamily.Monospace
            "cursive" -> FontFamily.Cursive
            "condensed" -> FontFamily.SansSerif
            else -> FontFamily.Default
        }
    }

    fun getFontDisplayName(fontKey: String): String {
        return when (fontKey) {
            "serif" -> "Serif Classic"
            "mono" -> "Mono Code"
            "cursive" -> "Cursive Script"
            "condensed" -> "Condensed Sans"
            else -> "System Default"
        }
    }
}
