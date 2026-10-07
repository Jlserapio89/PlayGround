package com.nexusai.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nexusai.domain.model.AiPersonality
import com.nexusai.domain.model.AiTone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("nexus_settings")

object PrefsKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode_v2") // true=oscuro, null=sistema
    val USE_SYSTEM_THEME = booleanPreferencesKey("use_system_theme")
    val PERSONA_NAME = stringPreferencesKey("persona_name")
    val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
    val TONE = stringPreferencesKey("tone")
    val TEMPERATURE = floatPreferencesKey("temperature")
    val MAX_TOKENS = intPreferencesKey("max_tokens")
    val LLM_ENDPOINT = stringPreferencesKey("llm_endpoint") // ej http://127.0.0.1:8080
    val TERMUX_ENABLED = booleanPreferencesKey("termux_enabled")
    val LOCALE = stringPreferencesKey("locale") // es, en, system
}

class SettingsStore(private val ctx: Context) {
    val personality: Flow<AiPersonality> = ctx.ds.data.map { p ->
        AiPersonality(
            name = p[PrefsKeys.PERSONA_NAME] ?: "Nexus",
            systemPrompt = p[PrefsKeys.SYSTEM_PROMPT] ?: AiPersonality.SYSTEM_DEFAULT,
            tone = runCatching { AiTone.valueOf(p[PrefsKeys.TONE] ?: "FRIENDLY") }.getOrDefault(AiTone.FRIENDLY),
            temperature = p[PrefsKeys.TEMPERATURE] ?: 0.7f,
            maxTokens = p[PrefsKeys.MAX_TOKENS] ?: 1024
        )
    }
    val llmEndpoint: Flow<String> = ctx.ds.data.map { it[PrefsKeys.LLM_ENDPOINT] ?: "http://127.0.0.1:8080" }
    val termuxEnabled: Flow<Boolean> = ctx.ds.data.map { it[PrefsKeys.TERMUX_ENABLED] ?: true }
    val all = ctx.ds.data

    suspend fun save(block: suspend (MutablePreferences) -> Unit) {
        ctx.ds.edit(block)
    }
}
