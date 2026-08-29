package com.qingyuan.lslife.core.data

import android.content.Context
import com.qingyuan.lslife.core.database.ImDao
import com.qingyuan.lslife.core.database.LocalConversationEntity
import com.qingyuan.lslife.core.database.LocalMessageEntity
import com.qingyuan.lslife.core.network.ApiService
import com.qingyuan.lslife.core.network.RealtimeClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImRepository @Inject constructor(
    private val api: ApiService,
    private val realtimeClient: RealtimeClient,
    private val imDao: ImDao,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("chat_repo_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    val totalUnreadCount: Flow<Int> = imDao.observeTotalUnread().map { it ?: 0 }

    private val _incomingMessages = kotlinx.coroutines.flow.MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val incomingMessages: kotlinx.coroutines.flow.SharedFlow<String> = _incomingMessages

    private val _ackEvents = kotlinx.coroutines.flow.MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val ackEvents: kotlinx.coroutines.flow.SharedFlow<String> = _ackEvents

    @Volatile
    var activeChatSessionId: String? = null

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initRepository() {
        realtimeClient.connect()
        repositoryScope.launch {
            syncConversationsQuietly()
        }
    }

    init {
        // Start observing WebSocket events and map them directly into Room SSOT
        repositoryScope.launch {
            realtimeClient.events.collect { text ->
                try {
                    val obj = json.parseToJsonElement(text).jsonObject
                    val eventType = obj["event"]?.jsonPrimitive?.content ?: obj["type"]?.jsonPrimitive?.content
                    when (eventType) {
                        "FORCE_LOGOUT" -> {
                            authRepository.logout()
                        }
                        "DATA_REFRESH_REQUIRED" -> {
                            _incomingMessages.emit(obj.toString())
                        }
                        "USER_STATE_CHANGED" -> {
                            // Immediately refresh the current user state via API
                            authRepository.me()
                        }
                        "chat_message" -> {
                            val msgObj = obj["message"]?.jsonObject ?: return@collect
                            val msgId = msgObj["id"]?.jsonPrimitive?.content ?: return@collect
                            val convId = msgObj["sessionId"]?.jsonPrimitive?.content ?: return@collect
                            val senderId = msgObj["senderId"]?.jsonPrimitive?.content ?: return@collect
                            val msgType = msgObj["type"]?.jsonPrimitive?.content ?: "TEXT"
                            val content = msgObj["content"]?.jsonPrimitive?.content ?: ""
                            val createdAtStr = msgObj["createdAt"]?.jsonPrimitive?.content
                            val createdAt = parseDateToLong(createdAtStr)

                            imDao.upsertMessage(
                                LocalMessageEntity(
                                    msgId = msgId,
                                    conversationId = convId,
                                    senderId = senderId,
                                    msgType = msgType,
                                    content = content,
                                    createdAt = createdAt,
                                    sendStatus = "SENT"
                                )
                            )
                            syncConversationsQuietly() // Refresh conversation lists
                            _incomingMessages.emit(msgObj.toString())
                        }
                        "message_recalled" -> {
                            val msgId = obj["messageId"]?.jsonPrimitive?.content ?: return@collect
                            imDao.recallMessage(msgId, "[已撤回一条消息]")
                            syncConversationsQuietly()
                        }
                        "MSG_ACK" -> {
                            val clientMsgId = obj["clientMsgId"]?.jsonPrimitive?.content ?: return@collect
                            val status = obj["status"]?.jsonPrimitive?.content ?: "SENT"
                            val convId = obj["conversationId"]?.jsonPrimitive?.content
                            if (convId != null) {
                                imDao.updateMessageStatusAndConvId(clientMsgId, status, convId)
                            } else {
                                imDao.updateMessageStatus(clientMsgId, status)
                            }
                            syncConversationsQuietly()
                            _ackEvents.emit(obj.toString())
                        }
                        "offline_sync" -> {
                            val convId = obj["conversationId"]?.jsonPrimitive?.content ?: return@collect
                            val msgsArray = obj["messages"]?.jsonArray
                            val entities = msgsArray?.mapNotNull { el ->
                                try {
                                    val mo = el.jsonObject
                                    LocalMessageEntity(
                                        msgId = mo["id"]?.jsonPrimitive?.content ?: UUID.randomUUID().toString(),
                                        conversationId = convId,
                                        senderId = mo["senderId"]?.jsonPrimitive?.content ?: "",
                                        msgType = mo["msgType"]?.jsonPrimitive?.content ?: "TEXT",
                                        content = mo["content"]?.jsonPrimitive?.content ?: "",
                                        createdAt = parseDateToLong(mo["createdAt"]?.jsonPrimitive?.content),
                                        sendStatus = "SENT"
                                    )
                                } catch (e: Exception) { null }
                            }
                            if (!entities.isNullOrEmpty()) {
                                imDao.upsertMessages(entities)
                                syncConversationsQuietly()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private var syncJob: kotlinx.coroutines.Job? = null

    fun syncConversationsQuietly() {
        syncJob?.cancel()
        syncJob = repositoryScope.launch {
            try {
                val response = api.chatSessions()
                if (response.code == 0 && response.data != null) {
                    val sessions = response.data
                    val entities = sessions.map { session ->
                        val lastMsgAt = parseDateToLong(session.updatedAt)
                        LocalConversationEntity(
                            conversationId = session.id,
                            peerId = session.targetUser?.id ?: "",
                            peerName = session.targetUser?.nickname ?: "未知用户",
                            peerAvatar = session.targetUser?.avatar,
                            targetId = null,
                            lastMessage = session.lastMessage ?: "",
                            lastMessageAt = lastMsgAt,
                            unreadCount = session.unread
                        )
                    }
                    
                    imDao.replaceAllConversations(entities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun sendMessage(convId: String, toUserId: String, targetId: String?, content: String, type: String = "TEXT", mediaHash: String? = null) {
        val clientMsgId = UUID.randomUUID().toString()
        
        // Optimistic write
        val currentUser = authRepository.cachedMe()
        val senderId = currentUser?.id ?: ""
        imDao.upsertMessage(
            LocalMessageEntity(
                msgId = clientMsgId,
                conversationId = convId.ifEmpty { "new" },
                senderId = senderId,
                msgType = type,
                content = content,
                createdAt = System.currentTimeMillis(),
                sendStatus = "SENDING"
            )
        )
        // Removed redundant syncConversationsQuietly() to prevent race conditions with MSG_ACK
        
        realtimeClient.sendChatMessage(
            clientMsgId = clientMsgId,
            toUserId = toUserId,
            targetId = targetId,
            content = content,
            type = type,
            mediaHash = mediaHash
        )
    }

    fun recallMessage(messageId: String) {
        realtimeClient.sendRecallMessage(messageId)
    }

    fun syncOffline(conversationId: String) {
        realtimeClient.sendOfflineSync(conversationId)
    }

    /**
     * 通过 REST API 直接拉取服务端消息并写入 Room。
     * 这是打开对话框时的主要消息加载路径，比 WebSocket sync_offline 快得多。
     */
    suspend fun syncMessagesFromRest(conversationId: String) {
        try {
            val response = api.chatMessages(conversationId)
            if (response.code == 0 && response.data != null) {
                val entities = response.data.map { msg ->
                    LocalMessageEntity(
                        msgId = msg.id,
                        conversationId = conversationId,
                        senderId = msg.senderId,
                        msgType = msg.type,
                        content = msg.content,
                        createdAt = parseDateToLong(msg.createdAt),
                        sendStatus = "SENT"
                    )
                }
                if (entities.isNotEmpty()) {
                    imDao.upsertMessages(entities)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteConversation(conversationId: String) {
        repositoryScope.launch {
            // 1. 本地数据库彻底删除该会话记录及所有聊天记录
            imDao.deleteConversation(conversationId)
            imDao.deleteMessagesByConvId(conversationId)
            
            // 2. 服务端数据库同步级联物理删除
            try {
                api.deleteChatSession(conversationId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            syncConversationsQuietly()
        }
    }

    fun hideConversation(conversationId: String) {
        deleteConversation(conversationId)
    }

    private fun parseDateToLong(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val cleanStr = dateStr.substringBefore(".").substringBefore("Z")
            format.parse(cleanStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun markSessionRead(convId: String) {
        if (convId.isBlank() || convId == "new") return
        repositoryScope.launch {
            try {
                imDao.clearUnreadCount(convId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            realtimeClient.sendReadAck(convId)
            try {
                api.chatMessages(convId)
            } catch (e: Exception) {}
        }
    }
}
