package com.qingyuan.lslife.feature.settings

import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.BuildConfig
import com.qingyuan.lslife.core.model.AppVersionInfo
import com.qingyuan.lslife.core.network.ApiService
import com.qingyuan.lslife.core.network.RealtimeClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import javax.inject.Inject

private const val PREFS_NAME = "ota_prefs"
private const val KEY_LAST_DIALOG_SHOWN_AT = "last_dialog_shown_at"
private const val TWENTY_FOUR_HOURS_MS = 24L * 60L * 60L * 1000L

/**
 * OTA 版本升级 ViewModel
 * - 启动时检查最新版本（与本地 BuildConfig.VERSION_CODE 对比）
 * - 每 24 小时最多弹一次弹窗（对强制更新例外）
 * - 通过 DownloadManager 下载 APK 并轮询进度
 * - 下载完成后触发系统安装器
 * - 监听 WebSocket 实时推送 APP_UPDATE_AVAILABLE
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val apiService: ApiService,
    private val realtimeClient: RealtimeClient,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** 当服务端版本 > 本地版本时，此 Flow 有值 */
    private val _updateInfo = MutableStateFlow<AppVersionInfo?>(null)
    val updateInfo: StateFlow<AppVersionInfo?> = _updateInfo.asStateFlow()

    /** 下载进度 0..100，-1 表示下载失败 */
    private val _downloadProgress = MutableStateFlow(-1)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    /** 下载已完成，等待安装 */
    private val _downloadedApkUri = MutableStateFlow<Uri?>(null)
    val downloadedApkUri: StateFlow<Uri?> = _downloadedApkUri.asStateFlow()

    /** 是否正在下载中 */
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private var activeDownloadId: Long = -1L

    init {
        viewModelScope.launch(Dispatchers.IO) {
            realtimeClient.events.collect { text ->
                try {
                    val obj = Json.parseToJsonElement(text).jsonObject
                    val eventType = obj["event"]?.jsonPrimitive?.content ?: obj["type"]?.jsonPrimitive?.content
                    if (eventType == "APP_UPDATE_AVAILABLE") {
                        // 收到服务端 WebSocket 推送新版本可用，强制忽略冷却期立即检查
                        checkForUpdate(ignoreCooldown = true)
                    }
                } catch (e: Exception) {
                    // Ignore JSON parsing errors for unrelated WS messages
                }
            }
        }
    }

    /**
     * 检查是否有新版本可用。
     * 仅在"已登录"状态下调用（MainActivity 内保证）。
     * 每 24 小时最多展示一次弹窗，如果是 WebSocket 推送或强制更新则可忽略冷却。
     */
    fun checkForUpdate(ignoreCooldown: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getLatestAppVersion()
                val serverVersion = response.data ?: return@launch

                // 服务端版本号 <= 本地版本号，无需更新
                if (serverVersion.versionCode <= BuildConfig.VERSION_CODE) return@launch

                // 24h 冷却检查 (除非忽略冷却，或者是强制更新)
                if (!ignoreCooldown && !serverVersion.isForced) {
                    val lastShownAt = prefs.getLong(KEY_LAST_DIALOG_SHOWN_AT, 0L)
                    val now = System.currentTimeMillis()
                    if (now - lastShownAt < TWENTY_FOUR_HOURS_MS) return@launch
                }

                // 触发弹窗
                _updateInfo.value = serverVersion
            } catch (e: Exception) {
                // 网络错误静默忽略，不影响正常使用
                e.printStackTrace()
            }
        }
    }

    /** 用户关闭弹窗（仅可选更新时可调用），记录时间戳 */
    fun dismissUpdate() {
        prefs.edit().putLong(KEY_LAST_DIALOG_SHOWN_AT, System.currentTimeMillis()).apply()
        _updateInfo.value = null
    }

    /** 开始下载 APK */
    fun startDownload(downloadUrl: String, versionName: String) {
        if (_isDownloading.value) return
        _isDownloading.value = true
        _downloadProgress.value = 0

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val fileName = "LsLife-v$versionName.apk"

                val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                    setTitle("LsLife 更新")
                    setDescription("正在下载 v$versionName...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                    setMimeType("application/vnd.android.package-archive")
                }

                activeDownloadId = dm.enqueue(request)
                pollDownloadProgress(dm, activeDownloadId, versionName)
            } catch (e: Exception) {
                e.printStackTrace()
                _isDownloading.value = false
                _downloadProgress.value = -1
            }
        }
    }

    /** 轮询 DownloadManager 进度，直到完成或失败 */
    private suspend fun pollDownloadProgress(dm: DownloadManager, downloadId: Long, versionName: String) {
        while (true) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = dm.query(query)

            if (cursor == null || !cursor.moveToFirst()) {
                cursor?.close()
                _isDownloading.value = false
                _downloadProgress.value = -1
                break
            }

            val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val bytesDownloaded = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val bytesTotal = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

            val status = cursor.getInt(statusIdx)
            val downloaded = cursor.getLong(bytesDownloaded)
            val total = cursor.getLong(bytesTotal)
            cursor.close()

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    _downloadProgress.value = 100
                    _isDownloading.value = false
                    // 获取已下载文件的 Uri
                    val fileUri = buildApkUri(versionName)
                    _downloadedApkUri.value = fileUri
                    // 记录弹窗时间，避免安装后重复提示
                    prefs.edit().putLong(KEY_LAST_DIALOG_SHOWN_AT, System.currentTimeMillis()).apply()
                    break
                }
                DownloadManager.STATUS_FAILED -> {
                    _isDownloading.value = false
                    _downloadProgress.value = -1
                    break
                }
                else -> {
                    if (total > 0) {
                        _downloadProgress.value = ((downloaded * 100L) / total).toInt()
                    }
                }
            }
            delay(500L)
        }
    }

    /** 构建下载文件的 FileProvider Uri */
    private fun buildApkUri(versionName: String): Uri? {
        return try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "LsLife-v$versionName.apk"
            )
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** 触发系统安装器安装 APK */
    fun installApk(apkUri: Uri) {
        try {
            val installIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 重置下载状态（用于关闭弹窗后清理） */
    fun resetDownloadState() {
        _downloadProgress.value = -1
        _isDownloading.value = false
        _downloadedApkUri.value = null
        activeDownloadId = -1L
    }
}
