package com.lianshan.lslife

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.ThemeMode
import com.lianshan.lslife.feature.settings.UpdateDialog
import com.lianshan.lslife.feature.settings.UpdateViewModel
import com.lianshan.lslife.ui.LsLifeApp
import com.lianshan.lslife.ui.SessionViewModel
import com.lianshan.lslife.ui.theme.LsLifeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sessionViewModel: SessionViewModel by viewModels()
    private val updateViewModel: UpdateViewModel by viewModels()

    private val showPermissionRationale = kotlinx.coroutines.flow.MutableStateFlow(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            showPermissionRationale.value = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        handleChatIntent(intent)
        setContent {
            val themeMode by sessionViewModel.themeMode.collectAsStateWithLifecycle()
            val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val updateInfo by updateViewModel.updateInfo.collectAsStateWithLifecycle()

            val useDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // 登录成功后触发版本检查（仅在登录状态下）
            androidx.compose.runtime.LaunchedEffect(isLoggedIn) {
                if (isLoggedIn == true) {
                    updateViewModel.checkForUpdate()
                }
            }

            LsLifeTheme(darkTheme = useDarkTheme) {
                LsLifeApp(sessionViewModel = sessionViewModel)

                val showRationale by showPermissionRationale.collectAsStateWithLifecycle()
                if (showRationale) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showPermissionRationale.value = false },
                        title = { androidx.compose.material3.Text("需要通知权限") },
                        text = { androidx.compose.material3.Text("为了防止您错过同城买家/商家的私聊和订单通知，请允许连山同城发送系统通知。") },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                showPermissionRationale.value = false
                                val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
                                }
                                startActivity(intent)
                            }) {
                                androidx.compose.material3.Text("去设置开启")
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showPermissionRationale.value = false }) {
                                androidx.compose.material3.Text("暂不")
                            }
                        }
                    )
                }

                // OTA 更新弹窗 — 有新版本时展示
                if (updateInfo != null) {
                    UpdateDialog(
                        versionInfo = updateInfo!!,
                        viewModel = updateViewModel,
                        onDismiss = {
                            updateViewModel.dismissUpdate()
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleChatIntent(intent)
    }

    private fun handleChatIntent(intent: Intent?) {
        val sessionId = intent?.getStringExtra("navigate_to_chat_session_id")
        val senderId = intent?.getStringExtra("navigate_to_chat_sender_id") ?: ""
        val senderName = intent?.getStringExtra("navigate_to_chat_sender_name") ?: "同城买家/商家"
        if (!sessionId.isNullOrBlank()) {
            sessionViewModel.triggerNavigateToChat(sessionId, senderId, senderName)
            intent.removeExtra("navigate_to_chat_session_id")
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showPermissionRationale.value = true
                } else {
                    try {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
