package com.lianshan.lslife.feature.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lianshan.lslife.core.model.AdminDashboardData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val dashboardState by viewModel.dashboardState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("平台数据中心", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            dashboardState?.let { data ->
                DashboardCard(
                    title = "新增用户 (24H)",
                    value = data.newUsers.toString(),
                    icon = Icons.Default.Group
                )
                DashboardCard(
                    title = "平台营收",
                    value = "¥${data.revenue}",
                    icon = Icons.Default.MonetizationOn
                )
                DashboardCard(
                    title = "待审核内容",
                    value = data.pendingReviews.toString(),
                    icon = Icons.Default.PendingActions
                )
                DashboardCard(
                    title = "待实名认证",
                    value = data.pendingKyc.toString(),
                    icon = Icons.Default.PendingActions
                )
                DashboardCard(
                    title = "待认证商家",
                    value = data.pendingMerchantCerts.toString(),
                    icon = Icons.Default.PendingActions
                )
            } ?: run {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
