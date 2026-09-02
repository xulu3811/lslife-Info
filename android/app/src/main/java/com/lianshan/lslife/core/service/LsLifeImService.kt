package com.qingyuan.lslife.core.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.qingyuan.lslife.LsLifeApplication
import com.qingyuan.lslife.MainActivity
import com.qingyuan.lslife.R
import com.qingyuan.lslife.core.data.AuthRepository
import com.qingyuan.lslife.core.data.ImRepository
import com.qingyuan.lslife.core.database.ImDao
import com.qingyuan.lslife.core.network.RealtimeClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 即时通信后台保活守护服务。
 * 1. 挂载持续前台通知，大幅提升系统进程优先级（防杀）。
 * 2. 独占托管实时 WebSocket 监听，并在收到离线或后台消息时发射手机顶栏横幅弹窗（Heads-up Notification）。
 * 3. 监听网络变动广播，在 4G/5G/Wi-Fi 切换时触发 WebSocket 离线同步与重连对齐。
 */
@AndroidEntryPoint
class LsLifeImService : Service() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var imRepository: ImRepository
    @Inject lateinit var imDao: ImDao
    @Inject lateinit var realtimeClient: RealtimeClient
    @Inject lateinit var tokenStore: com.qingyuan.lslife.core.data.TokenStore

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var lastNotificationTime = 0L
    private val notificationDebounceMs = 1500L

    override fun onCreate() {
        super.onCreate()
        imRepository.initRepository()
        
        serviceScope.launch {
            if (!tokenStore.keepAliveFlow.first()) {
                stopSelf()
                return@launch
            }
            observeIncomingMessages()
            registerNetworkListener()
            
            // Listen for changes and kill if disabled
            tokenStore.keepAliveFlow.collect { keepAlive ->
                if (!keepAlive) {
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()
        serviceScope.launch {
            if (!tokenStore.keepAliveFlow.first()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
                return@launch
            }
            try {
                // 单次离线消息同步，避免 collect 触发死循环
                val sessions = imDao.getConversationsFlow().first()
                sessions.forEach { s ->
                    try { realtimeClient.sendOfflineSync(s.conversationId) } catch (e: Exception) {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServiceNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, LsLifeApplication.CHANNEL_ID_FOREGROUND)
            .setContentTitle("清远同城生活：即时通信守护中")
            .setContentText("保持交易沟通长连接，保护对话隐私与订单通知不漏回")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(FOREGROUND_NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING)
            } else {
                startForeground(FOREGROUND_NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeIncomingMessages() {
        serviceScope.launch {
            imRepository.incomingMessages.collect { msgJson ->
                try {
                    val msgObj = org.json.JSONObject(msgJson)
                    val senderId = msgObj.optString("senderId")
                    val sessionId = msgObj.optString("sessionId").ifEmpty { msgObj.optString("conversationId") }
                    val content = msgObj.optString("content")
                    val type = msgObj.optString("type").ifEmpty { msgObj.optString("msgType") }

                    val myId = authRepository.cachedMe()?.id
                    if (senderId.isNotEmpty() && senderId != myId) {
                        // 如果用户当前没有停留在该会话页面内，则必定弹出顶栏横幅弹窗与提示音
                        if (imRepository.activeChatSessionId != sessionId) {
                            val now = System.currentTimeMillis()
                            if (now - lastNotificationTime > notificationDebounceMs) {
                                lastNotificationTime = now
                                showTopBarPopupNotification(
                                    sessionId = sessionId,
                                    senderId = senderId,
                                    content = when (type.uppercase()) {
                                        "IMAGE" -> "[图片]"
                                        "VOICE" -> "[语音消息]"
                                        "POST_CARD" -> "[商品/服务]"
                                        "LOCATION" -> "[位置]"
                                        else -> content.ifBlank { "[新消息]" }
                                    }
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showTopBarPopupNotification(sessionId: String, senderId: String, content: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_chat_session_id", sessionId)
            putExtra("navigate_to_chat_sender_id", senderId)
            putExtra("navigate_to_chat_sender_name", "同城买家/商家")
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, LsLifeApplication.CHANNEL_ID_IM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("收到新消息")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 200, 250))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, false)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        try {
            NotificationManagerCompat.from(this).notify(if (sessionId.isNotEmpty()) sessionId.hashCode() else 10087, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerNetworkListener() {
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    serviceScope.launch {
                        try {
                            imDao.getConversationsFlow().first().forEach { s ->
                                realtimeClient.sendOfflineSync(s.conversationId)
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
            cm.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (e: Exception) {}
        try {
            networkCallback?.let {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                cm.unregisterNetworkCallback(it)
            }
        } catch (e: Exception) {}
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 10086

        fun start(context: Context) {
            try {
                val intent = Intent(context, LsLifeImService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, LsLifeImService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
