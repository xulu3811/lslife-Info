package com.qingyuan.lslife.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.qingyuan.lslife.core.model.Report
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.components.NetworkImage

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
    val topTabs = listOf("举报处理", "资源治理", "风控词库", "拦截记录")

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color(0xFFF3F5F8)
    ) { padding ->
                Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = topLevelTab,
                containerColor = Color.Transparent,
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
                containerColor = Color.Transparent,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    val selectedIndex = tabs.indexOfFirst { it.first == state.currentTab }.takeIf { it >= 0 } ?: 0
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = scheme.primary,
                        height = 3.dp
                    )
                },
                divider = { Divider(color = Color(0xFFF1F3F4)) }
            ) {
                tabs.forEach { (status, label) ->
                    Tab(
                        selected = state.currentTab == status,
                        onClick = { viewModel.load(status) },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (state.currentTab == status) FontWeight.Bold else FontWeight.Normal,
                                color = if (state.currentTab == status) scheme.primary else Color(0xFF5F6368)
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
        } else if (topLevelTab == 1) {
            ResourceGovernanceSection()
        } else {
            RiskDictionarySection(state = state, viewModel = viewModel)
        }
    }
}
}

@Composable
private fun ResourceGovernanceSection(
    governanceViewModel: com.qingyuan.lslife.feature.admin.GovernanceViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state by governanceViewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        com.qingyuan.lslife.feature.admin.PostGovernanceTab(state, governanceViewModel, context)
        
        if (state.activeDialogAction != com.qingyuan.lslife.feature.admin.GovernanceActionType.NONE) {
            com.qingyuan.lslife.feature.admin.GovernanceDialog(state, governanceViewModel, context)
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
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE8EAED)),
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
                            color = if (report.targetType == "POST") Color(0xFFE8F0FE) else Color(0xFFFCE8E6),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (report.targetType == "POST") "帖子违规" else "用户违规",
                        fontSize = 11.sp,
                        color = if (report.targetType == "POST") Color(0xFF1A73E8) else Color(0xFFEA4335)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = report.createdAt.replace("T", " ").take(16),
                    fontSize = 12.sp,
                    color = Color(0xFF5F6368)
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
                        "PENDING" -> Color(0xFFFBBC05)
                        "RESOLVED" -> Color(0xFF34A853)
                        else -> Color(0xFF5F6368)
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
                color = Color(0xFF5F6368),
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
                        .background(Color(0xFFF1F3F4))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "举报人：${report.reporter?.nickname ?: "匿名"} (${report.reporter?.phone ?: ""})",
                    fontSize = 12.sp,
                    color = Color(0xFF5F6368)
                )
            }
        }
    }
}
@Composable
private fun RiskDictionarySection(state: AdminReportListState, viewModel: AdminReportListViewModel) {
    LaunchedEffect(Unit) { viewModel.loadWords() }
    
    var showAddDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importWordsFromUri(context, uri)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("本地 DFA 风控词库", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { launcher.launch("text/plain") },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("导入TXT", fontSize = 13.sp)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("添加违禁词", fontSize = 13.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.wordsLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.words.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("词库为空", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.words) { word ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F3F4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(word.word, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF333333))
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val levelText = if(word.level >= 3) "严重违禁 (直拒)" else "可疑 (需人工审)"
                                    val levelColor = if(word.level >= 3) Color(0xFFEA4335) else Color(0xFFFBBC05)
                                    Text(levelText, fontSize = 12.sp, color = levelColor)
                                    Spacer(Modifier.width(8.dp))
                                    Text("添加于: ${word.createdAt.take(10)}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteWord(word.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var inputWord by remember { mutableStateOf("") }
        var inputLevel by remember { mutableIntStateOf(3) }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加风控词汇") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputWord,
                        onValueChange = { inputWord = it },
                        label = { Text("词汇内容 (例如：冰毒)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("风控等级:", fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = inputLevel == 3, onClick = { inputLevel = 3 })
                        Text("严重违禁 (命中即拦截)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = inputLevel == 2, onClick = { inputLevel = 2 })
                        Text("可疑 (转人工复核)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (inputWord.isNotBlank()) {
                        viewModel.addWord(inputWord.trim(), inputLevel)
                        showAddDialog = false
                    }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            },
            containerColor = Color.Transparent
        )
    }
}


@Composable
fun ModerationLogsSection(
    state: AdminReportListState,
    viewModel: AdminReportListViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.loadModerationLogs()
    }
    val scheme = MaterialTheme.colorScheme

    if (state.logsLoading) {
        LoadingBox(modifier = Modifier.fillMaxSize())
        return
    }

    if (state.logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("无拦截记录", color = Color.Gray, fontSize = 16.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "用户: " + (log.user?.nickname ?: "未知") + " (" + (log.user?.phone ?: "") + ")",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF3C4043)
                            )
                            val badgeColor = if (log.result == "REJECTED") Color(0xFFEA4335) else scheme.primary
                            val badgeText = if (log.result == "REJECTED") "已拦截 (Lv.3)" else "人工审核 (Lv.2)"
                            Box(
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(badgeText, color = badgeColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = log.content,
                            fontSize = 14.sp,
                            color = Color(0xFF5F6368),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Divider(color = Color(0xFFF1F3F4))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "命中违禁词: " + log.matchedWords,
                            fontSize = 13.sp,
                            color = scheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "拦截时间: " + log.createdAt.replace("T", " ").substring(0, 16),
                            fontSize = 12.sp,
                            color = Color(0xFF9AA0A6),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
