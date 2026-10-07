package com.nexusai

import android.app.Application
import androidx.room.Room
import com.nexusai.data.local.NexusDb
import com.nexusai.data.local.SettingsStore
import com.nexusai.data.repo.ChatRepository
import com.nexusai.integration.llm.LlmClient
import com.nexusai.integration.termux.TermuxManager

class NexusApp : Application() {
    lateinit var db: NexusDb; lateinit var settings: SettingsStore
    lateinit var repo: ChatRepository; lateinit var termux: TermuxManager
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, NexusDb::class.java, "nexus.db").build()
        settings = SettingsStore(this)
        termux = TermuxManager(this)
        repo = ChatRepository(db.dao(), settings, LlmClient())
    }
}
