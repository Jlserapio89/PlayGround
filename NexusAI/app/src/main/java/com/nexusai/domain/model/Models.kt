package com.nexusai.domain.model

import java.util.UUID

enum class Role { USER, ASSISTANT, SYSTEM }

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val model: String = "local-llama"
)

enum class AiTone(val labelRes: Int) {
    NEUTRAL(0), FRIENDLY(1), PROFESSIONAL(2), CONCISE(3), CREATIVE(4)
}

data class AiPersonality(
    val name: String = "Nexus",
    val systemPrompt: String = SYSTEM_DEFAULT,
    val tone: AiTone = AiTone.FRIENDLY,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1024
) {
    companion object {
        const val SYSTEM_DEFAULT =
            "Eres NexusAI, un asistente autónomo open source que se ejecuta localmente. " +
            "Respondes en el idioma del usuario (español por defecto). Eres útil, preciso y respetas la privacidad: " +
            "todo se procesa en el dispositivo vía Termux cuando es posible."
    }
}

data class TermuxStatus(
    val installed: Boolean = false,
    val allowExternalApps: Boolean = false,
    val apiInstalled: Boolean = false,
    val llmEndpointReachable: Boolean = false,
    val lastCheck: Long = 0L
)

data class ShizukuStatus(
    val available: Boolean = false,
    val permissionGranted: Boolean = false,
    val version: Int = 0,
    val needsUserAction: Boolean = true
)

data class AppPermissionsState(
    val storage: Boolean = false,
    val notifications: Boolean = false,
    val network: Boolean = true
)
