package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.SmsViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConversationListScreen
import com.example.ui.screens.FilterManagementScreen
import com.example.ui.screens.NotificationSettingsScreen
import com.example.ui.screens.SimSettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: SmsViewModel = viewModel()

                // Register Permissions launcher
                val permissionsToRequest = buildList {
                    add(Manifest.permission.RECEIVE_SMS)
                    add(Manifest.permission.SEND_SMS)
                    add(Manifest.permission.READ_SMS)
                    add(Manifest.permission.READ_PHONE_STATE)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }.toTypedArray()

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.refreshStatus()
                }

                LaunchedEffect(Unit) {
                    viewModel.refreshStatus()
                    handleIntent(intent, viewModel)
                }

                DualSimSmsApp(
                    viewModel = viewModel,
                    onRequestPermissions = {
                        permissionLauncher.launch(permissionsToRequest)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIntent(intent: Intent?, viewModel: SmsViewModel) {
        if (intent == null) return

        // Check if opened from notification
        val notifSender = intent.getStringExtra("conversation_sender")
        if (!notifSender.isNullOrBlank()) {
            viewModel.openConversation(notifSender)
            return
        }

        // Check if opened via SENDTO / SMS intent
        val scheme = intent.data?.scheme
        if (scheme == "sms" || scheme == "smsto") {
            val recipient = intent.data?.schemeSpecificPart?.substringBefore("?")
            if (!recipient.isNullOrBlank()) {
                viewModel.openConversation(recipient)
            }
        }
    }
}

@Composable
fun DualSimSmsApp(
    viewModel: SmsViewModel,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeSender by viewModel.activeConversationSender.collectAsState()

    BackHandler(enabled = currentScreen != AppScreen.CONVERSATIONS) {
        if (currentScreen == AppScreen.CHAT) {
            viewModel.closeConversation()
        } else {
            viewModel.navigateTo(AppScreen.CONVERSATIONS)
        }
    }

    when (currentScreen) {
        AppScreen.CONVERSATIONS -> {
            ConversationListScreen(
                viewModel = viewModel,
                onRequestPermissions = onRequestPermissions,
                modifier = modifier.fillMaxSize()
            )
        }

        AppScreen.CHAT -> {
            ChatScreen(
                viewModel = viewModel,
                sender = activeSender ?: "Contact",
                onBack = { viewModel.closeConversation() },
                modifier = modifier.fillMaxSize()
            )
        }

        AppScreen.FILTER_RULES -> {
            FilterManagementScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppScreen.CONVERSATIONS) },
                modifier = modifier.fillMaxSize()
            )
        }

        AppScreen.NOTIFICATIONS -> {
            NotificationSettingsScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppScreen.CONVERSATIONS) },
                modifier = modifier.fillMaxSize()
            )
        }

        AppScreen.SIM_SETTINGS -> {
            SimSettingsScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppScreen.CONVERSATIONS) },
                modifier = modifier.fillMaxSize()
            )
        }
    }
}
