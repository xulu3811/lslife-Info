package com.qingyuan.lslife.feature.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.material3.HorizontalDivider
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
import com.qingyuan.lslife.BuildConfig
import com.qingyuan.lslife.core.model.NotificationMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenBindPhone: () -> Unit = {},
    onOpenBindEmail: () -> Unit = {},
    onOpenChangePassword: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val maskedPhone = state.phone?.let { if (it.length >= 11) "${it.substring(0, 3)}****${it.substring(7)}" else it } ?: "未绑定"
    val maskedEmail = state.email?.let { if (it.indexOf("@") > 1) "${it.substring(0, 2)}***${it.substring(it.indexOf("@"))}" else it } ?: "未设置"
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
        containerColor = Color(0xFFF3F5F8),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "设置与隐私", 
                        fontSize = 17.sp, 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = Color(0xFF374151)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF3F5F8)),
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
            
            // 模块 1: 账号与安全
            SettingsGroupCard(title = "账号与安全") {
                SettingsActionRow(
                    icon = Icons.Outlined.PhoneAndroid,
                    title = "手机号绑定",
                    subtitle = "更换或解绑手机号",
                    rightText = maskedPhone,
                    onClick = onOpenBindPhone
                )
                SettingsActionRow(
                    icon = Icons.Outlined.Email,
                    title = "安全邮箱",
                    subtitle = "用于接收重要通知与账单",
                    rightText = maskedEmail,
                    onClick = onOpenBindEmail
                )
                SettingsActionRow(
                    icon = Icons.Outlined.Lock,
                    title = "修改密码",
                    subtitle = "定期更改密码以保障账号安全",
                    onClick = onOpenChangePassword,
                    showDivider = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 模块 2: 消息与存储
            SettingsGroupCard(title = "消息与存储") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = Color(0xFF374151).copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "消息通知提醒", 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Medium, 
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "选择接收同城消息的提醒方式", 
                            fontSize = 11.sp, 
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                M3SegmentedControl(
                    items = NotificationMode.entries,
                    selectedItem = state.notificationMode,
                    itemLabel = { it.label },
                    onItemSelected = { viewModel.setNotificationMode(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(start = 50.dp, end = 16.dp), 
                    thickness = 0.5.dp, 
                    color = Color(0xFFF3F4F6)
                )

                SettingsActionRow(
                    icon = Icons.Outlined.CleaningServices,
                    title = "清理本地缓存",
                    subtitle = "清除临时图片与商家数据缓存",
                    rightText = if (state.clearingCache) "清理中..." else "立即清理",
                    onClick = { confirmClear = true },
                    showDivider = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 模块 3: 关于与条款
            SettingsGroupCard(title = "关于与条款") {
                SettingsActionRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "隐私政策与服务协议",
                    subtitle = "了解同城信息安全与保护方式",
                    onClick = onOpenPrivacy,
                    showDivider = true
                )
                SettingsActionRow(
                    icon = Icons.Outlined.Info,
                    title = "关于同城清远",
                    subtitle = "当前版本 v${BuildConfig.VERSION_NAME}",
                    onClick = onOpenAbout,
                    showDivider = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 退出登录按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable { confirmLogout = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "退出登录",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "© 2026 清远壮族瑶族自治县 · 智慧同城生活平台",
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清理本地缓存？", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("清理将释放存储空间，不会影响您的正常使用。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        viewModel.clearCache()
                    },
                ) { Text("确认清理", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            },
        )
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("退出登录？", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("退出后将无法接收同城商家的实时私聊消息。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        viewModel.logout()
                    },
                ) { Text("确认退出", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            },
        )
    }
}

@Composable
private fun SettingsGroupCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151),
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 4.dp)
            )
            content()
        }
    }
}

@Composable
private fun <T> M3SegmentedControl(
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
            .background(Color(0xFFF3F5F8))
            .padding(4.dp),
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
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF374151) else Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    rightText: String? = null,
    onClick: () -> Unit,
    showDivider: Boolean = true,
) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = Color(0xFF374151).copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
            if (rightText != null) {
                Text(
                    rightText,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(14.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 50.dp, end = 16.dp), 
                thickness = 0.5.dp, 
                color = Color(0xFFF3F4F6)
            )
        }
    }
}
