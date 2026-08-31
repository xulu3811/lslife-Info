package com.qingyuan.lslife.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BindEmailScreen(
    onBack: () -> Unit,
    onChangeEmail: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val email = state.email
    val isBound = !email.isNullOrBlank()

    var showUnbindDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "邮箱绑定",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isBound) Color(0xFFE8F0FE) else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Email,
                    contentDescription = null,
                    tint = if (isBound) Color(0xFF1A73E8) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isBound) {
                val maskedEmail = if (email!!.indexOf("@") > 1) "${email.substring(0, 2)}***${email.substring(email.indexOf("@"))}" else email
                Text(
                    text = "已绑定邮箱",
                    fontSize = 15.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = maskedEmail,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "邮箱可用于接收电子账单、安全验证与活动通知",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onChangeEmail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Text("更换邮箱", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { showUnbindDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("解绑邮箱", fontSize = 14.sp, color = Color(0xFF6B7280))
                }

            } else {
                Text(
                    text = "尚未绑定邮箱",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "绑定邮箱后，您可以使用邮箱快速登录，并享受更安全的服务",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onChangeEmail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Text("立即绑定", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showUnbindDialog) {
        AlertDialog(
            onDismissRequest = { showUnbindDialog = false },
            title = { Text("确认解绑邮箱？", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("解绑后您将无法通过该邮箱接收系统通知，且不可用于密码找回，请谨慎操作。", fontSize = 13.sp, color = Color(0xFF4B5563)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnbindDialog = false
                        // TODO: Implement actual unbind logic
                    },
                ) { Text("确认解绑", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showUnbindDialog = false }) { Text("取消", color = Color(0xFF6B7280)) }
            },
        )
    }
}
