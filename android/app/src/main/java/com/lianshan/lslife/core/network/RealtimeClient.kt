package com.lianshan.lslife.core.network

import android.util.Log
import com.lianshan.lslife.BuildConfig
import com.lianshan.lslife.core.data.TokenStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeClient @Inject constructor(
    private val client: OkHttpClient,
    private val tokenStore: TokenStore,
) {
    private var _activeWebSocket: WebSocket? = null
    
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val events: Flow<String> = _events.asSharedFlow()

    private var connectJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isIntentionalClose = false
    private var reconnectDelay = 1000L

    fun connect() {
        if (_activeWebSocket != null) return
        isIntentionalClose = false
        
        connectJob?.cancel()
        connectJob = scope.launch {
            while (isActive && !isIntentionalClose) {
                val token = tokenStore.current()
                if (token.isNullOrBlank()) {
                    delay(2000)
                    continue
                }

                val request = Request.Builder()
                    .url("${BuildConfig.WS_BASE_URL}?token=$token")
                    .build()

                val connectedDeferred = CompletableDeferred<Boolean>()

                val listener = object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        _activeWebSocket = webSocket
                        reconnectDelay = 1000L // Reset backoff
                        connectedDeferred.complete(true)
                        webSocket.send("""{"action":"sync_offline","conversationId":"all"}""")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        _events.tryEmit(text)
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        Log.e("RealtimeClient", "WebSocket Error", t)
                        _activeWebSocket = null
                        if (!connectedDeferred.isCompleted) {
                            connectedDeferred.complete(false)
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        _activeWebSocket = null
                        if (!connectedDeferred.isCompleted) {
                            connectedDeferred.complete(false)
                        }
                    }
                }

                _activeWebSocket = client.newWebSocket(request, listener)
                
                val connected = connectedDeferred.await()
                if (!connected) {
                    delay(reconnectDelay)
                    reconnectDelay = (reconnectDelay * 2).coerceAtMost(30000L)
                } else {
                    // Wait until the connection drops
                    while (_activeWebSocket != null && !isIntentionalClose) {
                        delay(1000)
                    }
                }
            }
        }
    }

    fun disconnect() {
        isIntentionalClose = true
        connectJob?.cancel()
        _activeWebSocket?.close(1000, "intentional close")
        _activeWebSocket = null
    }

    fun events(): Flow<String> = events

    fun sendChatMessage(
        clientMsgId: String, 
        toUserId: String, 
        targetId: String?, 
        content: String, 
        type: String = "TEXT", 
        mediaHash: String? = null
    ) {
        val targetPart = if (targetId != null) ""","targetId":"$targetId"""" else ""
        val hashPart = if (mediaHash != null) ""","mediaHash":"$mediaHash"""" else ""
        val payload = """{"action":"chat","clientMsgId":"$clientMsgId","toUserId":"$toUserId","content":"$content","type":"$type"$targetPart$hashPart}"""
        _activeWebSocket?.send(payload)
    }

    fun sendRecallMessage(messageId: String) {
        val payload = """{"action":"recall","messageId":"$messageId"}"""
        _activeWebSocket?.send(payload)
    }

    fun sendOfflineSync(conversationId: String) {
        val payload = """{"action":"sync_offline","conversationId":"$conversationId"}"""
        _activeWebSocket?.send(payload)
    }

    fun sendReadAck(sessionId: String) {
        val payload = """{"action":"read_ack","sessionId":"$sessionId"}"""
        _activeWebSocket?.send(payload)
    }
}
