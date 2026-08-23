package com.lianshan.lslife.feature.wallet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.R
import com.lianshan.lslife.core.model.RechargePackage
import com.lianshan.lslife.core.model.WalletLog
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.PaymentBottomSheet
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.PrimaryRed
import com.lianshan.lslife.ui.theme.Dimens
import kotlinx.coroutines.launch

/**
 * 我的钱包页面 (Joybuy 欧美简约风格 3D Soft UI 重构版)
 * 遵循规范:
 * 1. 12dp/14dp 优雅圆角与 0.5dp 细致高亮线框
 * 2. 18dp 纯净线性矢量图标与清爽浅色微容器
 * 3. 资产卡片 + 3 栏服务保障 + 充值套餐微选择矩阵 + 吸底结算栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val virtualCoinName = stringResource(id = R.string.virtual_coin_name)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showPaymentSheet by remember { mutableStateOf(false) }
    var showLogsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val selectedPackage = uiState.packages.find { it.id == uiState.selectedPackageId } 
        ?: uiState.packages.firstOrNull()

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "我的钱包",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.loadLogs()
                            showLogsSheet = true
                        }
                    ) {
                        Icon(
                            Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "账单明细",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (selectedPackage != null) {
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
                                    "¥${"%.2f".format(selectedPackage.price)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryRed
                                )
                            }
                            Text(
                                "到账 ${selectedPackage.coinsAmount + selectedPackage.bonusCoins} $virtualCoinName",
                                fontSize = 10.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = { showPaymentSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Text(
                                "立即充值",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.packages.isEmpty()) {
            LoadingBox(Modifier.padding(paddingValues).fillMaxSize())
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 一、 Joybuy 3D Soft UI 资产总览卡
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF2F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "$virtualCoinName 当前余额",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }

                        Text(
                            "1 $virtualCoinName = 1.00元",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        AnimatedContent(targetState = uiState.coinBalance, label = "") { balance ->
                            Text(
                                text = balance.toString(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = virtualCoinName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // 底部安全通道微胶囊
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Security,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "银行级 SSL 资金加密保障 · 实时入账 · 全平台通用",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // 二、 3 栏服务与保障矩阵 (Joybuy Micro Features)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WalletFeatureItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Storefront,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = "全场景通用",
                    desc = "商品购买与推广"
                )
                WalletFeatureItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Bolt,
                    iconBg = Color(0xFFF0FDF4),
                    iconTint = Color(0xFF16A34A),
                    title = "极速到账",
                    desc = "充值完成秒级上屏"
                )
                WalletFeatureItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.ReceiptLong,
                    iconBg = Color(0xFFFFFBEB),
                    iconTint = Color(0xFFD97706),
                    title = "透明对账",
                    desc = "每笔流水精准记录"
                )
            }

            // 三、 充值套餐选择区 (Joybuy 2 栏微卡片网格)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "选择充值套餐",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(start = 2.dp)
                )
                Text(
                    "充值档位越多赠送越多",
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // 套餐网格展示
            val chunkedPackages = uiState.packages.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                chunkedPackages.forEach { rowPackages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowPackages.forEach { pkg ->
                            val isSelected = uiState.selectedPackageId == pkg.id
                            val discount = (pkg.coinsAmount - pkg.price).toInt()
                            val badge = when {
                                pkg.bonusCoins > 0 -> "赠 ${pkg.bonusCoins} 币"
                                discount > 0 -> "省 ${discount} 元"
                                pkg.coinsAmount >= 100 -> "热销"
                                else -> null
                            }

                            JoybuyPackageCard(
                                modifier = Modifier.weight(1f),
                                pkg = pkg,
                                virtualCoinName = virtualCoinName,
                                isSelected = isSelected,
                                badge = badge,
                                onClick = { viewModel.selectPackage(pkg.id) }
                            )
                        }
                        if (rowPackages.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // 四、 充值说明与协议保障
            Spacer(Modifier.height(4.dp))
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "充值须知与说明",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        "1. 充值成功的连山币将存入个人账户中，永久有效，不设过期时间。",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        "2. 连山币可用于购买同城商品、置顶推广、实物配送及开通平台增值服务。",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        "3. 如遇充值未到账或支付异常，可点击右上角【账单明细】核对或联系客服协助处理。",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // 支付渠道选择拉起面板
        if (showPaymentSheet && selectedPackage != null) {
            PaymentBottomSheet(
                amount = selectedPackage.price,
                onDismissRequest = { showPaymentSheet = false },
                onPaymentMethodSelected = { _ ->
                    showPaymentSheet = false
                    viewModel.recharge()
                }
            )
        }

        // 账单明细 BottomSheet
        if (showLogsSheet) {
            WalletLogsBottomSheet(
                logs = uiState.logs,
                isLoading = uiState.isLoadingLogs,
                virtualCoinName = virtualCoinName,
                onDismiss = { showLogsSheet = false }
            )
        }
    }
}

/**
 * Joybuy 极简服务微卡片
 */
@Composable
private fun WalletFeatureItem(
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
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = desc,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Joybuy 欧美极简充值套餐卡片
 */
@Composable
private fun JoybuyPackageCard(
    modifier: Modifier = Modifier,
    pkg: RechargePackage,
    virtualCoinName: String,
    isSelected: Boolean,
    badge: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFFEF2F2) else Color.White)
            .border(
                BorderStroke(
                    if (isSelected) 1.5.dp else 0.5.dp,
                    if (isSelected) PrimaryRed else Color(0xFFE2E8F0)
                ),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 10.dp)
    ) {
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(topStart = 0.dp, bottomStart = 6.dp, topEnd = 8.dp, bottomEnd = 0.dp))
                    .background(PrimaryRed)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${pkg.coinsAmount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) PrimaryRed else Color(0xFF1E293B)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = virtualCoinName,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) PrimaryRed else Color(0xFF64748B)
                )
            }

            if (pkg.bonusCoins > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "+ 送 ${pkg.bonusCoins} $virtualCoinName",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryRed
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "¥${"%.2f".format(pkg.price)}",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) PrimaryRed else Color(0xFF64748B)
            )
        }
    }
}

/**
 * 账单明细 BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletLogsBottomSheet(
    logs: List<WalletLog>,
    isLoading: Boolean,
    virtualCoinName: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "账单明细记录",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "关闭",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed, modifier = Modifier.size(28.dp))
                }
            } else if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "暂无交易明细记录",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(logs) { log ->
                        val isPositive = log.amount > 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formatTradeType(log.tradeType),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = log.createdAt.take(16).replace("T", " "),
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isPositive) "+${log.amount}" else "${log.amount}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) Color(0xFF16A34A) else PrimaryRed
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "结余: ${log.balanceAfter} $virtualCoinName",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTradeType(type: String): String = when (type.lowercase()) {
    "recharge" -> "在线充值"
    "promotion" -> "推广购买"
    "publish" -> "信息发布"
    "order" -> "商品消费"
    else -> type
}
