package com.qingyuan.lslife.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qingyuan.lslife.core.model.MembershipPlan
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.components.PaymentBottomSheet
import com.qingyuan.lslife.ui.components.NetworkImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var selectedPlanForPayment by remember { mutableStateOf<MembershipPlan?>(null) }
    val user = state.user

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val isMerchant = user?.role?.contains("MERCHANT") == true || state.merchantCertStatus == "APPROVED"
    val isSuperAdmin = user?.role == "SUPERADMIN" || user?.role == "ADMIN"

    val merchantPlans = remember {
        listOf(
            MembershipPlan(
                tier = "merchant_vip_month",
                name = "金牌商家包月",
                price = 68.0,
                period = "月",
                benefits = listOf("每月50条免费发布额度", "AI智能文案无限次润色", "商家专属尊贵标识", "每月专享 15 张曝光卡")
            ),
            MembershipPlan(
                tier = "merchant_vip_quarter",
                name = "金牌商家包季",
                price = 168.0,
                period = "季",
                benefits = listOf("每月50条免费发布额度", "AI智能文案无限次润色", "商家专属尊贵标识", "每月专享 20 张曝光卡", "立省36元")
            )
        )
    }

    val personalPlans = remember {
        listOf(
            MembershipPlan(
                tier = "vip",
                name = "VIP会员",
                price = 19.9,
                period = "月",
                benefits = listOf("专属VIP尊贵标识", "发布信息免审核优先", "每月专享 30 张曝光卡")
            ),
            MembershipPlan(
                tier = "premium",
                name = "至尊会员",
                price = 39.9,
                period = "月",
                benefits = listOf("专属至尊尊贵标识", "发布信息免审核优先", "专属客服极速响应", "每月专享 100 张曝光卡")
            )
        )
    }

    val currentPlans = if (isMerchant && !isSuperAdmin) merchantPlans else personalPlans
    var activePlan by remember(isMerchant) { mutableStateOf(currentPlans.first()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("超级会员", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("总计: ", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "¥${activePlan.price}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "开通即享全套特权",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { selectedPlanForPayment = activePlan },
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                    ) {
                        Text("立即开通", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        if (state.loading && state.plans.isEmpty()) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Header User Info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NetworkImage(
                        url = user?.avatar,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user?.nickname ?: "未命名用户",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val currentTierName = when(user?.membershipTier) {
                            "vip" -> "VIP会员"
                            "premium" -> "至尊会员"
                            "merchant_vip_month", "merchant_vip_quarter" -> "金牌商家"
                            else -> "普通用户"
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "当前身份: $currentTierName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Plan Selection
            Column {
                Text(
                    text = "选择您的会员套餐",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentPlans.forEach { plan ->
                        val isSelected = activePlan.tier == plan.tier
                        M3PlanCard(
                            modifier = Modifier.weight(1f),
                            plan = plan,
                            isSelected = isSelected,
                            onClick = { activePlan = plan }
                        )
                    }
                }
            }

            // Benefits Details
            Column {
                Text(
                    text = "尊享 ${activePlan.name} 特权",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        activePlan.benefits.forEach { benefit ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Check",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = benefit,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        selectedPlanForPayment?.let { plan ->
            PaymentBottomSheet(
                amount = plan.price,
                onDismissRequest = { selectedPlanForPayment = null },
                onPaymentMethodSelected = { _ ->
                    selectedPlanForPayment = null
                    viewModel.subscribe(plan.tier)
                }
            )
        }
    }
}

@Composable
private fun M3PlanCard(
    modifier: Modifier = Modifier,
    plan: MembershipPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = plan.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${plan.price}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/${plan.period}",
                    fontSize = 12.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
