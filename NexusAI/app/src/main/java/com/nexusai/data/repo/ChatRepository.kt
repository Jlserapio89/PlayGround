package com.nexusai.data.repo

import com.nexusai.data.local.*
import com.nexusai.domain.model.*
import com.nexusai.integration.llm.ChatMsg
import com.nexusai.integration.llm.LlmClient
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val dao: NexusDao,
    private val settings: SettingsStore,
    private val llm: LlmClient
) {
    suspend fun conversations() = dao.conversations().map {
        Conversation(it.id, it.title, it.createdAt, it.updatedAt, it.isFavorite, it.model)
    }
    suspend fun favorites() = dao.favorites().map {
        Conversation(it.id, it.title, it.createdAt, it.updatedAt, it.isFavorite, it.model)
    }
    suspend fun messages(cid: String) = dao.messages(cid).map {
        Message(it.id, it.conversationId, Role.valueOf(it.role), it.content, it.timestamp)
    }
    suspend fun newConversation(title: String): Conversation {
        val c = Conversation(title = title.ifBlank { "Nueva conversación" })
        dao.upsertConversation(ConversationEntity(c.id, c.title, c.createdAt, c.updatedAt, c.isFavorite, c.model))
        return c
    }
    suspend fun setFavorite(id: String, fav: Boolean) = dao.setFavorite(id, fav)
    suspend fun delete(id: String) = dao.deleteConversation(id)

    suspend fun ask(cid: String, prompt: String): Message {
        val persona = settings.personality.first()
        val endpoint = settings.llmEndpoint.first()
        dao.insertMessage(MessageEntity(java.util.UUID.randomUUID().toString(), cid, "USER", prompt, System.currentTimeMillis()))
        val hist = dao.messages(cid).dropLast(1).takeLast(20).map { ChatMsg(it.role.lowercase(), it.content) }
        val answer = try {
            llm.chat(endpoint, persona, hist, prompt)
        } catch (e: Exception) {
            // Fallback offline: responde sin servidor para que la app siga siendo útil
            "⚠️ No pude contactar al modelo local en $endpoint.\n\n" +
            "Activa Termux (Ajustes → Termux → Instalar backend) y reintenta.\nDetalle: ${e.message}\n\n" +
            "Mientras tanto: puedo ayudarte a configurar Termux/Shizuku paso a paso. ¿Quieres la guía?"
        }
        val m = Message(conversationId = cid, role = Role.ASSISTANT, content = answer)
        dao.insertMessage(MessageEntity(m.id, m.conversationId, "ASSISTANT", m.content, m.timestamp))
        return m
    }
}
