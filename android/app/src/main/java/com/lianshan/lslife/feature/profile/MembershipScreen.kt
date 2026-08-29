package com.qingyuan.lslife.feature.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qingyuan.lslife.core.model.MembershipPlan
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.components.PaymentBottomSheet
import com.qingyuan.lslife.ui.components.SoftCard
import com.qingyuan.lslife.ui.theme.PrimaryRed
import com.qingyuan.lslife.ui.theme.Dimens

/**
 * 会员权益中心 (Joybuy 欧美简约 3D Soft UI 重构版)
 * 设计规范:
 * 1. 顶部用户身份与尊享特权徽章卡
 * 2. 套餐 2 栏微选择器 (支持包月 / 包季选中高亮)
 * 3. 4 大核心特权 3D Soft UI 矩阵可视化网格
 * 4. 底部吸底结算与开通栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var selectedPlanForPayment by remember { mutableStateOf<MembershipPlan?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val isMerchant = state.user?.role?.contains("MERCHANT") == true || state.merchantCertStatus == "APPROVED"

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
                benefits = listOf("每月50条免费发布额度", "AI智能文案无限次润色", "商家专属尊贵标识", "立省36元", "每月专享 20 张曝光卡")
            )
        )
    }

    val personalPlans = remember {
        listOf(
            MembershipPlan(
                tier = "urgent_tag_1",
                name = "极速急售卡 · 1张",
                price = 2.9,
                period = "次",
                benefits = listOf("信息流列表“急售”高亮标签", "优先展示权重", "24小时有效")
            ),
            MembershipPlan(
                tier = "urgent_tag_5",
                name = "极速急售卡 · 5张",
                price = 9.9,
                period = "次",
                benefits = listOf("信息流列表“急售”高亮标签", "优先展示权重", "买5送1，更划算")
            )
        )
    }

    val currentPlans = if (isMerchant) merchantPlans else personalPlans
    var activePlan by remember(isMerchant) { mutableStateOf(currentPlans.first()) }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "会员权益",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
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
                            Text(
                                "实付: ",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                "¥${activePlan.price}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRed
                            )
                            Text(
                                " / ${activePlan.period}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        if (activePlan.tier == "merchant_vip_quarter") {
                            Text(
                                "已享包季特惠 · 立省 ¥36",
                                fontSize = 10.5.sp,
                                color = PrimaryRed,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                "开通即享全套特权与曝光卡",
                                fontSize = 10.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Button(
                        onClick = { selectedPlanForPayment = activePlan },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Text(
                            "立即开通",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 一、 用户身份与特权状态卡 (Joybuy 欧美极简 Header)
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isMerchant) Icons.Outlined.WorkspacePremium else Icons.Outlined.Person,
                                contentDescription = null,
                                tint = PrimaryRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "当前身份 · ${if (isMerchant) "认证商家" else "个人用户"}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (isMerchant) "商家专属：开通阶梯包月，畅享无限曝光与AI赋能" else "个人专属：无需包月，按需购买急售卡",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF2F2))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isMerchant) "商家认证" else "个人认证",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryRed
                        )
                    }
                }
            }

            // 二、 套餐选择卡片 (2 栏对称卡片选择器)
            Text(
                "选择开通套餐",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(start = 2.dp, top = 2.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentPlans.forEach { plan ->
                    val isSelected = activePlan.tier == plan.tier
                    val isQuarter = plan.tier == "merchant_vip_quarter" || plan.tier == "urgent_tag_5"

                    JoybuyPlanCard(
                        modifier = Modifier.weight(1f),
                        plan = plan,
                        isSelected = isSelected,
                        badge = if (isQuarter) (if (isMerchant) "立省36元" else "买5送1") else null,
                        originalPrice = if (isQuarter && isMerchant) "¥204" else null,
                        onClick = { activePlan = plan }
                    )
                }
            }

            // 三、 核心权益矩阵 (Joybuy 4 栏 Soft UI 网格)
            Text(
                "尊享特权清单",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(start = 2.dp, top = 6.dp)
            )

            if (isMerchant) {
                val isQuarterSelected = activePlan.tier == "merchant_vip_quarter"
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BenefitGridCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.PostAdd,
                            iconBg = Color(0xFFEFF6FF),
                            iconTint = Color(0xFF2563EB),
                            title = "每月50条发布额度",
                            desc = "同城各分类无限次极速发布"
                        )
                        BenefitGridCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.AutoAwesome,
                            iconBg = Color(0xFFFAF5FF),
                            iconTint = Color(0xFF9333EA),
                            title = "AI智能文案无限润色",
                            desc = "DeepSeek 深度提炼高转化文案"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BenefitGridCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.WorkspacePremium,
                            iconBg = Color(0xFFFFFBEB),
                            iconTint = Color(0xFFD97706),
                            title = "金牌商家尊贵标识",
                            desc = "全端展示专属品牌认证标识"
                        )
                        BenefitGridCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.ConfirmationNumber,
                            iconBg = Color(0xFFFEF2F2),
                            iconTint = PrimaryRed,
                            title = if (isQuarterSelected) "每月赠 20 张曝光卡" else "每月赠 15 张曝光卡",
                            desc = if (isQuarterSelected) "价值 100 元，置顶擦亮优先抵扣" else "价值 75 元，置顶擦亮优先抵扣"
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BenefitGridCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.LocalFireDepartment,
                            iconBg = Color(0xFFFEF2F2),
                            iconTint = PrimaryRed,
                            title = "急售高亮专属底色",
                            desc = "信息流第一眼抓住买家眼球"
                        )
                        BenefitGridCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.TrendingUp,
                            iconBg = Color(0xFFF0FDF4),
                            iconTint = Color(0xFF16A34A),
                            title = "搜索前排优先展示",
                            desc = "曝光权重提升 500%"
                        )
                    }
                }
            }

            // 四、 服务保障说明
            Spacer(Modifier.height(4.dp))
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "开通须知与保障",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        "1. 会员权益与附赠曝光卡在支付成功后即刻自动到账生效。",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        "2. 会员赠送的曝光卡每月 1 号凌晨自动重置刷新，月底前可随时消耗。",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        "3. 如需开具发票或有对公转账需求，请联系平台官方客服。",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
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

/**
 * Joybuy 欧美极简套餐卡片
 */
@Composable
private fun JoybuyPlanCard(
    modifier: Modifier = Modifier,
    plan: MembershipPlan,
    isSelected: Boolean,
    badge: String?,
    originalPrice: String?,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFFEF2F2) else Color.White,
        border = BorderStroke(
            if (isSelected) 1.5.dp else 0.5.dp,
            if (isSelected) PrimaryRed else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = plan.name,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) PrimaryRed else Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) PrimaryRed else Color(0xFF1E293B)
                )
                Text(
                    text = "${plan.price}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) PrimaryRed else Color(0xFF1E293B)
                )
                Text(
                    text = " / ${plan.period}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(Modifier.height(8.dp))

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PrimaryRed)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                val subText = if (plan.period == "次") {
                    val count = if (plan.tier == "urgent_tag_5") 5 else 1
                    "折合每次 ¥${"%.2f".format(plan.price / count)}"
                } else {
                    "折合每日 ¥${"%.1f".format(plan.price / 30)}"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = subText,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * 权益矩阵 3D Soft UI 卡片
 */
@Composable
private fun BenefitGridCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    desc: String
) {
    SoftCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = desc,
                fontSize = 10.5.sp,
                color = Color(0xFF64748B),
                lineHeight = 14.sp
            )
        }
    }
}
