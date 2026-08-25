package com.lianshan.lslife.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    onOpenApprovals: () -> Unit,
    onOpenUserGovernance: () -> Unit,
    onOpenContentGovernance: () -> Unit,
    onOpenReports: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("平台管理中枢", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            DataOverviewSection()
            Spacer(modifier = Modifier.height(20.dp))
            
            Spacer(modifier = Modifier.height(20.dp))
            TodoListSection()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SoftCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 0.5.dp,
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun DataOverviewSection() {
    SoftCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("核心大盘", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("今日实时", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DataStatItem("新增用户", "128", Color(0xFF3B82F6))
            DataStatItem("新增发布", "356", Color(0xFF10B981))
            DataStatItem("待办审批", "12", Color(0xFFF59E0B))
            DataStatItem("异常举报", "3", Color(0xFFEF4444))
        }
    }
}

@Composable
private fun DataStatItem(label: String, value: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        }
    }
}


@Composable
private fun TodoListSection() {
    SoftCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("全站待办与动态", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("全部", fontSize = 12.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        TodoItem("商家入驻", "用户 138****1234 提交了申请", Color(0xFFF59E0B))
        Divider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(start = 28.dp))
        TodoItem("实名认证", "用户 李* 提交了认证", Color(0xFF3B82F6))
        Divider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(start = 28.dp))
        TodoItem("违规举报", "二手车出售帖被多人举报", Color(0xFFEF4444))
    }
}

@Composable
private fun TodoItem(tag: String, title: String, tagColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .border(0.5.dp, tagColor, RoundedCornerShape(4.dp))
                .background(tagColor.copy(alpha = 0.1f))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(tag, fontSize = 9.sp, color = tagColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title, 
            fontSize = 13.sp, 
            color = Color(0xFF334155), 
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
    }
}
