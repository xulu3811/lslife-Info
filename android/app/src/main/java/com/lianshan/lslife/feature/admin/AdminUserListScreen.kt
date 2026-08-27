package com.lianshan.lslife.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.AdminUserListItem
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserListScreen(
    onBack: () -> Unit,
    viewModel: AdminUserListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val snackbar = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var searchText by remember { mutableStateOf("") }
    
    var selectedUser by remember { mutableStateOf<AdminUserListItem?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.load(refresh = true)
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
                title = { Text("用户管理", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("搜索手机号或昵称", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "搜索", tint = Color.Gray)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F3F4),
                        unfocusedContainerColor = Color(0xFFF1F3F4),
                        disabledContainerColor = Color(0xFFF1F3F4),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.search(searchText)
                        }
                    )
                )
            }

            if (state.loading && state.items.isEmpty()) {
                LoadingBox(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.items.isEmpty()) {
                        item {
                                EmptyState(
                                title = "未找到用户",
                                subtitle = "换个关键词搜搜看吧~",
                                icon = Icons.Filled.PersonSearch,
                                modifier = Modifier.fillMaxWidth().height(400.dp)
                            )
                        }
                    } else {
                        items(state.items) { user ->
                            AdminUserCard(
                                user = user,
                                onActionClick = {
                                    selectedUser = user
                                    showBottomSheet = true
                                }
                            )
                        }
                    }
                    if (state.loading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    } else if (state.hasMore && state.items.isNotEmpty()) {
                        item {
                            LaunchedEffect(Unit) {
                                viewModel.load()
                            }
                        }
                    }
                }
            }
        }
        
        if (showBottomSheet && selectedUser != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "操作用户: ${selectedUser?.nickname}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Divider(color = Color(0xFFF1F3F4))
                    
                    val statusOptions = listOf(
                        "active" to "恢复正常",
                        "muted" to "禁言 (Mute)",
                        "banned" to "永久封禁 (Ban)"
                    )
                    
                    statusOptions.forEach { (statusValue, label) ->
                        if (selectedUser?.status != statusValue) {
                            Text(
                                text = label,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                showBottomSheet = false
                                                viewModel.updateUserStatus(selectedUser!!.id, statusValue)
                                            }
                                        }
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: AdminUserListItem,
    onActionClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE8EAED)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                url = user.avatar,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F3F4))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nickname,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "手机号: ${user.phone}",
                    fontSize = 13.sp,
                    color = Color(0xFF5F6368)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (user.status) {
                                    "active" -> Color(0xFFE6F4EA)
                                    "muted" -> Color(0xFFFEF7E0)
                                    else -> Color(0xFFFCE8E6)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (user.status) {
                                "active" -> "正常"
                                "muted" -> "已禁言"
                                else -> "已封禁"
                            },
                            fontSize = 11.sp,
                            color = when (user.status) {
                                "active" -> Color(0xFF34A853)
                                "muted" -> Color(0xFFFBBC05)
                                else -> Color(0xFFEA4335)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "注册: ${user.createdAt.take(10)}",
                        fontSize = 11.sp,
                        color = Color(0xFF5F6368)
                    )
                }
            }
            IconButton(onClick = onActionClick) {
                Icon(Icons.Filled.MoreVert, contentDescription = "操作", tint = Color(0xFF5F6368))
            }
        }
    }
}
