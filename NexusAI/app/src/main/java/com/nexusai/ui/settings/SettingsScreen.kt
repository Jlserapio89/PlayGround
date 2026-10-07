package com.nexusai.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.nexusai.NexusApp
import com.nexusai.data.local.PrefsKeys
import com.nexusai.domain.model.AiTone
import com.nexusai.integration.shizuku.ShizukuManager
import com.nexusai.integration.termux.TermuxManager
import com.nexusai.util.NexusNotifications
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as NexusApp
    val scope = rememberCoroutineScope()
    var termuxStatus by remember { mutableStateOf<com.nexusai.domain.model.TermuxStatus?>(null) }
    var shizukuStatus by remember { mutableStateOf(ShizukuManager.status(ctx.packageManager)) }
    var endpoint by remember { mutableStateOf("http://127.0.0.1:8080") }
    var personaName by remember { mutableStateOf("Nexus") }
    var sysPrompt by remember { mutableStateOf("") }
    var temp by remember { mutableStateOf(0.7f) }
    var dark by remember { mutableStateOf<Boolean?>(null) }
    val notifPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        val p = app.settings.all.first()
        endpoint = p[PrefsKeys.LLM_ENDPOINT] ?: endpoint
        personaName = p[PrefsKeys.PERSONA_NAME] ?: "Nexus"
        sysPrompt = p[PrefsKeys.SYSTEM_PROMPT] ?: com.nexusai.domain.model.AiPersonality.SYSTEM_DEFAULT
        temp = p[PrefsKeys.TEMPERATURE] ?: 0.7f
        termuxStatus = app.termux.checkStatus(endpoint)
    }

    fun save() = scope.launch {
        app.settings.save {
            it[PrefsKeys.LLM_ENDPOINT] = endpoint.trim()
            it[PrefsKeys.PERSONA_NAME] = personaName
            it[PrefsKeys.SYSTEM_PROMPT] = sysPrompt
            it[PrefsKeys.TEMPERATURE] = temp
        }
    }
    fun refresh(expectSuccess: Boolean = false) = scope.launch {
        termuxStatus = app.termux.checkStatus(endpoint)
        shizukuStatus = ShizukuManager.status(ctx.packageManager)
        if (termuxStatus?.llmEndpointReachable == true)
            NexusNotifications.notify(ctx, NexusNotifications.ID_TERMUX, "NexusAI ✓ Termux conectado", "Backend LLM en $endpoint responde correctamente.")
        if (shizukuStatus.permissionGranted)
            NexusNotifications.notify(ctx, NexusNotifications.ID_SHIZUKU, "NexusAI ✓ Shizuku activo", "Acceso elevado disponible.")
        else if (expectSuccess) Unit
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Configuración avanzada") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // — Termux —
            Text("Termux • backend LLM libre", style = MaterialTheme.typography.titleMedium)
            Card { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusRow("Termux instalado", termuxStatus?.installed)
                StatusRow("Termux:API instalado", termuxStatus?.apiInstalled)
                StatusRow("Modelo local alcanzable", termuxStatus?.llmEndpointReachable)
                OutlinedTextField(endpoint, { endpoint = it; save() }, label = { Text("Endpoint (llama-server / ollama)") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { app.termux.bootstrapLlm(); }) { Text("⚙️ Instalar backend") }
                    OutlinedButton(onClick = { refresh() }) { Text("Probar conexión") }
                }
                Text("Oficial: activa en Termux 'Allow external apps' y levanta `llama-server -m modelo.gguf --port 8080`. Todo offline.", style = MaterialTheme.typography.bodySmall)
            }}
            // — Shizuku —
            Text("Shizuku • acceso avanzado", style = MaterialTheme.typography.titleMedium)
            Card { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusRow("Shizuku disponible", shizukuStatus.available)
                StatusRow("Permiso concedido", shizukuStatus.permissionGranted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { ShizukuManager.requestPermission(); scope.launch { kotlinx.coroutines.delay(800); shizukuStatus = ShizukuManager.status(ctx.packageManager) } }) { Text("🔑 Solicitar permiso") }
                    OutlinedButton(onClick = { refresh() }) { Text("Actualizar estado") }
                }
                if (shizukuStatus.needsUserAction) Text("⚠️ Abre Shizuku Manager e inicia el servicio (ADB Wi-Fi o root), luego autoriza a NexusAI.", color = MaterialTheme.colorScheme.error)
            }}
            // — Permisos —
            Text("Permisos de la IA", style = MaterialTheme.typography.titleMedium)
            Card { Column(Modifier.padding(12.dp)) {
                PermRow("Almacenamiento", "Guardar / restaurar chats y modelos.", ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                PermRow("Notificaciones", "Avisos de conexión Termux/Shizuku.", if (Build.VERSION.SDK_INT >= 33) ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED else true)
                PermRow("Red local", "Hablar con el LLM en 127.0.0.1 (nunca sale a internet).", true)
                if (Build.VERSION.SDK_INT >= 33) Button(onClick = { notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Solicitar notificaciones") }
            }}
            // — Personalización —
            Text("Personalización", style = MaterialTheme.typography.titleMedium)
            Card { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(personaName, { personaName = it; save() }, label = { Text("Nombre de la IA") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(sysPrompt, { sysPrompt = it; save() }, label = { Text("System prompt") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Text("Creatividad (temperatura): ${"%.2f".format(temp)}")
                Slider(temp, { temp = it; save() }, valueRange = 0f..1.5f)
                var tone by remember { mutableStateOf(AiTone.FRIENDLY) }
                Text("Tono: ${tone.name}")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AiTone.values().forEach { t -> FilterChip(t == tone, { tone = t; scope.launch { app.settings.save { it[PrefsKeys.TONE] = t.name } } }, { Text(t.name.take(4)) }) }
                }
            }}
            Text("Modo oscuro adaptativo + ES/EN + backup en Ajustes del sistema. Arquitectura modular: cambia el .gguf en Termux sin reinstalar.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun StatusRow(label: String, ok: Boolean?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label); Text(if (ok == true) "✓" else if (ok == false) "✗" else "…",
            color = if (ok == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
    }
}
@Composable private fun PermRow(title: String, desc: String, granted: Boolean) {
    ListItem(headlineContent = { Text(title) }, supportingContent = { Text(desc) },
        trailingContent = { Text(if (granted) "✓" else "○") })
}
