package com.lianshan.lslife.feature.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.network.ApiService
import com.lianshan.lslife.core.network.KycUser
import com.lianshan.lslife.ui.components.EmptyState
import com.lianshan.lslife.ui.components.LoadingBox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KycReviewListState(
    val loading: Boolean = true,
    val users: List<KycUser> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminKycReviewViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(KycReviewListState())
    val state: StateFlow<KycReviewListState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val res = api.getAdminKyc()
                if (res.code == 0) {
                    _state.update { it.copy(loading = false, users = res.data ?: emptyList()) }
                } else {
                    _state.update { it.copy(loading = false, error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun audit(userId: String, action: String) {
        viewModelScope.launch {
            try {
                api.auditAdminKyc(userId, mapOf("action" to action))
                load()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminKycReviewListScreen(
    onBack: () -> Unit,
    viewModel: AdminKycReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "实名认证审核", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        if (state.loading) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
        } else if (state.users.isEmpty()) {
            EmptyState(title = "暂无待审核的实名申请", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.users) { user ->
                    KycReviewCard(
                        user = user,
                        onApprove = { viewModel.audit(user.id, "approve") },
                        onReject = { viewModel.audit(user.id, "reject") }
                    )
                }
            }
        }
    }
}

@Composable
private fun KycReviewCard(
    user: KycUser,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("申请用户", fontSize = 12.sp, color = Color(0xFF888888))
            Spacer(Modifier.height(4.dp))
            Text("手机号: ${user.phone}", fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("当前昵称: ${user.nickname}", fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            Text("实名信息", fontSize = 12.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text("真实姓名: ${user.realName ?: "未提供"}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onReject,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("驳回", color = Color(0xFFD32F2F), fontSize = 12.5.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onApprove, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("通过", color = Color.White, fontSize = 12.5.sp)
                }
            }
        }
    }
}
