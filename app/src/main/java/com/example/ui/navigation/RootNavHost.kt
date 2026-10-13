package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.NoteCategory
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.SignUpScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.notes.CalendarViewScreen
import com.example.ui.notes.CategoryNotesScreen
import com.example.ui.notes.NoteEditorScreen
import com.example.ui.notes.NotesViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.store.PremiumStoreScreen
import com.example.ui.store.WalletViewModel

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val MAIN = "main"
    const val PREMIUM_STORE = "premium_store"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val CATEGORY_NOTES = "category_notes/{categoryId}"
    const val NOTE_EDITOR = "note_editor"
    const val CALENDAR_VIEW = "calendar_view"

    fun categoryNotes(cat: NoteCategory) = "category_notes/${cat.id}"
}

@Composable
fun RootNavHost(
    authViewModel: AuthViewModel,
    walletViewModel: WalletViewModel,
    notesViewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier.fillMaxSize(),
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(300)) }
    ) {
        // 1. Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    val nextRoute = if (currentUser?.isLoggedIn == true) Routes.MAIN else Routes.LOGIN
                    navController.navigate(nextRoute) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Login Screen
        composable(
            route = Routes.LOGIN,
            exitTransition = {
                // Login content fades and slides down (500ms)
                fadeOut(tween(500)) + slideOutVertically(tween(500, easing = FastOutSlowInEasing)) { 100 }
            }
        ) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) },
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 3. Sign Up Screen
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 5. Main Shell Screen (Main tabs)
        composable(
            route = Routes.MAIN,
            enterTransition = {
                // Main fades and scales in (500ms)
                fadeIn(tween(500)) + scaleIn(tween(500, easing = FastOutSlowInEasing), initialScale = 0.94f)
            }
        ) {
            MainShellScreen(
                authViewModel = authViewModel,
                walletViewModel = walletViewModel,
                notesViewModel = notesViewModel,
                onOpenPremiumStore = { navController.navigate(Routes.PREMIUM_STORE) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenNoteEditor = { navController.navigate(Routes.NOTE_EDITOR) },
                onOpenCategory = { cat -> navController.navigate(Routes.categoryNotes(cat)) },
                onOpenCalendar = { navController.navigate(Routes.CALENDAR_VIEW) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 6. Premium Store Screen
        composable(
            route = Routes.PREMIUM_STORE,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350))
            }
        ) {
            PremiumStoreScreen(
                walletViewModel = walletViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCoinStore = {
                    walletViewModel.openCoinStoreTab()
                    navController.popBackStack()
                }
            )
        }

        // 7. Settings Screen (Accessed from Profile)
        composable(
            route = Routes.SETTINGS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350))
            }
        ) {
            SettingsScreen(
                walletViewModel = walletViewModel,
                notesViewModel = notesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. History Screen
        composable(
            route = Routes.HISTORY,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350))
            }
        ) {
            HistoryScreen(
                walletViewModel = walletViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. Category Notes Screen
        composable(
            route = Routes.CATEGORY_NOTES,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350))
            }
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("categoryId") ?: "normal"
            val category = NoteCategory.entries.find { it.id == catId } ?: NoteCategory.NORMAL
            CategoryNotesScreen(
                category = category,
                notesViewModel = notesViewModel,
                walletViewModel = walletViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenNoteEditor = { navController.navigate(Routes.NOTE_EDITOR) }
            )
        }

        // 10. Note Editor Screen
        composable(
            route = Routes.NOTE_EDITOR,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350))
            }
        ) {
            NoteEditorScreen(
                notesViewModel = notesViewModel,
                walletViewModel = walletViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Calendar View Screen
        composable(
            route = Routes.CALENDAR_VIEW,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350))
            }
        ) {
            CalendarViewScreen(
                notesViewModel = notesViewModel,
                walletViewModel = walletViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenNoteEditor = { navController.navigate(Routes.NOTE_EDITOR) }
            )
        }
    }
}
