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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoinPack
import com.example.model.GiftItem
import com.example.model.formatSen
import com.example.model.formatSenAmount
import com.example.ui.components.AnimatedCheckmarkGlow
import com.example.ui.components.CoinBurstOverlay
import com.example.ui.components.HeartSparklesBurstOverlay
import com.example.ui.components.HeroGradientStyle
import com.example.ui.components.SenCard
import com.example.ui.components.SenHeroCard
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinStoreScreen(
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val coinPacksList by walletViewModel.coinPacks.collectAsState()
    val balance by walletViewModel.balance.collectAsState()
    val animatedBalance by animateIntAsState(targetValue = balance, animationSpec = tween(600), label = "balance_anim")
    val selectedPack by walletViewModel.selectedPackForPurchase.collectAsState()
    val isPurchasing by walletViewModel.isPurchasing.collectAsState()
    val purchaseSuccess by walletViewModel.purchaseSuccess.collectAsState()
    val showCoinBurst by walletViewModel.showCoinBurst.collectAsState()

    val selectedGift by walletViewModel.selectedGift.collectAsState()
    val showGiftBurst by walletViewModel.showGiftBurst.collectAsState()

    var showGiftSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        walletViewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        TuftedLeatherBackground(modifier = Modifier.fillMaxSize(), isFaintHeaderOnly = true)

        // SINGLE TOP-LEVEL LazyColumn for the ENTIRE Coin Store layout
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Navigation Bar Item (Main Tab Header without back arrow)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Coin Store",
                        color = SenTextWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                    )
                    HorizontalDivider(color = SenBorderSubtle)
                }
            }

            // 1. Hero Intro Card (Deep Green Gradient)
            item {
                SenHeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradientStyle = HeroGradientStyle.DEEP
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Enjoying SenCraft?",
                            color = SenTextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Buy sen packs to unlock profile stickers and premium fonts. Sen are added to your account right after purchase.",
                            color = SenTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Nested Translucent Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                        ) {
                            Text(
                                text = "Use sen to unlock profile stickers and premium fonts in the Premium Store.",
                                color = SenMint,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Nested Pill Row: Balance
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CircleShape,
                            color = SenRaisedSurface,
                            border = BorderStroke(1.dp, SenBorderStrong)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🪙", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Balance",
                                        color = SenTextSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = formatSen(animatedBalance),
                                    color = SenTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. "Buy Me Something" Card (Emerald Gradient, wide pill card)
            item {
                SenHeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradientStyle = HeroGradientStyle.EMERALD,
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Gifts",
                                        tint = SenMint,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Buy Me Something",
                                    color = SenTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Spend your sen to send me a small gift",
                                    color = SenTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Pill button "Send a gift"
                        Surface(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(CircleShape)
                                .clickable { showGiftSheet = true },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Send a gift",
                                    color = SenTextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Section Title: Coin Packs
            item {
                Text(
                    text = "Sen Coin Packs",
                    color = SenTextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 4. Coin packs: Rows of 2 using coinPacks sorted by price ascending
            val pairs = coinPacksList.sortedBy { it.senAmount }.chunked(2)

            pairs.forEach { pair ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Column 1
                        Box(modifier = Modifier.weight(1f)) {
                            CoinPackCard(
                                pack = pair[0],
                                onBuyClick = { walletViewModel.openPurchaseSheet(pair[0]) }
                            )
                        }

                        // Column 2
                        if (pair.size > 1) {
                            Box(modifier = Modifier.weight(1f)) {
                                CoinPackCard(
                                    pack = pair[1],
                                    onBuyClick = { walletViewModel.openPurchaseSheet(pair[1]) }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 5. Footer: Safe & Secure Payments
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = SenMint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Safe & Secure Payments",
                        color = SenTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
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

        // Coin Burst Overlay
        if (showCoinBurst) {
            CoinBurstOverlay(onFinished = { walletViewModel.dismissCoinBurst() })
        }

        // Gift Burst Overlay
        if (showGiftBurst) {
            HeartSparklesBurstOverlay(onFinished = { walletViewModel.dismissGiftBurst() })
        }

        // Purchase Confirmation Bottom Sheet
        if (selectedPack != null) {
            val pack = selectedPack!!
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { walletViewModel.closePurchaseSheet() },
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
                        text = "Buy ${formatSen(pack.senAmount)} for ${pack.displayPrice}?",
                        color = SenTextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sen coins are immediately added to your balance for stickers and custom typography.",
                        color = SenTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    if (purchaseSuccess) {
                        AnimatedCheckmarkGlow(sizeDp = 56)
                    } else if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = SenBrightEmerald,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SenPillButton(
                                text = "Confirm Purchase (${pack.displayPrice})",
                                onClick = {
                                    walletViewModel.confirmPurchase(activity = activity) {}
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            SenPillButton(
                                text = "Cancel",
                                onClick = { walletViewModel.closePurchaseSheet() },
                                isOutlined = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Buy Me Something Gifts Bottom Sheet
        if (showGiftSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showGiftSheet = false },
                sheetState = sheetState,
                containerColor = SenCardSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Buy Me Something",
                        color = SenTextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Send a small token of gratitude using your sen coins",
                        color = SenTextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    walletViewModel.giftItems.forEach { gift ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    showGiftSheet = false
                                    walletViewModel.confirmGift(
                                        gift = gift,
                                        onInsufficientFunds = {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(3)
                                            }
                                        }
                                    )
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = SenRaisedSurface,
                            border = BorderStroke(1.dp, SenBorderStrong)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = gift.emoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = gift.name,
                                            color = SenTextWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = formatSen(gift.costSen),
                                            color = SenMint,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.height(34.dp),
                                    shape = CircleShape,
                                    color = SenAccentEmerald,
                                    border = BorderStroke(1.dp, SenBorderSubtle)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Send",
                                            color = SenTextWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

/**
 * Clean, high-performance Coin Pack Card without nested scroll locks or height glitches
 */
@Composable
private fun CoinPackCard(
    pack: CoinPack,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SenCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top-left: mint coin icon, bold amount, small "sen" label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🪙", fontSize = 18.sp)
                Text(
                    text = formatSenAmount(pack.senAmount),
                    color = SenMint,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "sen",
                    color = SenTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom-right: emerald pill price button with bold text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBuyClick),
                    shape = CircleShape,
                    color = SenAccentEmerald,
                    border = BorderStroke(1.dp, SenBorderStrong)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pack.displayPrice,
                            color = SenTextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
