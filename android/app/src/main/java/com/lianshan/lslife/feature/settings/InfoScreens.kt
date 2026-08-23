package com.lianshan.lslife.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.lianshan.lslife.BuildConfig
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.Dimens

@Composable
fun AboutScreen(onBack: () -> Unit) {
    InfoPage(title = "关于连山同城", onBack = onBack) {
        Text(
            "连山同城",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "面向连山壮族瑶族自治县的本地生活服务平台，连接社区居民、商家与本地服务者。",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "© 2026 连山同城 · 智慧同城生活平台",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    InfoPage(title = "隐私与服务协议", onBack = onBack) {
        PrivacySection(
            "我们如何使用信息",
            "为完成登录、订单、配送、发布与客户服务，我们仅在必要范围内处理手机号、收货地址、订单和发布内容。",
        )
        PrivacySection(
            "设备权限",
            "定位用于附近服务和配送追踪；相机用于发布图片；通知用于订单与配送提醒。你可以随时在系统设置中关闭权限。",
        )
        PrivacySection(
            "信息安全",
            "通信采用 HTTPS 加密。敏感信息按最小化原则处理，未经授权不会向无关第三方提供。",
        )
        PrivacySection(
            "你的权利",
            "你可以管理个人资料、地址与通知偏好，并可联系我们申请访问、更正或删除个人信息。",
        )
        PrivacySection(
            "未成年人保护",
            "未成年人应在监护人指导下使用本服务。我们不会主动收集与服务无关的未成年人信息。",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        SoftCard(
            modifier = Modifier
                .padding(padding)
                .padding(Dimens.lg),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
