package com.example.ui.store

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FontRegistry
import com.example.model.StoreItem
import com.example.model.StoreItemCategory
import com.example.model.formatSen
import com.example.ui.components.CoinChip
import com.example.ui.components.SenCard
import com.example.ui.components.SenPillButton
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBorderSubtle
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenCardSurface
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumStoreScreen(
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCoinStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance by walletViewModel.balance.collectAsState()
    val animatedBalance by animateIntAsState(targetValue = balance, animationSpec = tween(500), label = "store_balance")
    val unlockedItems by walletViewModel.unlockedItems.collectAsState()
    val activeSticker by walletViewModel.activeSticker.collectAsState()
    val activeFont by walletViewModel.activeFont.collectAsState()
    val selectedItemForUnlock by walletViewModel.selectedItemForUnlock.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        walletViewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val stickers = walletViewModel.premiumStoreItems.filter { it.category == StoreItemCategory.STICKER }
    val fonts = walletViewModel.premiumStoreItems.filter { it.category == StoreItemCategory.FONT }

    Box(modifier = modifier.fillMaxSize()) {
        TuftedLeatherBackground(modifier = Modifier.fillMaxSize(), isFaintHeaderOnly = true)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onNavigateBack),
                        shape = CircleShape,
                        color = SenRaisedSurface,
                        border = BorderStroke(1.dp, SenBorderStrong)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SenTextWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Premium Store",
                        color = SenTextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                CoinChip(
                    balance = animatedBalance,
                    onClick = onNavigateToCoinStore
                )
            }

            HorizontalDivider(color = SenBorderSubtle)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Profile Stickers
                item {
                    Column {
                        Text(
                            text = "Profile Stickers",
                            color = SenTextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wear luxury emojis as your journal avatar",
                            color = SenTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Grid of stickers (2 columns)
                val stickerPairs = stickers.chunked(2)
                stickerPairs.forEach { pair ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            StoreItemCard(
                                item = pair[0],
                                isUnlocked = unlockedItems.contains(pair[0].id),
                                isCurrentlyUsed = activeSticker == pair[0].preview,
                                onUnlockClick = { walletViewModel.openUnlockSheet(pair[0]) },
                                onUseClick = { walletViewModel.useSticker(pair[0].preview) },
                                modifier = Modifier.weight(1f)
                            )

                            if (pair.size > 1) {
                                StoreItemCard(
                                    item = pair[1],
                                    isUnlocked = unlockedItems.contains(pair[1].id),
                                    isCurrentlyUsed = activeSticker == pair[1].preview,
                                    onUnlockClick = { walletViewModel.openUnlockSheet(pair[1]) },
                                    onUseClick = { walletViewModel.useSticker(pair[1].preview) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Section 2: Premium Fonts
                item {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "Premium Fonts",
                            color = SenTextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Curated typography to elevate your reading & note-taking experience",
                            color = SenTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Grid of fonts (2 columns)
                val fontPairs = fonts.chunked(2)
                fontPairs.forEach { pair ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            StoreItemCard(
                                item = pair[0],
                                isUnlocked = unlockedItems.contains(pair[0].id),
                                isCurrentlyUsed = activeFont == pair[0].fontKey,
                                onUnlockClick = { walletViewModel.openUnlockSheet(pair[0]) },
                                onUseClick = { pair[0].fontKey?.let { walletViewModel.useFont(it) } },
                                modifier = Modifier.weight(1f)
                            )

                            if (pair.size > 1) {
                                StoreItemCard(
                                    item = pair[1],
                                    isUnlocked = unlockedItems.contains(pair[1].id),
                                    isCurrentlyUsed = activeFont == pair[1].fontKey,
                                    onUnlockClick = { walletViewModel.openUnlockSheet(pair[1]) },
                                    onUseClick = { pair[1].fontKey?.let { walletViewModel.useFont(it) } },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Overlay Snackbar Host
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }

        // Unlock Confirmation Bottom Sheet
        if (selectedItemForUnlock != null) {
            val item = selectedItemForUnlock!!
            val sheetState = rememberModalBottomSheetState()
            val hasEnoughSen = balance >= item.costSen

            ModalBottomSheet(
                onDismissRequest = { walletViewModel.closeUnlockSheet() },
                sheetState = sheetState,
                containerColor = SenCardSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Unlock ${item.name} for ${formatSen(item.costSen)}?",
                        color = SenTextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Preview
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = SenRaisedSurface,
                        border = BorderStroke(1.dp, SenBorderStrong)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (item.category == StoreItemCategory.STICKER) {
                                Text(text = item.preview, fontSize = 38.sp)
                            } else {
                                Text(
                                    text = "Aa",
                                    fontFamily = FontRegistry.getFontFamily(item.fontKey ?: "default"),
                                    fontSize = 28.sp,
                                    color = SenMint,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (hasEnoughSen) "Your current balance is ${formatSen(balance)}. After unlock you will have ${formatSen(balance - item.costSen)}."
                               else "You currently have ${formatSen(balance)}, which is not enough for this ${formatSen(item.costSen)} unlock.",
                        color = SenTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (hasEnoughSen) {
                        SenPillButton(
                            text = "Unlock (${formatSen(item.costSen)})",
                            onClick = {
                                walletViewModel.confirmUnlock(item) {}
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        SenPillButton(
                            text = "Not enough sen · Get Coins",
                            onClick = {
                                walletViewModel.closeUnlockSheet()
                                onNavigateToCoinStore()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SenPillButton(
                        text = "Cancel",
                        onClick = { walletViewModel.closeUnlockSheet() },
                        isOutlined = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun StoreItemCard(
    item: StoreItem,
    isUnlocked: Boolean,
    isCurrentlyUsed: Boolean,
    onUnlockClick: () -> Unit,
    onUseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SenCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = SenRaisedSurface,
                border = BorderStroke(1.dp, SenBorderStrong)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (item.category == StoreItemCategory.STICKER) {
                        Text(text = item.preview, fontSize = 32.sp)
                    } else {
                        Text(
                            text = item.preview,
                            color = SenMint,
                            fontFamily = FontRegistry.getFontFamily(item.fontKey ?: "default"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.name,
                color = SenTextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (!isUnlocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪙", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatSen(item.costSen),
                        color = SenMint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = if (isCurrentlyUsed) "Active" else "Unlocked",
                    color = if (isCurrentlyUsed) SenBrightEmerald else SenTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            if (isUnlocked) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isCurrentlyUsed, onClick = onUseClick),
                    shape = CircleShape,
                    color = if (isCurrentlyUsed) SenBrightEmerald.copy(alpha = 0.2f) else SenAccentEmerald,
                    border = BorderStroke(1.dp, if (isCurrentlyUsed) SenBrightEmerald else SenBorderStrong)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isCurrentlyUsed) "In Use" else "Use",
                            color = if (isCurrentlyUsed) SenBrightEmerald else SenTextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onUnlockClick),
                    shape = CircleShape,
                    color = SenRaisedSurface,
                    border = BorderStroke(1.dp, SenBorderStrong)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Unlock",
                            tint = SenMint,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Unlock",
                            color = SenTextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
