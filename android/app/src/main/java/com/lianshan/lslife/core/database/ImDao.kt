package com.lianshan.lslife.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImDao {
    @Query("SELECT * FROM local_messages WHERE conversationId = :convId ORDER BY createdAt ASC")
    fun getMessagesFlow(convId: String): Flow<List<LocalMessageEntity>>

    @Query("SELECT * FROM local_messages WHERE conversationId IN (:convIds) ORDER BY createdAt ASC")
    fun getMessagesFlowByConvIds(convIds: List<String>): Flow<List<LocalMessageEntity>>

    @Query("SELECT COUNT(*) FROM local_messages WHERE conversationId = :convId")
    suspend fun getMessageCount(convId: String): Int

    @Query("SELECT * FROM local_conversations ORDER BY lastMessageAt DESC")
    fun getConversationsFlow(): Flow<List<LocalConversationEntity>>

    @Query("SELECT * FROM local_conversations WHERE conversationId = :convId")
    suspend fun getConversation(convId: String): LocalConversationEntity?

    @Query("SELECT * FROM local_conversations WHERE peerId = :peerId LIMIT 1")
    suspend fun getConversationByPeerId(peerId: String): LocalConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<LocalMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: LocalMessageEntity)

    @Query("UPDATE local_messages SET sendStatus = :status WHERE msgId = :msgId")
    suspend fun updateMessageStatus(msgId: String, status: String)

    @Query("UPDATE local_messages SET sendStatus = :status, conversationId = :convId WHERE msgId = :msgId")
    suspend fun updateMessageStatusAndConvId(msgId: String, status: String, convId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: LocalConversationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversations(conversations: List<LocalConversationEntity>)
    
    @androidx.room.Transaction
    suspend fun replaceAllConversations(conversations: List<LocalConversationEntity>) {
        clearConversations()
        upsertConversations(conversations)
    }

    @Query("SELECT SUM(unreadCount) FROM local_conversations")
    fun observeTotalUnread(): Flow<Int?>
    
    @Query("UPDATE local_conversations SET unreadCount = 0 WHERE conversationId = :convId")
    suspend fun clearUnreadCount(convId: String)
    
    @Query("DELETE FROM local_conversations")
    suspend fun clearConversations()
    
    @Query("DELETE FROM local_messages")
    suspend fun clearMessages()

    @Query("DELETE FROM local_messages WHERE msgId IN (:msgIds)")
    suspend fun deleteMessages(msgIds: Set<String>)

    @Query("DELETE FROM local_conversations WHERE conversationId = :convId")
    suspend fun deleteConversation(convId: String)

    @Query("DELETE FROM local_messages WHERE conversationId = :convId")
    suspend fun deleteMessagesByConvId(convId: String)

    @Query("UPDATE local_messages SET msgType = 'RECALLED', content = :recallText WHERE msgId = :msgId")
    suspend fun recallMessage(msgId: String, recallText: String)
}
