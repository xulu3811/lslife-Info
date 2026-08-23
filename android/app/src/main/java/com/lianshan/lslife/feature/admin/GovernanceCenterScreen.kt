package com.lianshan.lslife.feature.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GovernanceState(
    val isLoading: Boolean = false,
    val targetUserId: String = "",
    val reason: String = "",
    val showConfirmDialog: Boolean = false
)

@HiltViewModel
class GovernanceViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(GovernanceState())
    val state: StateFlow<GovernanceState> = _state.asStateFlow()

    fun updateTargetUserId(id: String) {
        _state.value = _state.value.copy(targetUserId = id)
    }

    fun updateReason(reason: String) {
        _state.value = _state.value.copy(reason = reason)
    }

    fun setShowConfirmDialog(show: Boolean) {
        _state.value = _state.value.copy(showConfirmDialog = show)
    }

    fun banUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_state.value.targetUserId.isBlank()) {
            onError("请输入目标用户 ID")
            return
        }
        if (_state.value.reason.isBlank()) {
            onError("必须填写违规原因")
            return
        }
        _state.value = _state.value.copy(isLoading = true, showConfirmDialog = false)
        viewModelScope.launch {
            try {
                val res = api.banUser(
                    mapOf(
                        "targetUserId" to _state.value.targetUserId,
                        "reason" to _state.value.reason
                    )
                )
                if (res.code == 0) {
                    onSuccess()
                } else {
                    onError(res.message ?: "操作失败")
                }
            } catch (e: Exception) {
                onError("网络异常: ${e.message}")
            } finally {
                _state.value = _state.value.copy(isLoading = false, targetUserId = "", reason = "")
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理中枢", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(0.5.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "数据风控与治理",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "执行强制下架、封禁账号等高危操作。\n请谨慎核对目标 ID 与违规事实。",
                        fontSize = 13.5.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = state.targetUserId,
                        onValueChange = { viewModel.updateTargetUserId(it) },
                        label = { Text("目标用户 ID (User ID)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.setShowConfirmDialog(true) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFF0F0),
                            contentColor = Color(0xFFE53935)
                        ),
                        enabled = state.targetUserId.isNotBlank() && !state.isLoading
                    ) {
                        Text(if (state.isLoading) "执行中..." else "强制封禁并下架资源")
                    }
                }
            }
        }
    }

    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowConfirmDialog(false) },
            title = {
                Text(
                    text = "二次确认：高危操作", 
                    color = Color(0xFFE53935),
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "即将永久封禁该用户，并级联下架其所有在展商品和帖子。该操作不可撤销。",
                        fontSize = 13.5.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.reason,
                        onValueChange = { viewModel.updateReason(it) },
                        label = { Text("必填：违规原因") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE53935),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.banUser(
                            onSuccess = {
                                Toast.makeText(context, "执行成功：已封禁并下架", Toast.LENGTH_SHORT).show()
                            },
                            onError = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state.reason.isNotBlank()
                ) {
                    Text("确认执行")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.setShowConfirmDialog(false) }
                ) {
                    Text("取消", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
