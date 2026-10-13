package com.example.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.model.Note
import com.example.model.NoteCategory
import com.example.ui.auth.AuthViewModel
import com.example.ui.components.FloatingBottomBar
import com.example.ui.components.MainNavTab
import com.example.ui.home.HomeScreen
import com.example.ui.notes.NotesViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.store.CoinStoreScreen
import com.example.ui.store.WalletViewModel

@Composable
fun MainShellScreen(
    authViewModel: AuthViewModel,
    walletViewModel: WalletViewModel,
    notesViewModel: NotesViewModel,
    onOpenPremiumStore: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNoteEditor: (Note?) -> Unit,
    onOpenCategory: (NoteCategory) -> Unit,
    onOpenCalendar: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(MainNavTab.HOME) }
    val currentUser by authViewModel.currentUser.collectAsState()
    val shouldSwitchToCoinStore by walletViewModel.requestCoinStoreTab.collectAsState()

    androidx.compose.runtime.LaunchedEffect(shouldSwitchToCoinStore) {
        if (shouldSwitchToCoinStore) {
            currentTab = MainNavTab.COIN_STORE
            walletViewModel.clearCoinStoreTabRequest()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Tab Content with crossfade transition
        Crossfade(
            targetState = currentTab,
            animationSpec = tween(durationMillis = 250),
            label = "tab_crossfade"
        ) { tab ->
            when (tab) {
                MainNavTab.COIN_STORE -> {
                    CoinStoreScreen(
                        walletViewModel = walletViewModel
                    )
                }
                MainNavTab.HOME -> {
                    HomeScreen(
                        user = currentUser,
                        walletViewModel = walletViewModel,
                        notesViewModel = notesViewModel,
                        onOpenCoinStore = { currentTab = MainNavTab.COIN_STORE },
                        onOpenNoteEditor = onOpenNoteEditor,
                        onOpenCategory = onOpenCategory,
                        onOpenCalendar = onOpenCalendar
                    )
                }
                MainNavTab.PROFILE -> {
                    ProfileScreen(
                        user = currentUser,
                        authViewModel = authViewModel,
                        walletViewModel = walletViewModel,
                        notesViewModel = notesViewModel,
                        onNavigateToCoinStore = { currentTab = MainNavTab.COIN_STORE },
                        onNavigateToPremiumStore = onOpenPremiumStore,
                        onNavigateToHistory = onOpenHistory,
                        onNavigateToSettings = onOpenSettings,
                        onLogout = onLogout
                    )
                }
            }
        }

        // Floating Bottom Bar (3 items: Coin Store, Home, Profile)
        FloatingBottomBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
