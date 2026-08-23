package com.lianshan.lslife.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val user1Id: String,
    val user2Id: String,
    val targetUserId: String?,
    val targetUserNickname: String?,
    val targetUserAvatar: String?,
    val lastMessage: String?,
    val unread1: Int, // 当前登录用户 (user1) 的未读数
    val unread2: Int,
    val updatedAt: Long
)

@Dao
interface ChatSessionDao {
    @Query("SELECT COALESCE(SUM(unread1), 0) FROM chat_sessions WHERE user1Id = :currentUserId")
    fun observeTotalUnread(currentUserId: String): Flow<Int>

    @Query("SELECT * FROM chat_sessions WHERE user1Id = :currentUserId ORDER BY updatedAt DESC")
    suspend fun getAllSessions(currentUserId: String): List<ChatSessionEntity>

    @Query("UPDATE chat_sessions SET unread1 = 0 WHERE id = :sessionId AND user1Id = :currentUserId")
    suspend fun clearUnread(sessionId: String, currentUserId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessions(sessions: List<ChatSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: ChatSessionEntity)

    @Query("UPDATE chat_sessions SET lastMessage = :lastMsg, unread1 = unread1 + :unreadIncrement, updatedAt = :updatedAt WHERE id = :sessionId AND user1Id = :currentUserId")
    suspend fun updateSessionMessage(sessionId: String, currentUserId: String, lastMsg: String, unreadIncrement: Int, updatedAt: Long)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId AND user1Id = :currentUserId")
    suspend fun deleteSession(sessionId: String, currentUserId: String)
}
