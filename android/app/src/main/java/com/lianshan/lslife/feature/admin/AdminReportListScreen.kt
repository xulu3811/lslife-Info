package com.lianshan.lslife.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.Report
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportListScreen(
    onBack: () -> Unit,
    onReportClick: (reportId: String, targetId: String, targetType: String) -> Unit,
    viewModel: AdminReportListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val snackbar = remember { SnackbarHostState() }

    var topLevelTab by remember { mutableIntStateOf(0) }
    val topTabs = listOf("举报处理", "资源治理")

    val tabs = listOf(
        "PENDING" to "待处理",
        "RESOLVED" to "已解决",
        "INVALID" to "已忽略"
    )

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("内容风控", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
                Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = topLevelTab,
                containerColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[topLevelTab]),
                        color = scheme.primary
                    )
                }
            ) {
                topTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = topLevelTab == index,
                        onClick = { topLevelTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    )
                }
            }
            
            if (topLevelTab == 0) {
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.first == state.currentTab }.takeIf { it >= 0 } ?: 0,
                containerColor = Color.White,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    val selectedIndex = tabs.indexOfFirst { it.first == state.currentTab }.takeIf { it >= 0 } ?: 0
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = scheme.primary,
                        height = 3.dp
                    )
                },
                divider = { Divider(color = Color(0xFFEEEEEE)) }
            ) {
                tabs.forEach { (status, label) ->
                    Tab(
                        selected = state.currentTab == status,
                        onClick = { viewModel.load(status) },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (state.currentTab == status) FontWeight.Bold else FontWeight.Normal,
                                color = if (state.currentTab == status) scheme.primary else Color(0xFF666666)
                            )
                        }
                    )
                }
            }

            if (state.loading) {
                LoadingBox(Modifier.fillMaxSize())
            } else if (state.reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无举报记录", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.reports) { report ->
                        ReportTicketCard(
                            report = report,
                            onClick = { onReportClick(report.id, report.targetId, report.targetType) }
                        )
                    }
                }
            }
        } else {
            ResourceGovernanceSection()
        }
    }
}
}

@Composable
private fun ResourceGovernanceSection() {
    var searchText by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("搜索违规帖子标题 / 内容", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "搜索", tint = Color.Gray)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF4F5F7),
                    unfocusedContainerColor = Color(0xFFF4F5F7),
                    disabledContainerColor = Color(0xFFF4F5F7),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                )
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFFCBD5E1))
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (searchText.isEmpty()) "输入关键词全站检索异常帖子" else "未找到与 \"${searchText}\" 相关的资源", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ReportTicketCard(
    report: Report,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (report.targetType == "POST") Color(0xFFE3F2FD) else Color(0xFFF3E5F5),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (report.targetType == "POST") "帖子违规" else "用户违规",
                        fontSize = 11.sp,
                        color = if (report.targetType == "POST") Color(0xFF1565C0) else Color(0xFF6A1B9A)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = report.createdAt.replace("T", " ").take(16),
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = when (report.status) {
                        "PENDING" -> "待处理"
                        "RESOLVED" -> "已处理"
                        else -> "已忽略"
                    },
                    fontSize = 12.sp,
                    color = when (report.status) {
                        "PENDING" -> Color(0xFFEF6C00)
                        "RESOLVED" -> Color(0xFF2E7D32)
                        else -> Color(0xFF999999)
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "举报对象：${report.targetTitle ?: "未知"}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "举报理由：${report.reason}",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                NetworkImage(
                    url = report.reporter?.avatar,
                    contentDescription = "举报人",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "举报人：${report.reporter?.nickname ?: "匿名"} (${report.reporter?.phone ?: ""})",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}
