package com.lianshan.lslife.feature.admin

import kotlinx.serialization.json.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val pendingPosts: Int = 0,
    val pendingProfile: Int = 0,
    val pendingKyc: Int = 0,
    val pendingMerchant: Int = 0,
    val loading: Boolean = true
)

@HiltViewModel
class AdminApprovalDashboardViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val res = api.getAdminDashboard()
                if (res.code == 0) {
                    val data = res.data
                    _state.update {
                        it.copy(
                            pendingPosts = data?.get("pendingReviews")?.let { try { it.jsonPrimitive.content.toIntOrNull() } catch (e: Exception) { null } } ?: 0,
                            pendingProfile = data?.get("pendingProfileReviews")?.let { try { it.jsonPrimitive.content.toIntOrNull() } catch (e: Exception) { null } } ?: 0,
                            pendingKyc = data?.get("pendingKyc")?.let { try { it.jsonPrimitive.content.toIntOrNull() } catch (e: Exception) { null } } ?: 0,
                            pendingMerchant = data?.get("pendingMerchantCerts")?.let { try { it.jsonPrimitive.content.toIntOrNull() } catch (e: Exception) { null } } ?: 0,
                            loading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApprovalDashboardScreen(
    onBack: () -> Unit,
    onOpenPostReview: () -> Unit,
    onOpenProfileReview: () -> Unit,
    onOpenKycReview: () -> Unit,
    onOpenMerchantCertReview: () -> Unit,
    viewModel: AdminApprovalDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "待审批事项", 
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 分组标题
            Text(
                text = "审批业务分类",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5F6368),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp)
            )

            // 聚合卡片 (对标“我的”页面统一样式)
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE8EAED)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ApprovalMenuRow(
                        icon = Icons.Outlined.PendingActions,
                        title = "商品/服务上架与修改",
                        count = state.pendingPosts,
                        onClick = onOpenPostReview,
                        showDivider = true,
                        iconTint = Color(0xFFFBBC05) // Google Yellow
                    )
                    ApprovalMenuRow(
                        icon = Icons.Outlined.AssignmentInd,
                        title = "用户个人信息修改",
                        count = state.pendingProfile,
                        onClick = onOpenProfileReview,
                        showDivider = true,
                        iconTint = Color(0xFF1A73E8) // Google Blue
                    )
                    ApprovalMenuRow(
                        icon = Icons.Outlined.HowToReg,
                        title = "个人实名认证",
                        count = state.pendingKyc,
                        onClick = onOpenKycReview,
                        showDivider = true,
                        iconTint = Color(0xFF1A73E8) // Google Blue
                    )
                    ApprovalMenuRow(
                        icon = Icons.Outlined.Storefront,
                        title = "商家入驻/店铺认证",
                        count = state.pendingMerchant,
                        onClick = onOpenMerchantCertReview,
                        showDivider = false,
                        iconTint = Color(0xFF34A853) // Google Green
                    )
                }
            }
        }
    }
}

@Composable
private fun ApprovalMenuRow(
    icon: ImageVector,
    title: String,
    count: Int,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            if (count > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFECEB),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(Color(0xFF1A73E8), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${count}条待审",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A73E8)
                        )
                    }
                }
            } else {
                Text(
                    text = "无待审",
                    fontSize = 12.sp,
                    color = Color(0xFF5F6368),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFCCCCCC),
                modifier = Modifier.size(16.dp)
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 46.dp, end = 16.dp)
                    .height(0.5.dp)
                    .background(Color(0xFFF2F2F2)),
            )
        }
    }
}
