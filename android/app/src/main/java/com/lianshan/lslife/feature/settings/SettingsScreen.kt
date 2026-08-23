package com.lianshan.lslife.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.BuildConfig
import com.lianshan.lslife.core.model.NotificationMode
import com.lianshan.lslife.core.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onLoggedOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmClear by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "设置与隐私", 
                        fontSize = 17.sp, 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // 一、 显示偏好模块
            SettingsSectionTitle("显示偏好")
            SettingsCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "外观主题", 
                                fontSize = 14.sp, 
                                fontWeight = FontWeight.SemiBold, 
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                "切换跟随系统或深浅色模式", 
                                fontSize = 11.sp, 
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    JoybuySegmentedControl(
                        items = ThemeMode.entries,
                        selectedItem = state.themeMode,
                        itemLabel = { it.label },
                        onItemSelected = { viewModel.setThemeMode(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 二、 消息与存储模块
            SettingsSectionTitle("消息与存储")
            SettingsCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "消息通知提醒", 
                                fontSize = 14.sp, 
                                fontWeight = FontWeight.SemiBold, 
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                "选择接收同城消息的提醒方式", 
                                fontSize = 11.sp, 
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    JoybuySegmentedControl(
                        items = NotificationMode.entries,
                        selectedItem = state.notificationMode,
                        itemLabel = { it.label },
                        onItemSelected = { viewModel.setNotificationMode(it) }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(0.5.dp)
                        .background(Color(0xFFF1F5F9))
                )

                JoybuyActionRow(
                    icon = Icons.Outlined.CleaningServices,
                    title = "清理本地缓存",
                    subtitle = "清除临时图片与商家数据缓存",
                    rightText = if (state.clearingCache) "清理中…" else "立即清理",
                    onClick = { confirmClear = true },
                    showDivider = false
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 三、 条款与关于模块
            SettingsSectionTitle("关于与条款")
            SettingsCard {
                JoybuyActionRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "隐私政策与服务协议",
                    subtitle = "了解同城信息安全与保护方式",
                    onClick = onOpenPrivacy,
                    showDivider = true
                )
                JoybuyActionRow(
                    icon = Icons.Outlined.Info,
                    title = "关于同城·连山",
                    subtitle = "当前版本 v${BuildConfig.VERSION_NAME}",
                    onClick = onOpenAbout,
                    showDivider = false
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 四、 退出登录模块 (Joybuy 典雅白卡胶囊设计)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { confirmLogout = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "退出登录",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF4D4F)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "© 2026 连山壮族瑶族自治县 · 智慧同城生活平台",
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清理本地缓存？", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("清理后商家与商品图片将在下次浏览时重新拉取，不会删除您的个人账号与发布信息。", fontSize = 13.sp, color = Color(0xFF64748B)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        viewModel.clearCache()
                    },
                ) { Text("确认清理", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消", color = Color(0xFF94A3B8)) }
            },
        )
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("退出当前账号？", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("退出后需要重新输入手机号登录以使用同城发布和互动功能。", fontSize = 13.sp, color = Color(0xFF64748B)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        viewModel.logout()
                    },
                ) { Text("确认退出", color = Color(0xFFFF4D4F), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("取消", color = Color(0xFF94A3B8)) }
            },
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color(0xFFF1F5F9)),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
        content = { Column(modifier = Modifier.fillMaxWidth(), content = content) }
    )
}

@Composable
private fun <T> JoybuySegmentedControl(
    items: List<T>,
    selectedItem: T,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onItemSelected(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = itemLabel(item),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun JoybuyActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    rightText: String? = null,
    onClick: () -> Unit,
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = Color(0xFF334155),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            if (rightText != null) {
                Text(
                    rightText,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(16.dp)
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp, end = 14.dp)
                    .height(0.5.dp)
                    .background(Color(0xFFF1F5F9))
            )
        }
    }
}
