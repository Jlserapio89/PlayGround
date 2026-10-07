package com.nexusai.integration.llm

import com.nexusai.domain.model.AiPersonality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Serializable data class ChatMsg(val role: String, val content: String)
@Serializable data class ChatReq(val model: String = "local", val messages: List<ChatMsg>, val temperature: Float = 0.7f, val max_tokens: Int = 1024, val stream: Boolean = false)

/**
 * Cliente OpenAI-compatible contra el servidor local en Termux.
 * Compatible con `llama-server` y `ollama serve` (ambos exponen /v1/chat/completions).
 * 100% offline y libre: ningún dato sale del dispositivo.
 */
class LlmClient @Inject constructor() {
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS).build()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun chat(endpoint: String, persona: AiPersonality, history: List<ChatMsg>, userPrompt: String): String =
        withContext(Dispatchers.IO) {
            val url = endpoint.trimEnd('/') + "/v1/chat/completions"
            val toneHint = when (persona.tone.name) {
                "CONCISE" -> " Responde de forma breve y directa."
                "PROFESSIONAL" -> " Usa un tono profesional."
                "CREATIVE" -> " Sé creativo y expresivo."
                "FRIENDLY" -> " Usa un tono amable y cercano."
                else -> ""
            }
            val req = ChatReq(
                messages = listOf(ChatMsg("system", persona.systemPrompt + toneHint)) + history + ChatMsg("user", userPrompt),
                temperature = persona.temperature, max_tokens = persona.maxTokens
            )
            val body = json.encodeToString(ChatReq.serializer(), req)
                .toRequestBody("application/json".toMediaType())
            val call = Request.Builder().url(url).post(body).build()
            http.newCall(call).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) throw IllegalStateException("LLM ${resp.code}: $raw")
                val obj = json.parseToJsonElement(raw).jsonObject
                obj["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                    ?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content
                    ?: obj["content"]?.jsonPrimitive?.contentOrNull
                    ?: throw IllegalStateException("Respuesta LLM inesperada: $raw")
            }
        }
}
