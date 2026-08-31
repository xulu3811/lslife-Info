package com.qingyuan.lslife.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qingyuan.lslife.core.model.ServerMonitorData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerMonitorScreen(
    viewModel: ServerMonitorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val serverState by viewModel.serverState.collectAsState()
    val loading by viewModel.loading.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startPolling()
        onDispose { }
    }

    Scaffold(
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            TopAppBar(
                title = { Text("服务器监控 (实时)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F2937)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1F2937))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (loading && serverState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            serverState?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // CPU Card
                    MonitorCard(
                        title = "CPU 状态",
                        icon = Icons.Default.DeveloperBoard,
                        iconTint = Color(0xFF4285F4)
                    ) {
                        Text("内核数: ${data.cpu.cores}", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("系统负载(1min): ${data.cpu.loadAvg}", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("综合占用率: ${data.cpu.percent}%", fontWeight = FontWeight.Bold, color = if (data.cpu.percent > 80) Color.Red else Color(0xFF1F2937))
                        LinearProgressIndicator(progress = { data.cpu.percent / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 8.dp), color = if (data.cpu.percent > 80) Color.Red else Color(0xFF4285F4))
                    }

                    // RAM Card
                    MonitorCard(
                        title = "内存状态",
                        icon = Icons.Default.Memory,
                        iconTint = Color(0xFF34A853)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("已用: ${data.ram.used}", fontSize = 14.sp)
                            Text("总量: ${data.ram.total}", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("内存占用率: ${data.ram.percent}%", fontWeight = FontWeight.Bold, color = if (data.ram.percent > 80) Color.Red else Color(0xFF1F2937))
                        LinearProgressIndicator(progress = { data.ram.percent / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 8.dp), color = if (data.ram.percent > 80) Color.Red else Color(0xFF34A853))
                    }

                    // Disk Card
                    MonitorCard(
                        title = "磁盘状态",
                        icon = Icons.Default.Storage,
                        iconTint = Color(0xFFF9AB00)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("已用: ${data.disk.used}", fontSize = 14.sp)
                            Text("总量: ${data.disk.total}", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("磁盘占用率: ${data.disk.percent}%", fontWeight = FontWeight.Bold, color = if (data.disk.percent > 90) Color.Red else Color(0xFF1F2937))
                        LinearProgressIndicator(progress = { data.disk.percent / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 8.dp), color = if (data.disk.percent > 90) Color.Red else Color(0xFFF9AB00))
                    }

                    // PM2 Card
                    MonitorCard(
                        title = "PM2 后端服务状态",
                        icon = Icons.Default.Storage,
                        iconTint = Color(0xFF8E24AA)
                    ) {
                        if (data.pm2.isEmpty()) {
                            Text("暂无运行中的 PM2 进程", fontSize = 14.sp, color = Color.Gray)
                        } else {
                            data.pm2.forEach { pm2 ->
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(pm2.name, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                        Text(pm2.status.uppercase(), color = if (pm2.status == "online") Color(0xFF059669) else Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("内存: ${pm2.memory} | CPU: ${pm2.cpu}%", fontSize = 12.sp, color = Color(0xFF4B5563))
                                    Text("重启次数: ${pm2.restarts}", fontSize = 12.sp, color = Color(0xFF4B5563))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MonitorCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
