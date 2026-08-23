package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("消息通知") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            MessageCard(
                icon = Icons.Filled.LocalShipping,
                title = "订单已发货",
                time = "刚刚",
                content = "您的订单 #202607180001 已经发货，骑手正在飞奔向您。",
                unread = true
            )
            MessageCard(
                icon = Icons.Filled.Info,
                title = "系统通知",
                time = "昨天 14:00",
                content = "欢迎加入连山同城生活！现在开通高级会员可享受每月50条免费发布额度。",
                unread = false
            )
        }
    }
}

@Composable
private fun MessageCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    time: String,
    content: String,
    unread: Boolean
) {
    SoftCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.md),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (unread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp).padding(top = Dimens.xs)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium,
                        color = if (unread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
