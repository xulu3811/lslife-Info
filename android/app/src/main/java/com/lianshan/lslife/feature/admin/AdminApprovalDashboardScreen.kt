package com.qingyuan.lslife.feature.admin

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
import com.qingyuan.lslife.core.network.ApiService
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
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF3F5F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "审批业务分类",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 12.dp)
            )

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    M3ApprovalMenuRow(
                        icon = Icons.Outlined.PendingActions,
                        title = "商品/服务上架与修改",
                        count = state.pendingPosts,
                        onClick = onOpenPostReview,
                        showDivider = true,
                        iconTint = Color(0xFFFBBC05),
                        iconBg = Color(0xFFFEF7E0)
                    )
                    M3ApprovalMenuRow(
                        icon = Icons.Outlined.AssignmentInd,
                        title = "用户个人信息修改",
                        count = state.pendingProfile,
                        onClick = onOpenProfileReview,
                        showDivider = true,
                        iconTint = Color(0xFF1A73E8),
                        iconBg = Color(0xFFE8F0FE)
                    )
                    M3ApprovalMenuRow(
                        icon = Icons.Outlined.HowToReg,
                        title = "个人实名认证",
                        count = state.pendingKyc,
                        onClick = onOpenKycReview,
                        showDivider = true,
                        iconTint = Color(0xFF1A73E8),
                        iconBg = Color(0xFFE8F0FE)
                    )
                    M3ApprovalMenuRow(
                        icon = Icons.Outlined.Storefront,
                        title = "商家入驻/店铺认证",
                        count = state.pendingMerchant,
                        onClick = onOpenMerchantCertReview,
                        showDivider = false,
                        iconTint = Color(0xFF34A853),
                        iconBg = Color(0xFFE6F4EA)
                    )
                }
            }
        }
    }
}

@Composable
private fun M3ApprovalMenuRow(
    icon: ImageVector,
    title: String,
    count: Int,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    iconTint: Color,
    iconBg: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(modifier = Modifier.size(34.dp), shape = CircleShape, color = iconBg) {
                Box(contentAlignment = Alignment.Center) { 
                    Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(18.dp)) 
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                modifier = Modifier.weight(1f)
            )

            if (count > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFDC2626), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${count}条待审",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            } else {
                Text(
                    text = "无待审",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp, end = 20.dp), color = Color(0xFFF3F4F6))
        }
    }
}
