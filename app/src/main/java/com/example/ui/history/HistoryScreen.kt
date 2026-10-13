package com.example.ui.history

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SenTransaction
import com.example.model.TransactionType
import com.example.model.formatSen
import com.example.ui.components.SenCard
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.store.WalletViewModel
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBorderSubtle
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite

@Composable
fun HistoryScreen(
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by walletViewModel.transactions.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        TuftedLeatherBackground(modifier = Modifier.fillMaxSize(), isFaintHeaderOnly = true)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Column {
                    Text(
                        text = "Wallet History",
                        color = SenTextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Transactions & sen balance activity",
                        color = SenTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = SenBorderSubtle)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (transactions.isEmpty()) {
                    item {
                        SenCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "📜", fontSize = 44.sp)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No activity yet",
                                    color = SenTextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Your coin purchases, gift contributions, and unlocks will appear here.",
                                    color = SenTextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItemCard(tx = tx)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItemCard(tx: SenTransaction) {
    SenCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = SenRaisedSurface,
                    border = BorderStroke(1.dp, SenBorderStrong)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when (tx.type) {
                            TransactionType.PURCHASE -> {
                                Text(text = "🪙", fontSize = 20.sp)
                            }
                            TransactionType.UNLOCK -> {
                                Icon(
                                    imageVector = Icons.Filled.LockOpen,
                                    contentDescription = "Unlock",
                                    tint = SenBrightEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            TransactionType.GIFT -> {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Gift",
                                    tint = SenMint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = tx.title,
                        color = SenTextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${tx.description} · ${tx.formattedDate}",
                        color = SenTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            val amountColor = if (tx.amountSen > 0) SenBrightEmerald else SenTextWhite
            val amountText = if (tx.amountSen > 0) "+${formatSen(tx.amountSen)}" else formatSen(tx.amountSen)

            Text(
                text = amountText,
                color = amountColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
