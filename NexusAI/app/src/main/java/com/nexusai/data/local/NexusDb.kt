package com.nexusai.data.local

import androidx.room.*

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean,
    val model: String
)

@Entity(tableName = "messages", foreignKeys = [ForeignKey(
    entity = ConversationEntity::class, parentColumns = ["id"],
    childColumns = ["conversationId"], onDelete = ForeignKey.CASCADE
)], indices = [Index("conversationId")])
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val timestamp: Long
)

@Dao
interface NexusDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    suspend fun conversations(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE isFavorite=1 ORDER BY updatedAt DESC")
    suspend fun favorites(): List<ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(c: ConversationEntity)

    @Query("UPDATE conversations SET isFavorite=:fav, updatedAt=:now WHERE id=:id")
    suspend fun setFavorite(id: String, fav: Boolean, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM conversations WHERE id=:id")
    suspend fun deleteConversation(id: String)

    @Query("SELECT * FROM messages WHERE conversationId=:cid ORDER BY timestamp ASC")
    suspend fun messages(cid: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(m: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId=:cid")
    suspend fun clearMessages(cid: String)
}

@Database(entities = [ConversationEntity::class, MessageEntity::class], version = 1, exportSchema = false)
abstract class NexusDb : RoomDatabase() {
    abstract fun dao(): NexusDao
}
