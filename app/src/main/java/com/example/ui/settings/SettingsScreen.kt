package com.example.ui.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FontRegistry
import com.example.ui.components.SenCard
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.notes.NotesViewModel
import com.example.ui.store.WalletViewModel
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBorderSubtle
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenCardSurface
import com.example.ui.theme.SenDanger
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite

@Composable
fun SettingsScreen(
    walletViewModel: WalletViewModel,
    notesViewModel: NotesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    val activeFont by walletViewModel.activeFont.collectAsState()
    val unlockedItems by walletViewModel.unlockedItems.collectAsState()
    val hapticEnabled by walletViewModel.hapticEnabled.collectAsState()

    var showClearNotesDialog by remember { mutableStateOf(false) }

    val fontOptions = listOf(
        "default" to "System Default",
        "serif" to "Serif Classic",
        "mono" to "Mono Code",
        "cursive" to "Cursive Script",
        "condensed" to "Condensed Sans"
    )

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

                Text(
                    text = "Settings",
                    color = SenTextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = SenBorderSubtle)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Typography
                item {
                    Text(
                        text = "Default Note Typography",
                        color = SenTextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    SenCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            fontOptions.forEach { (key, label) ->
                                val isUnlocked = key == "default" || unlockedItems.contains("font_$key")
                                val isSelected = activeFont == key

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable(enabled = isUnlocked) {
                                            walletViewModel.useFont(key)
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) SenAccentEmerald.copy(alpha = 0.4f) else Color.Transparent,
                                    border = BorderStroke(1.dp, if (isSelected) SenBrightEmerald else Color.Transparent)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = label,
                                                color = if (isUnlocked) SenTextWhite else SenTextSecondary.copy(alpha = 0.5f),
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontFamily = FontRegistry.getFontFamily(key)
                                            )
                                            if (!isUnlocked) {
                                                Text(
                                                    text = "Unlock in Premium Store",
                                                    color = SenMint.copy(alpha = 0.7f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                tint = SenMint,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Preferences
                item {
                    Text(
                        text = "Preferences",
                        color = SenTextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    SenCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = SenRaisedSurface,
                                    border = BorderStroke(1.dp, SenBorderStrong)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Vibration,
                                            contentDescription = null,
                                            tint = SenMint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Haptic Feedback",
                                        color = SenTextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Tactile response on button taps",
                                        color = SenTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Switch(
                                checked = hapticEnabled,
                                onCheckedChange = { walletViewModel.toggleHaptic(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SenTextWhite,
                                    checkedTrackColor = SenAccentEmerald,
                                    uncheckedThumbColor = SenTextSecondary,
                                    uncheckedTrackColor = SenRaisedSurface
                                )
                            )
                        }
                    }
                }

                // Section: Storage & Maintenance
                item {
                    Text(
                        text = "Data & Storage",
                        color = SenTextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    SenCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        onClick = { showClearNotesDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = SenRaisedSurface,
                                    border = BorderStroke(1.dp, SenBorderStrong)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteSweep,
                                            contentDescription = null,
                                            tint = SenDanger,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Clear All Notes",
                                        color = SenDanger,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Erase all saved notes and checklists",
                                        color = SenTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: App Version info
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = SenTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SenCraft v1.0.0 (Premium Build)",
                            color = SenTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Clear notes confirmation dialog
        if (showClearNotesDialog) {
            AlertDialog(
                onDismissRequest = { showClearNotesDialog = false },
                containerColor = SenCardSurface,
                title = { Text("Clear All Notes?", color = SenDanger, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Are you sure you want to delete all notes and checklists? Your coins and unlocked items will remain intact.",
                        color = SenTextSecondary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        notesViewModel.clearAllNotes()
                        showClearNotesDialog = false
                    }) {
                        Text("Clear All", color = SenDanger, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearNotesDialog = false }) {
                        Text("Cancel", color = SenTextWhite)
                    }
                }
            )
        }
    }
}
