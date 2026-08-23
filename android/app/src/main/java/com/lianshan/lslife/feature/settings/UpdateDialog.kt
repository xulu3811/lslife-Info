package com.lianshan.lslife.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.AppVersionInfo

/**
 * OTA 更新弹窗
 * - 强制更新：不可关闭，无"稍后"按钮
 * - 可选更新：可关闭，有"稍后再说"按钮
 * - 下载中展示进度条
 * - 下载完成后自动触发安装
 */
@Composable
fun UpdateDialog(
    versionInfo: AppVersionInfo,
    viewModel: UpdateViewModel,
    onDismiss: () -> Unit,
) {
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()
    val downloadedUri by viewModel.downloadedApkUri.collectAsStateWithLifecycle()

    // 下载完成后自动触发安装
    LaunchedEffect(downloadedUri) {
        if (downloadedUri != null) {
            viewModel.installApk(downloadedUri!!)
        }
    }

    val isForced = versionInfo.isForced

    Dialog(
        onDismissRequest = {
            // 强制更新不允许通过点击外部关闭
            if (!isForced && !isDownloading) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isForced,
            dismissOnClickOutside = !isForced,
            usePlatformDefaultWidth = false,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // ====== 顶部渐变图标区域 ======
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // 外圈光晕
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SystemUpdate,
                                contentDescription = "更新图标",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                        // 版本标签
                        Text(
                            text = "v${versionInfo.versionName}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 14.dp)
                        )
                    }

                    // ====== 内容区域 ======
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // 标题
                        Text(
                            text = if (isForced) "⚠️ 需要更新才能继续" else "🎉 发现新版本",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(4.dp))

                        // 文件大小
                        if (versionInfo.fileSize != null && versionInfo.fileSize > 0L) {
                            Text(
                                text = "安装包大小：${formatFileSize(versionInfo.fileSize)}",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E),
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // 更新日志
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5F7FA),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "更新内容",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                versionInfo.releaseNotes
                                    .split("\n")
                                    .filter { it.isNotBlank() }
                                    .forEach { line ->
                                        Text(
                                            text = "• $line",
                                            fontSize = 13.sp,
                                            color = Color(0xFF424242),
                                            lineHeight = 20.sp,
                                        )
                                    }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ====== 下载进度条 ======
                        if (isDownloading || downloadProgress in 0..99) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val progress = if (downloadProgress < 0) 0f else downloadProgress / 100f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF1565C0),
                                    trackColor = Color(0xFFE3F2FD),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (downloadProgress >= 100) "下载完成，正在启动安装..." else "正在下载... $downloadProgress%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9E9E9E),
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        // ====== 按钮区域 ======
                        if (!isDownloading || downloadProgress < 0) {
                            // "立即更新"按钮（下载完成后变"立即安装"）
                            Button(
                                onClick = {
                                    if (downloadedUri != null) {
                                        viewModel.installApk(downloadedUri!!)
                                    } else {
                                        viewModel.startDownload(versionInfo.downloadUrl, versionInfo.versionName)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1565C0),
                                    contentColor = Color.White,
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp,
                                )
                            ) {
                                Text(
                                    text = when {
                                        downloadedUri != null -> "📦 立即安装"
                                        downloadProgress == -1 && !isDownloading -> "⬇️ 立即更新"
                                        else -> "⬇️ 立即更新"
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                )
                            }

                            // 可选更新：稍后按钮
                            if (!isForced) {
                                Spacer(Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        viewModel.dismissUpdate()
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                ) {
                                    Text(
                                        text = "稍后再说",
                                        color = Color(0xFF9E9E9E),
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                        } else {
                            // 下载中禁用按钮
                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFFBBDEFB),
                                    disabledContentColor = Color(0xFF1565C0),
                                )
                            ) {
                                Text(
                                    text = "下载中 $downloadProgress%",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
