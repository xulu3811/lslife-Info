package com.qingyuan.lslife.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.qingyuan.lslife.core.model.AdminDashboardData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToMonitor: () -> Unit
) {
    val dashboardState by viewModel.dashboardState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            TopAppBar(
                title = { Text("平台数据中心", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F2937)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1F2937))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            dashboardState?.let { data ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        M3DashboardCard(
                            title = "注册用户(总数)",
                            value = data.totalUsers.toString(),
                            icon = Icons.Default.Group,
                            iconTint = Color(0xFF4285F4),
                            iconBg = Color(0xFFE8F0FE)
                        )
                    }
                    item {
                        M3DashboardCard(
                            title = "平台会员(总数)",
                            value = data.totalMembers.toString(),
                            icon = Icons.Default.CardMembership,
                            iconTint = Color(0xFFF9AB00),
                            iconBg = Color(0xFFFEF7E0)
                        )
                    }
                    item {
                        M3DashboardCard(
                            title = "已实名认证(百分比)",
                            value = data.kycPercentage,
                            icon = Icons.Default.VerifiedUser,
                            iconTint = Color(0xFF34A853),
                            iconBg = Color(0xFFE6F4EA)
                        )
                    }
                    item {
                        M3DashboardCard(
                            title = "已认证商家(数量)",
                            value = data.verifiedMerchants.toString(),
                            icon = Icons.Default.Storefront,
                            iconTint = Color(0xFF8E24AA),
                            iconBg = Color(0xFFF3E5F5)
                        )
                    }
                    item {
                        M3DashboardCard(
                            title = "服务器监控模块",
                            value = "点击查看",
                            icon = Icons.Default.Storage,
                            iconTint = Color(0xFFEA4335),
                            iconBg = Color(0xFFFCE8E6),
                            onClick = onNavigateToMonitor
                        )
                    }
                    item {
                        M3DashboardCard(
                            title = "用户累计充值金额",
                            value = "￥${data.totalRecharge}",
                            icon = Icons.Default.MonetizationOn,
                            iconTint = Color(0xFF00ACC1),
                            iconBg = Color(0xFFE0F7FA)
                        )
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun M3DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth().let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 13.sp, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
        }
    }
}
