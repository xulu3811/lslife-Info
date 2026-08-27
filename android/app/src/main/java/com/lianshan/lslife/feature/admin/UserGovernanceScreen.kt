package com.lianshan.lslife.feature.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lianshan.lslife.core.model.AdminUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGovernanceScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val users by viewModel.users.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户治理", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(users) { user ->
                UserGovernanceCard(
                    user = user,
                    onBan = { viewModel.banUser(user.id) { _, _ -> } }
                )
            }
        }
    }
}

@Composable
fun UserGovernanceCard(
    user: AdminUser,
    onBan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("昵称: ${user.nickname ?: "未设置"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("手机号: ${user.phone}", style = MaterialTheme.typography.bodyMedium)
            Text("状态: ${if (user.status == "banned") "已封禁" else "正常"}", style = MaterialTheme.typography.bodyMedium, color = if (user.status == "banned") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            Text("钱包余额: ¥${user.walletBalance}", style = MaterialTheme.typography.bodyMedium)
            Text("注册时间: ${user.createdAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (user.status != "banned") {
                    Button(
                        onClick = onBan,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("封禁账号")
                    }
                }
            }
        }
    }
}
