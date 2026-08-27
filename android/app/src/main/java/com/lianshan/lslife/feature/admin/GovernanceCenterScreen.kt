package com.lianshan.lslife.feature.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.GovernancePostDto
import com.lianshan.lslife.core.network.GovernanceUserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GovernanceActionType {
    BAN_USER, REVOKE_POST, REVOKE_KYC, REVOKE_MERCHANT, NONE
}

data class GovernanceState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val targetId: String = "", 
    val reason: String = "",
    val activeDialogAction: GovernanceActionType = GovernanceActionType.NONE,
    val userSearchQuery: String = "",
    val postSearchQuery: String = "",
    val userResults: List<GovernanceUserDto> = emptyList(),
    val postResults: List<GovernancePostDto> = emptyList()
)

@HiltViewModel
class GovernanceViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(GovernanceState())
    val state: StateFlow<GovernanceState> = _state.asStateFlow()

    fun updateReason(reason: String) {
        _state.value = _state.value.copy(reason = reason)
    }

    fun setDialogAction(action: GovernanceActionType, targetId: String) {
        _state.value = _state.value.copy(activeDialogAction = action, targetId = targetId, reason = "")
    }

    fun dismissDialog() {
        _state.value = _state.value.copy(activeDialogAction = GovernanceActionType.NONE, targetId = "", reason = "")
    }

    fun updateUserSearchQuery(query: String) {
        _state.value = _state.value.copy(userSearchQuery = query)
    }

    fun updatePostSearchQuery(query: String) {
        _state.value = _state.value.copy(postSearchQuery = query)
    }

    fun searchUsers(onError: (String) -> Unit) {
        val query = _state.value.userSearchQuery
        if (query.isBlank()) return
        
        _state.value = _state.value.copy(isSearching = true)
        viewModelScope.launch {
            try {
                val res = api.searchGovernanceUsers(query)
                if (res.code == 0) {
                    _state.value = _state.value.copy(userResults = res.data ?: emptyList())
                } else {
                    onError(res.message ?: "搜索失败")
                }
            } catch (e: Exception) {
                onError("网络异常: ${e.message}")
            } finally {
                _state.value = _state.value.copy(isSearching = false)
            }
        }
    }

    fun searchPosts(onError: (String) -> Unit) {
        val query = _state.value.postSearchQuery
        if (query.isBlank()) return
        
        _state.value = _state.value.copy(isSearching = true)
        viewModelScope.launch {
            try {
                val res = api.searchGovernancePosts(query)
                if (res.code == 0) {
                    _state.value = _state.value.copy(postResults = res.data ?: emptyList())
                } else {
                    onError(res.message ?: "搜索失败")
                }
            } catch (e: Exception) {
                onError("网络异常: ${e.message}")
            } finally {
                _state.value = _state.value.copy(isSearching = false)
            }
        }
    }

    fun executeAction(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val action = _state.value.activeDialogAction
        val id = _state.value.targetId
        val reason = _state.value.reason

        if (id.isBlank()) {
            onError("无效的目标 ID")
            return
        }
        if (reason.isBlank()) {
            onError("必须填写违规原因")
            return
        }

        _state.value = _state.value.copy(isLoading = true, activeDialogAction = GovernanceActionType.NONE)
        
        viewModelScope.launch {
            try {
                val res = when (action) {
                    GovernanceActionType.BAN_USER -> api.banUser(mapOf("targetUserId" to id, "reason" to reason))
                    GovernanceActionType.REVOKE_POST -> api.revokePost(mapOf("targetPostId" to id, "reason" to reason))
                    GovernanceActionType.REVOKE_KYC -> api.revokeKyc(mapOf("targetUserId" to id, "reason" to reason))
                    GovernanceActionType.REVOKE_MERCHANT -> api.revokeMerchant(mapOf("targetUserId" to id, "reason" to reason))
                    GovernanceActionType.NONE -> throw Exception("Invalid action")
                }
                
                if (res.code == 0) {
                    onSuccess("操作成功！")
                    // Refresh current search results
                    if (_state.value.userSearchQuery.isNotBlank()) searchUsers(onError)
                    if (_state.value.postSearchQuery.isNotBlank()) searchPosts(onError)
                } else {
                    onError(res.message ?: "操作失败")
                }
            } catch (e: Exception) {
                onError("网络异常: ${e.message}")
            } finally {
                _state.value = _state.value.copy(isLoading = false, targetId = "", reason = "")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovernanceCenterScreen(
    onBack: () -> Unit,
    viewModel: GovernanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("用户治理", "资源治理")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理中枢 (先查后审)", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF5F5F7) // iOS style light gray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF1A73E8),
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF1A73E8)
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                title, 
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color(0xFF1A73E8) else Color.Gray
                            ) 
                        }
                    )
                }
            }

            if (selectedTabIndex == 0) {
                UserGovernanceTab(state, viewModel, context)
            } else {
                PostGovernanceTab(state, viewModel, context)
            }
        }
    }

    if (state.activeDialogAction != GovernanceActionType.NONE) {
        GovernanceDialog(state, viewModel, context)
    }
}

