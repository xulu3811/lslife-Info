package com.qingyuan.lslife.ui

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.data.AuthRepository
import com.qingyuan.lslife.core.data.ImRepository
import com.qingyuan.lslife.core.data.TokenStore
import com.qingyuan.lslife.core.model.NotificationMode
import com.qingyuan.lslife.core.model.ThemeMode
import com.qingyuan.lslife.core.service.LsLifeImService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.qingyuan.lslife.core.data.LsRepository
import com.qingyuan.lslife.core.database.ImDao

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val imRepository: ImRepository,
    private val imDao: ImDao,
    private val lsRepository: LsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _navigateToChatChannel = kotlinx.coroutines.channels.Channel<Triple<String, String, String>>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val navigateToChatFlow = _navigateToChatChannel.receiveAsFlow()


    fun triggerNavigateToChat(sessionId: String, targetUserId: String, targetName: String) {
        _navigateToChatChannel.trySend(Triple(sessionId, targetUserId, targetName))
    }

    val isLoggedIn = authRepository.isLoggedIn.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null,
    )

    val themeMode = tokenStore.themeModeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ThemeMode.SYSTEM,
    )

    val unreadCount: StateFlow<Int> = imRepository.totalUnreadCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )
    private var lastAlertTime = 0L
    private val alertDebounceMs = 1500L

    val keepAlive = tokenStore.keepAliveFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    private val _inAppBanner = MutableStateFlow<InAppMessageBannerData?>(null)
    val inAppBanner = _inAppBanner.asStateFlow()
    private var bannerDismissJob: kotlinx.coroutines.Job? = null

    fun dismissInAppBanner() {
        _inAppBanner.value = null
    }

    init {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(authRepository.isLoggedIn, tokenStore.keepAliveFlow) { loggedIn, keepAlive ->
                Pair(loggedIn, keepAlive)
            }.collect { (loggedIn, keepAlive) ->
                if (loggedIn == true) {
                    try {
                        authRepository.me()
                    } catch (e: Exception) {}
                    refreshUnreadCount()
                    if (keepAlive) {
                        LsLifeImService.start(context)
                    } else {
                        LsLifeImService.stop(context)
                    }
                } else {
                    LsLifeImService.stop(context)
                }
            }
        }

        viewModelScope.launch {
            imRepository.incomingMessages.collect { msgJson ->
                try {
                    val msgObj = org.json.JSONObject(msgJson)
                    val senderId = msgObj.optString("senderId")
                    val sessionId = msgObj.optString("sessionId").ifEmpty { msgObj.optString("conversationId") }
                    val rawContent = msgObj.optString("content")
                    val type = msgObj.optString("type").ifEmpty { msgObj.optString("msgType") }
                    val myId = authRepository.cachedMe()?.id

                    if (senderId.isNotEmpty() && senderId != myId && imRepository.activeChatSessionId != sessionId) {
                        val now = System.currentTimeMillis()
                        if (now - lastAlertTime > alertDebounceMs) {
                            lastAlertTime = now
                            
                            // 1. 播放声音与震动
                            val mode = tokenStore.notificationModeFlow.first()
                            playNotificationAlert(mode)

                            // 2. 构造前台悬浮胶囊弹窗数据
                            val displayContent = when (type.uppercase()) {
                                "IMAGE" -> "[图片]"
                                "VOICE" -> "[语音消息]"
                                "POST_CARD" -> "[商品/房源卡片]"
                                else -> rawContent.ifBlank { "[新消息]" }
                            }
                            val conv = imDao.getConversation(sessionId)
                            val senderName = conv?.peerName ?: "买家/商家"
                            val senderAvatar = conv?.peerAvatar

                            _inAppBanner.value = InAppMessageBannerData(
                                sessionId = sessionId,
                                senderId = senderId,
                                senderName = senderName,
                                avatar = senderAvatar,
                                content = displayContent
                            )

                            // 3. 4秒后自动平滑收起
                            bannerDismissJob?.cancel()
                            bannerDismissJob = viewModelScope.launch {
                                kotlinx.coroutines.delay(4000)
                                if (_inAppBanner.value?.sessionId == sessionId) {
                                    _inAppBanner.value = null
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            try {
                imRepository.syncConversationsQuietly()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun playNotificationAlert(mode: NotificationMode) {
        when (mode) {
            NotificationMode.RINGTONE -> {
                try {
                    val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val mediaPlayer = android.media.MediaPlayer().apply {
                        setDataSource(context, notificationUri)
                        setAudioAttributes(
                            android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        prepare()
                        setOnCompletionListener { it.release() }
                        start()
                    }
                } catch (e: Exception) {
                    try {
                        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val r = RingtoneManager.getRingtone(context, notification)
                        r.play()
                    } catch (e2: Exception) {}
                }
                
                // 伴随轻度震动提醒
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(200)
                    }
                } catch (e: Exception) {}
            }
            NotificationMode.VIBRATE -> {
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 150, 200), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(300)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            NotificationMode.SILENT -> {}
        }
    }

    fun toggleKeepAlive(enabled: Boolean) {
        viewModelScope.launch {
            tokenStore.setKeepAlive(enabled)
        }
    }
}

data class InAppMessageBannerData(
    val sessionId: String,
    val senderId: String,
    val senderName: String,
    val avatar: String?,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
