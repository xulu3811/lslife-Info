package com.qingyuan.lslife.feature.admin

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.hilt.navigation.compose.hiltViewModel
import com.qingyuan.lslife.core.model.AdminUser

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
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            TopAppBar(
                title = { Text("用户治理", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F2937)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFF1F2937))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "昵称: ${user.nickname ?: "未命名"}", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f)
                )
                if (user.status == "banned") {
                    Box(modifier = Modifier.background(Color(0xFFFEF2F2), androidx.compose.foundation.shape.RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("已封禁", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                    }
                } else {
                    Box(modifier = Modifier.background(Color(0xFFE6F4EA), androidx.compose.foundation.shape.RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("正常", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("手机号: ${user.phone}", fontSize = 14.sp, color = Color(0xFF4B5563))
            Spacer(modifier = Modifier.height(6.dp))
            Text("钱包余额: ￥${user.walletBalance}", fontSize = 14.sp, color = Color(0xFF4B5563))
            Spacer(modifier = Modifier.height(6.dp))
            Text("注册时间: ${user.createdAt}", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            
            if (user.status != "banned") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onBan,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFDC2626)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("封禁账号", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
