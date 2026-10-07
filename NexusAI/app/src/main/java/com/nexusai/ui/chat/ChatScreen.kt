package com.nexusai.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.nexusai.domain.model.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: ChatViewModel, onOpenSettings: () -> Unit, onOpenHistory: () -> Unit) {
    val msgs by vm.msgs.collectAsState()
    val busy by vm.busy.collectAsState()
    val conv by vm.conv.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(msgs.size) { if (msgs.isNotEmpty()) listState.animateScrollToItem(msgs.size - 1) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(conv?.title ?: "NexusAI") },
            navigationIcon = { IconButton(onClick = onOpenHistory) { Icon(Icons.Default.History, null) } },
            actions = {
                IconButton(onClick = { conv?.let { vm.toggleFavorite(it.id, !it.isFavorite) } }) {
                    Icon(if (conv?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder, null)
                }
                IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, null) }
            }) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = input, onValueChange = { input = it },
                        modifier = Modifier.weight(1f), placeholder = { Text("Pregunta a NexusAI…") }, maxLines = 4)
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(onClick = { if (input.isNotBlank() && !busy) { vm.send(input.trim()); input = "" } }, enabled = !busy) {
                        if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Send, null)
                    }
                }
            }
        }
    ) { pad ->
        if (msgs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👋", style = MaterialTheme.typography.displayLarge)
                    Text("¿En qué te ayudo hoy?", style = MaterialTheme.typography.headlineSmall)
                    Text("Corre 100% local vía Termux • Libre y privado", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = { vm.send("Configúrame Termux paso a paso") }, label = { Text("⚙️ Configurar Termux") })
                        SuggestionChip(onClick = { vm.send("¿Qué puede hacer Shizuku?") }, label = { Text("🔑 Shizuku") })
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pad).padding(horizontal = 12.dp), state = listState, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                items(msgs, key = { it.id }) { m ->
                    val isUser = m.role == Role.USER
                    Card(
                        modifier = Modifier.fillMaxWidth(if (isUser) 1f else 1f),
                        colors = CardDefaults.cardColors(containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(if (isUser) "Tú" else "NexusAI", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            if (isUser) Text(m.content) else RichText { Markdown(m.content) }
                        }
                    }
                }
                if (busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }
        }
    }
}
