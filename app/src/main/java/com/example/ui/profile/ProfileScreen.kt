package com.example.ui.profile

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NoteCategory
import com.example.model.User
import com.example.model.formatSenAmount
import com.example.ui.auth.AuthViewModel
import com.example.ui.components.HeroGradientStyle
import com.example.ui.components.SenCard
import com.example.ui.components.SenCraftLogo
import com.example.ui.components.SenHeroCard
import com.example.ui.components.SenPillButton
import com.example.ui.components.TuftedLeatherBackground
import com.example.ui.notes.NotesViewModel
import com.example.ui.store.WalletViewModel
import com.example.ui.theme.SenAccentEmerald
import com.example.ui.theme.SenBorderStrong
import com.example.ui.theme.SenBorderSubtle
import com.example.ui.theme.SenBrightEmerald
import com.example.ui.theme.SenCardSurface
import com.example.ui.theme.SenDanger
import com.example.ui.theme.SenFieldFill
import com.example.ui.theme.SenMint
import com.example.ui.theme.SenRaisedSurface
import com.example.ui.theme.SenTextSecondary
import com.example.ui.theme.SenTextWhite
import com.example.util.AppLinks
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    authViewModel: AuthViewModel,
    walletViewModel: WalletViewModel,
    notesViewModel: NotesViewModel,
    onNavigateToCoinStore: () -> Unit,
    onNavigateToPremiumStore: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val balance by walletViewModel.balance.collectAsState()
    val activeSticker by walletViewModel.activeSticker.collectAsState()
    val unlockedItems by walletViewModel.unlockedItems.collectAsState()
    val allNotes by notesViewModel.allNotes.collectAsState()

    val normalCount = allNotes.count { it.category == NoteCategory.NORMAL }
    val shoppingCount = allNotes.count { it.category == NoteCategory.SHOPPING }
    val schoolCount = allNotes.count { it.category == NoteCategory.SCHOOL }

    // Dialog & Sheet States
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(user?.name ?: "") }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier.fillMaxSize()) {
        TuftedLeatherBackground(modifier = Modifier.fillMaxSize(), isFaintHeaderOnly = true)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header: Title with thin divider
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Profile",
                        color = SenTextWhite,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = SenBorderSubtle)
                }
            }

            // 1. Header Hero Card (Deep Green Gradient)
            item {
                SenHeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradientStyle = HeroGradientStyle.DEEP
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Avatar with camera badge
                            Box(modifier = Modifier.size(72.dp)) {
                                Surface(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                    shape = RoundedCornerShape(20.dp),
                                    color = SenRaisedSurface,
                                    border = BorderStroke(1.5.dp, SenBorderStrong)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = activeSticker,
                                            fontSize = 32.sp
                                        )
                                    }
                                }

                                // Camera badge to pick sticker
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .clickable { showStickerSheet = true },
                                    shape = CircleShape,
                                    color = SenBrightEmerald,
                                    border = BorderStroke(1.5.dp, SenCardSurface)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.CameraAlt,
                                            contentDescription = "Change avatar",
                                            tint = Color(0xFF050B08),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // Share & Logout Action Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { shareApp(context) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "Share",
                                        tint = SenTextWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { authViewModel.logout(onLogout) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Logout,
                                        contentDescription = "Log out",
                                        tint = SenTextWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Name
                        Text(
                            text = user?.name ?: "Sen Scholar",
                            color = SenTextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Email
                        Text(
                            text = user?.email ?: "scholar@sencraft.luxury",
                            color = SenTextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Joined date
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = SenMint,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Joined ${user?.joinedDate ?: "October 2026"}",
                                color = SenTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dark pill "Edit profile" button
                        Surface(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    editNameInput = user?.name ?: ""
                                    showEditNameDialog = true
                                },
                            shape = CircleShape,
                            color = SenRaisedSurface,
                            border = BorderStroke(1.dp, SenBorderStrong)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Edit profile",
                                    color = SenTextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Balance Card (Emerald Gradient)
            item {
                SenHeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradientStyle = HeroGradientStyle.EMERALD
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🪙", fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "YOUR SEN",
                                    color = SenMint,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = formatSenAmount(balance),
                                color = SenTextWhite,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Circular History button
                            Surface(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onNavigateToHistory),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = "History",
                                        tint = SenTextWhite,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Pill "+ Buy More" button
                            Surface(
                                modifier = Modifier
                                    .height(46.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onNavigateToCoinStore),
                                shape = CircleShape,
                                color = SenBrightEmerald,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+ Buy More",
                                        color = Color(0xFF050B08),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Three Stat Chips: Notes, Shopping, School
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChip(count = normalCount, label = "Notes", emoji = "📝", modifier = Modifier.weight(1f))
                    StatChip(count = shoppingCount, label = "Shopping", emoji = "🛒", modifier = Modifier.weight(1f))
                    StatChip(count = schoolCount, label = "School", emoji = "🎓", modifier = Modifier.weight(1f))
                }
            }

            // 4. List Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileMenuCard(
                        icon = Icons.Filled.ShoppingBag,
                        title = "Premium Store",
                        subtitle = "Unlock stickers & typography fonts",
                        onClick = onNavigateToPremiumStore
                    )
                    ProfileMenuCard(
                        icon = Icons.Filled.Paid,
                        title = "Coin Store",
                        subtitle = "Purchase sen coin packages",
                        onClick = onNavigateToCoinStore
                    )
                    ProfileMenuCard(
                        icon = Icons.Filled.Settings,
                        title = "Settings",
                        subtitle = "Font styles & app preferences",
                        onClick = onNavigateToSettings
                    )
                    ProfileMenuCard(
                        icon = Icons.Filled.Share,
                        title = "Share App",
                        subtitle = "Invite companions to SenCraft",
                        onClick = { shareApp(context) }
                    )
                    ProfileMenuCard(
                        icon = Icons.Filled.PrivacyTip,
                        title = "Privacy Policy",
                        subtitle = "Read data usage & privacy terms",
                        onClick = {
                            AppLinks.openUrl(context, AppLinks.PRIVACY_POLICY_URL) {
                                scope.launch { snackbarHostState.showSnackbar("No app found to open this link") }
                            }
                        }
                    )
                    ProfileMenuCard(
                        icon = Icons.Filled.Info,
                        title = "About Developer",
                        subtitle = "Craftsmanship & version credits",
                        onClick = { showAboutSheet = true }
                    )
                }
            }

            // 5. Destructive Account Actions
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Red "Log out" text button
                    TextButton(
                        onClick = { authViewModel.logout(onLogout) }
                    ) {
                        Text(
                            text = "Log out",
                            color = SenDanger,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Red outlined pill "Delete account"
                    SenPillButton(
                        text = "Delete account",
                        onClick = { showDeleteAccountDialog = true },
                        isOutlined = true,
                        isDanger = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Small text link "Request deletion online"
                    Text(
                        text = "Request deletion online",
                        color = SenMint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            AppLinks.openUrl(context, AppLinks.ACCOUNT_DELETION_URL) {
                                scope.launch { snackbarHostState.showSnackbar("No app found to open this link") }
                            }
                        }
                    )
                }
            }
        }

        // Edit Profile Dialog
        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                containerColor = SenCardSurface,
                title = { Text("Edit Profile Name", color = SenTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Your name", color = SenTextSecondary) },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SenTextWhite,
                            unfocusedTextColor = SenTextWhite,
                            focusedContainerColor = SenFieldFill,
                            unfocusedContainerColor = SenFieldFill,
                            focusedBorderColor = SenBrightEmerald,
                            unfocusedBorderColor = SenBorderStrong
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (editNameInput.isNotBlank()) {
                            authViewModel.updateName(editNameInput.trim())
                        }
                        showEditNameDialog = false
                    }) {
                        Text("Save", color = SenBrightEmerald, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text("Cancel", color = SenTextSecondary)
                    }
                }
            )
        }

        // Delete Account Confirmation Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                containerColor = SenCardSurface,
                title = { Text("Delete account?", color = SenDanger, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Delete account? This permanently deletes your account, notes, sen balance, unlocked items, history and settings from this device. Your sen balance will be lost. This cannot be undone.",
                        color = SenTextSecondary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteAccountDialog = false
                        authViewModel.deleteAccount(onLogout)
                    }) {
                        Text("Delete", color = SenDanger, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountDialog = false }) {
                        Text("Cancel", color = SenTextWhite)
                    }
                }
            )
        }

        // Sticker Picker Bottom Sheet
        if (showStickerSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showStickerSheet = false },
                sheetState = sheetState,
                containerColor = SenCardSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Choose Profile Sticker",
                        color = SenTextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Equip unlocked stickers as your avatar icon",
                        color = SenTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    val allStickers = listOf(
                        "default_notebook" to "📓",
                        "sticker_fox" to "🦊",
                        "sticker_panda" to "🐼",
                        "sticker_rocket" to "🚀",
                        "sticker_moon" to "🌙",
                        "sticker_crown" to "👑",
                        "sticker_dragon" to "🐉"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        allStickers.forEach { (id, emoji) ->
                            val isUnlocked = unlockedItems.contains(id)
                            val isEquipped = activeSticker == emoji

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(enabled = isUnlocked) {
                                        walletViewModel.useSticker(emoji)
                                        showStickerSheet = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isEquipped) SenAccentEmerald else SenRaisedSurface,
                                    border = BorderStroke(1.dp, if (isEquipped) SenBrightEmerald else SenBorderStrong)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isUnlocked) {
                                            Text(text = emoji, fontSize = 28.sp)
                                        } else {
                                            Text(text = "🔒", fontSize = 20.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isEquipped) "Used" else if (isUnlocked) "Wear" else "Store",
                                    color = if (isEquipped) SenMint else SenTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // About Developer Bottom Sheet
        if (showAboutSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showAboutSheet = false },
                sheetState = sheetState,
                containerColor = SenCardSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SenCraftLogo(size = 72.dp, showGlow = true)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "SenCraft",
                        color = SenTextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Version 1.0.0",
                        color = SenMint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Developed by MD MUHAIMIN HOSSEN",
                        color = SenTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SenPillButton(
                        text = "Facebook",
                        onClick = {
                            AppLinks.openUrl(context, AppLinks.DEVELOPER_FACEBOOK_URL) {
                                scope.launch { snackbarHostState.showSnackbar("No app found to open this link") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }
}

@Composable
private fun StatChip(
    count: Int,
    label: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    SenCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                color = SenTextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = SenTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ProfileMenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null,
    isEnabled: Boolean = true
) {
    SenCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = if (isEnabled) onClick else null
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
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = SenRaisedSurface,
                    border = BorderStroke(1.dp, SenBorderStrong)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isEnabled) SenMint else SenTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = if (isEnabled) SenTextWhite else SenTextSecondary.copy(alpha = 0.7f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (badge != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = SenRaisedSurface,
                                border = BorderStroke(1.dp, SenBorderSubtle)
                            ) {
                                Text(
                                    text = badge,
                                    color = SenMint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = SenTextSecondary.copy(alpha = if (isEnabled) 0.8f else 0.4f),
                        fontSize = 12.sp
                    )
                }
            }

            if (isEnabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Navigate",
                    tint = SenTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun shareApp(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Experience SenCraft: A luxury dark green sanctuary for notes, shopping lists, and thoughts. Download today!")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share SenCraft"))
}
