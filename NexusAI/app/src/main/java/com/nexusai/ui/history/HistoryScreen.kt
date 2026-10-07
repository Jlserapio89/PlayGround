package com.nexusai.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusai.domain.model.Conversation
import com.nexusai.ui.chat.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: ChatViewModel, onBack: () -> Unit, onOpen: (Conversation) -> Unit) {
    val history by vm.history.collectAsState()
    var onlyFav by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filtered = history.filter { (!onlyFav || it.isFavorite) && (query.isBlank() || it.title.contains(query, true)) }
    Scaffold(topBar = { TopAppBar(title = { Text("Historial") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
        actions = { IconButton(onClick = { vm.newChat() }) { Icon(Icons.Default.Add, null) } }) }) { pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Buscar…") }, leadingIcon = { Icon(Icons.Default.Search, null) })
            Row { FilterChip(onlyFav, { onlyFav = !onlyFav }, { Text("⭐ Favoritos") }) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { c ->
                    ListItem(
                        modifier = Modifier.clickable { onOpen(c); onBack() },
                        headlineContent = { Text(c.title) },
                        supportingContent = { Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(c.updatedAt))) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { vm.toggleFavorite(c.id, !c.isFavorite) }) {
                                    Icon(if (c.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, null)
                                }
                            }
                        })
                    Divider()
                }
            }
        }
    }
}
