package com.lianshan.lslife.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.AuthRepository
import com.lianshan.lslife.core.data.ImRepository
import com.lianshan.lslife.core.database.ImDao
import com.lianshan.lslife.core.database.LocalMessageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.lianshan.lslife.core.data.LsRepository
import javax.inject.Inject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull

data class ChatUiState(
    val loading: Boolean = false,
    val messages: List<LocalMessageEntity> = emptyList(),
    val error: String? = null,
    val currentUserId: String = "",
    val currentUserName: String = "",
    val currentUserAvatar: String? = null,
    val targetAvatar: String? = null,
    val isFriend: Boolean = true
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val imRepository: ImRepository,
    private val imDao: ImDao,
    private val authRepository: AuthRepository,
    private val lsRepository: LsRepository,
    private val api: com.lianshan.lslife.core.network.ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state

    private var currentConvId: String = ""
    private var targetUserId: String = ""
    private var targetPostId: String? = null
    private var postCardSent: Boolean = false

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    private var messageJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            authRepository.me().onSuccess { user ->
                _state.update { 
                    it.copy(
                        currentUserId = user.id,
                        currentUserName = user.nickname.takeIf { n -> !n.isNullOrBlank() } ?: "我",
                        currentUserAvatar = user.avatar
                    )
                }
            }
        }
        
        viewModelScope.launch {
            imRepository.incomingMessages.collect { msgJson ->
                try {
                    val obj = kotlinx.serialization.json.Json.parseToJsonElement(msgJson).jsonObject
                    val serverConvId = obj["sessionId"]?.jsonPrimitive?.content ?: return@collect
                    val senderId = obj["senderId"]?.jsonPrimitive?.content ?: return@collect
                    
                    if (currentConvId.isEmpty() || currentConvId == "new") {
                        if (senderId == _state.value.currentUserId || senderId == targetUserId) {
                            currentConvId = serverConvId
                            imRepository.activeChatSessionId = serverConvId
                            subscribeToMessages(serverConvId)
                            imRepository.markSessionRead(serverConvId)
                        }
                    } else if (currentConvId == serverConvId) {
                        imRepository.markSessionRead(serverConvId)
                    }
                } catch (e: Exception) {}
            }
        }

        viewModelScope.launch {
            imRepository.ackEvents.collect { ackJson ->
                try {
                    val obj = kotlinx.serialization.json.Json.parseToJsonElement(ackJson).jsonObject
                    val serverConvId = obj["conversationId"]?.jsonPrimitive?.content
                    if (!serverConvId.isNullOrEmpty() && (currentConvId.isEmpty() || currentConvId == "new")) {
                        currentConvId = serverConvId
                        imRepository.activeChatSessionId = serverConvId
                        subscribeToMessages(serverConvId)
                        imRepository.markSessionRead(serverConvId)
                    }
                } catch (e: Exception) {}
            }
        }
    }

    private fun subscribeToMessages(convId: String) {
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            val convIds = if (convId.isEmpty() || convId == "new") listOf("new") else listOf(convId, "new")
            imDao.getMessagesFlowByConvIds(convIds).collect { localMessages ->
                _state.update { it.copy(messages = localMessages, loading = false) }
            }
        }
    }

    fun initSession(convId: String, toUserId: String, initPostId: String? = null) {
        currentConvId = convId
        targetUserId = toUserId
        targetPostId = initPostId
        
        // 1. 立即订阅消息流，确保发送的第一条消息能毫秒级乐观上屏
        subscribeToMessages(convId)

        // 2. 如果是 "new"，尝试从本地库匹配已有与该用户的会话
        viewModelScope.launch {
            if (convId.isEmpty() || convId == "new") {
                val existingConv = imDao.getConversationByPeerId(toUserId)
                if (existingConv != null) {
                    currentConvId = existingConv.conversationId
                    imRepository.activeChatSessionId = currentConvId
                    subscribeToMessages(currentConvId)
                    imRepository.markSessionRead(currentConvId)
                    _state.update { it.copy(targetAvatar = existingConv.peerAvatar) }
                } else {
                    // Try to fetch target profile if new and no local conv
                    lsRepository.getUserPublicProfile(toUserId).onSuccess { profile ->
                        _state.update { it.copy(targetAvatar = profile.avatar) }
                    }
                }
            } else {
                imRepository.activeChatSessionId = convId
                imRepository.markSessionRead(convId)
                val conv = imDao.getConversation(convId)
                if (conv != null) {
                    _state.update { it.copy(targetAvatar = conv.peerAvatar) }
                } else {
                    lsRepository.getUserPublicProfile(toUserId).onSuccess { profile ->
                        _state.update { it.copy(targetAvatar = profile.avatar) }
                    }
                }
            }

            val resolvedConvId = currentConvId
            if (resolvedConvId.isNotEmpty() && resolvedConvId != "new") {
                imRepository.syncMessagesFromRest(resolvedConvId)
                imRepository.syncOffline(resolvedConvId)
            }
            
            // 检查好友状态
            try {
                val res = api.getFriendList()
                if (res.code == 0 && res.data != null) {
                    val isF = res.data.friends.any { it.id == toUserId }
                    _state.update { it.copy(isFriend = isF) }
                }
            } catch (e: Exception) {}

            // Send product card automatically if initPostId is provided
            if (!initPostId.isNullOrEmpty() && !postCardSent) {
                postCardSent = true
                try {
                    val postRes = api.post(initPostId)
                    if (postRes.code == 0 && postRes.data != null) {
                        val post = postRes.data
                        val title = post.title
                        val image = post.images.firstOrNull() ?: ""
                        
                        val json = org.json.JSONObject().apply {
                            put("id", post.id)
                            put("title", title)
                            put("price", post.price ?: 0.0)
                            put("image", image)
                        }.toString()
                        
                        sendMessage(json, "POST_CARD")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun sendMessage(content: String, type: String = "TEXT") {
        if (content.isBlank() || targetUserId.isEmpty()) return
        
        viewModelScope.launch {
            imRepository.sendMessage(currentConvId, targetUserId, targetPostId, content, type)
            if (type == "TEXT") {
                _inputText.value = ""
            }
        }
    }

    fun sendLocation(lat: Double, lng: Double, name: String, address: String) {
        val json = """{"lat":$lat,"lng":$lng,"name":"$name","address":"$address"}"""
        sendMessage(json, "LOCATION")
    }

    fun uploadAndSendImage(imagePath: String, compress: Boolean = true) {
        viewModelScope.launch {
            try {
                val file = java.io.File(imagePath)
                if (!file.exists()) return@launch
                val requestFile = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val body = okhttp3.MultipartBody.Part.createFormData("image", file.name, requestFile)
                val res = api.uploadChatImage(body)
                if (res.code == 0 && res.data != null) {
                    imRepository.sendMessage(
                        convId = currentConvId,
                        toUserId = targetUserId,
                        targetId = targetPostId,
                        content = res.data.url,
                        type = "IMAGE",
                        mediaHash = res.data.mediaHash
                    )
                } else {
                    _state.update { it.copy(error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "图片上传失败") }
            }
        }
    }

    fun sendVoice(filePath: String, duration: Int) {
        viewModelScope.launch {
            try {
                val file = java.io.File(filePath)
                if (!file.exists()) return@launch
                val requestFile = okhttp3.RequestBody.create("audio/mp4".toMediaTypeOrNull(), file)
                val body = okhttp3.MultipartBody.Part.createFormData("audio", file.name, requestFile)
                val res = api.uploadAudio(body)
                if (res.code == 0 && res.data != null) {
                    imRepository.sendMessage(
                        convId = currentConvId,
                        toUserId = targetUserId,
                        targetId = targetPostId,
                        content = "${res.data.url}|$duration",
                        type = "VOICE",
                        mediaHash = res.data.mediaHash
                    )
                } else {
                    _state.update { it.copy(error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "语音上传失败") }
            }
        }
    }

    fun recallMessage(messageId: String) {
        imRepository.recallMessage(messageId)
    }

    private val _selectedMessageIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedMessageIds: StateFlow<Set<String>> = _selectedMessageIds.asStateFlow()

    fun toggleSelection(msgId: String) {
        val current = _selectedMessageIds.value
        _selectedMessageIds.value = if (current.contains(msgId)) {
            current - msgId
        } else {
            current + msgId
        }
    }

    fun clearSelection() {
        _selectedMessageIds.value = emptySet()
    }

    fun deleteSelectedMessages() {
        val toDelete = _selectedMessageIds.value
        if (toDelete.isEmpty()) return
        
        viewModelScope.launch {
            imDao.deleteMessages(toDelete)
            val count = imDao.getMessageCount(currentConvId)
            if (count == 0) {
                imRepository.hideConversation(currentConvId)
            }
            _selectedMessageIds.value = emptySet()
            // If the last message was deleted, update conversation? (Handled passively by syncing next time, or could be refined)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        imRepository.activeChatSessionId = null
    }
}
