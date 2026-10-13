package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.BillingRepository
import com.example.data.DataStoreManager
import com.example.data.DefaultNotesRepository
import com.example.data.DefaultAuthRepository
import com.example.data.RealBillingRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.navigation.RootNavHost
import com.example.ui.notes.NotesViewModel
import com.example.ui.store.WalletViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SenBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dataStoreManager = DataStoreManager(applicationContext)
        val authRepository = DefaultAuthRepository(dataStoreManager)
        val billingRepository: BillingRepository = RealBillingRepository(applicationContext, dataStoreManager)
        val notesRepository = DefaultNotesRepository(dataStoreManager)

        val authViewModel = AuthViewModel(authRepository)
        val walletViewModel = WalletViewModel(billingRepository)
        val notesViewModel = NotesViewModel(notesRepository)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SenBackground
                ) {
                    RootNavHost(
                        authViewModel = authViewModel,
                        walletViewModel = walletViewModel,
                        notesViewModel = notesViewModel
                    )
                }
            }
        }
    }
}
