package com.lianshan.lslife.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. 本地消息实体
@Entity(tableName = "local_messages")
data class LocalMessageEntity(
    @PrimaryKey val msgId: String,
    val conversationId: String,
    val senderId: String,
    val msgType: String, // TEXT, IMAGE, PRODUCT_CARD
    val content: String, // 解密后的展示文本或JSON
    val createdAt: Long,
    val sendStatus: String // SENDING, SENT, FAILED
)

// 2. 本地会话实体
@Entity(tableName = "local_conversations")
data class LocalConversationEntity(
    @PrimaryKey val conversationId: String,
    val peerId: String,
    val peerName: String,
    val peerAvatar: String?,
    val targetId: String?,
    val lastMessage: String,
    val lastMessageAt: Long,
    val unreadCount: Int
)
