package com.nexusai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusai.data.repo.ChatRepository
import com.nexusai.domain.model.Conversation
import com.nexusai.domain.model.Message
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repo: ChatRepository) : ViewModel() {
    private val _conv = MutableStateFlow<Conversation?>(null)
    private val _msgs = MutableStateFlow<List<Message>>(emptyList())
    private val _busy = MutableStateFlow(false)
    private val _history = MutableStateFlow<List<Conversation>>(emptyList())
    val conv: StateFlow<Conversation?> = _conv
    val msgs: StateFlow<List<Message>> = _msgs
    val busy: StateFlow<Boolean> = _busy
    val history: StateFlow<List<Conversation>> = _history

    fun refreshHistory() = viewModelScope.launch { _history.value = repo.conversations() }
    fun openConversation(c: Conversation) = viewModelScope.launch {
        _conv.value = c; _msgs.value = repo.messages(c.id)
    }
    fun newChat() = viewModelScope.launch {
        val c = repo.newConversation("Chat ${history.value.size + 1}")
        _conv.value = c; _msgs.value = emptyList(); refreshHistory()
    }
    fun toggleFavorite(id: String, fav: Boolean) = viewModelScope.launch {
        repo.setFavorite(id, fav); refreshHistory()
        _conv.value = _conv.value?.copy(isFavorite = fav)
    }
    fun send(prompt: String) = viewModelScope.launch {
        var c = _conv.value ?: repo.newConversation(prompt.take(40)).also { _conv.value = it; refreshHistory() }
        _busy.value = true
        try { repo.ask(c.id, prompt); _msgs.value = repo.messages(c.id); refreshHistory() }
        finally { _busy.value = false }
    }
    init { refreshHistory() }
}
