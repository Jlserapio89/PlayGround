package com.nexusai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.*
import com.nexusai.ui.chat.ChatScreen
import com.nexusai.ui.chat.ChatViewModel
import com.nexusai.ui.history.HistoryScreen
import com.nexusai.ui.settings.SettingsScreen
import com.nexusai.ui.theme.NexusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NexusApp
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(c: Class<T>): T = ChatViewModel(app.repo) as T
        }
        val vm: ChatViewModel by viewModels { factory }
        setContent {
            val dark = isSystemInDarkTheme()
            NexusTheme(dark = dark) {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "chat") {
                    composable("chat") { ChatScreen(vm, onOpenSettings = { nav.navigate("settings") }, onOpenHistory = { nav.navigate("history") }) }
                    composable("history") { HistoryScreen(vm, onBack = { nav.popBackStack() }, onOpen = { vm.openConversation(it) }) }
                    composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
                }
                LaunchedEffect(Unit) {
                    intent?.data?.toString()?.let { if (it.startsWith("nexusai://chat")) vm.newChat() }
                }
            }
        }
    }
}