@Composable
fun UserGovernanceTab(
    state: GovernanceState, 
    viewModel: GovernanceViewModel, 
    context: android.content.Context
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.userSearchQuery,
            onValueChange = { viewModel.updateUserSearchQuery(it) },
            label = { Text("搜索用户手机号 / 昵称 / UUID") },
            trailingIcon = {
                IconButton(onClick = { viewModel.searchUsers { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A73E8),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isSearching && state.userResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1A73E8))
            }
        } else if (state.userResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("输入关键词以检索用户", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.userResults) { user ->
                    UserCard(user, viewModel)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun PostGovernanceTab(
    state: GovernanceState, 
    viewModel: GovernanceViewModel, 
    context: android.content.Context
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.postSearchQuery,
            onValueChange = { viewModel.updatePostSearchQuery(it) },
            label = { Text("搜索帖子标题 / 描述 / UUID") },
            trailingIcon = {
                IconButton(onClick = { viewModel.searchPosts { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A73E8),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isSearching && state.postResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1A73E8))
            }
        } else if (state.postResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("输入关键词以检索帖子资源", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.postResults) { post ->
                    PostCard(post, viewModel)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun UserCard(user: GovernanceUserDto, viewModel: GovernanceViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = user.avatar ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${user.id}",
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF1F5F9)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.nickname ?: "未知用户", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text(text = "手机号: ${user.phone ?: "未绑定"}", fontSize = 13.sp, color = Color(0xFF64748B))
                    Text(text = "ID: ${user.id}", fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (user.status != "banned") {
                    Button(
                        onClick = { viewModel.setDialogAction(GovernanceActionType.BAN_USER, user.id) },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE8E6), contentColor = Color(0xFFEA4335)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("账号封禁", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("已被封禁", fontSize = 12.sp)
                    }
                }

                if (user.realNameStatus == "verified") {
                    Button(
                        onClick = { viewModel.setDialogAction(GovernanceActionType.REVOKE_KYC, user.id) },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF7ED), contentColor = Color(0xFFEA580C)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("撤销实名", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (user.role == "MERCHANT_VERIFIED") {
                    Button(
                        onClick = { viewModel.setDialogAction(GovernanceActionType.REVOKE_MERCHANT, user.id) },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF7ED), contentColor = Color(0xFFEA580C)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("撤销商家", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(post: GovernancePostDto, viewModel: GovernanceViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.description ?: "无描述", fontSize = 13.sp, color = Color(0xFF64748B), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "发布者: ${post.user?.nickname ?: "未知"} (${post.user?.phone ?: "-"})", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "ID: ${post.id}", fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                
                if (post.status == "published") {
                    Button(
                        onClick = { viewModel.setDialogAction(GovernanceActionType.REVOKE_POST, post.id) },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE8E6), contentColor = Color(0xFFEA4335)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("强制下架", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Text(
                        text = "状态: ${post.status}",
                        fontSize = 12.sp,
                        color = Color(0xFFEA4335),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.background(Color(0xFFFCE8E6), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GovernanceDialog(
    state: GovernanceState, 
    viewModel: GovernanceViewModel, 
    context: android.content.Context
) {
    val (dialogTitle, dialogWarning) = when (state.activeDialogAction) {
        GovernanceActionType.BAN_USER -> "封禁用户账号" to "即将永久封禁该用户，并级联下架其所有在展商品和帖子。该操作不可撤销！"
        GovernanceActionType.REVOKE_POST -> "强制下架资源" to "即将强制下架指定帖子/商品，用户将无法再展示该资源。"
        GovernanceActionType.REVOKE_KYC -> "撤销实名认证" to "即将撤销该用户的实名身份信息。"
        GovernanceActionType.REVOKE_MERCHANT -> "注销商家认证" to "即将剥离该用户的商家权限并注销店铺信息。"
        else -> "" to ""
    }

    AlertDialog(
        onDismissRequest = { viewModel.dismissDialog() },
        title = {
            Text(
                text = "二次确认：$dialogTitle", 
                color = Color(0xFF1A73E8),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = dialogWarning,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.reason,
                    onValueChange = { viewModel.updateReason(it) },
                    label = { Text("必填：违规原因 / 备注") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A73E8),
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.executeAction(
                        onSuccess = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = state.reason.isNotBlank() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("确认执行")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.dismissDialog() }
            ) {
                Text("取消", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}
