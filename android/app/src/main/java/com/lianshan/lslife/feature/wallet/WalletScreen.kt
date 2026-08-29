package com.qingyuan.lslife.feature.wallet

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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qingyuan.lslife.R
import com.qingyuan.lslife.core.model.RechargePackage
import com.qingyuan.lslife.core.model.WalletLog
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.components.PaymentBottomSheet
import kotlinx.coroutines.launch

// Google Primary Colors
private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)

// Google Tonal Backgrounds
private val GoogleBlueLight = Color(0xFFE8F0FE)
private val GoogleRedLight = Color(0xFFFCE8E6)
private val GoogleYellowLight = Color(0xFFFEF7E0)
private val GoogleGreenLight = Color(0xFFE6F4EA)

private val GoogleGreyBorder = Color(0xFFDADCE0)
private val GoogleTextPrimary = Color(0xFF202124)
private val GoogleTextSecondary = Color(0xFF5F6368)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val virtualCoinName = stringResource(id = R.string.virtual_coin_name)
    val snackbarHostState = remember { SnackbarHostState() }

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
        containerColor = MaterialTheme.colorScheme.background, // Google App default background
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "我的钱包",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
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
                            Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "账单明细",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (selectedPackage != null) {
                Surface(
                    shadowElevation = 8.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "实付: ",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "¥${"%.2f".format(selectedPackage.price)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoogleRed
                                )
                            }
                            Text(
                                "到账 ${selectedPackage.coinsAmount + selectedPackage.bonusCoins} $virtualCoinName",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showPaymentSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                "立即充值",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Card - Asset Overview
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(GoogleBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = GoogleBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$virtualCoinName 当前余额",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "1 $virtualCoinName = 1.00元",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            AnimatedContent(targetState = uiState.coinBalance, label = "") { balance ->
                                Text(
                                    text = balance.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoogleBlue
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = virtualCoinName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = GoogleBlue,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        
                        // Compact Safe banner
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Icon(
                                Icons.Outlined.Security,
                                contentDescription = null,
                                tint = GoogleGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                "SSL 资金加密保障",
                                fontSize = 10.sp,
                                color = GoogleGreen
                            )
                        }
                    }
                }
            }

            // 3 Features - Compact inline layout
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WalletFeatureItemCompact(icon = Icons.Outlined.Storefront, iconTint = GoogleBlue, title = "全场景通用")
                WalletFeatureItemCompact(icon = Icons.Outlined.Bolt, iconTint = GoogleYellow, title = "极速到账")
                WalletFeatureItemCompact(icon = Icons.AutoMirrored.Outlined.ReceiptLong, iconTint = GoogleGreen, title = "透明对账")
            }

            // Recharge Packages
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "选择充值套餐",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "充值档位越多赠送越多",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val chunkedPackages = uiState.packages.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunkedPackages.forEach { rowPackages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPackages.forEach { pkg ->
                            val isSelected = uiState.selectedPackageId == pkg.id
                            val discount = (pkg.coinsAmount - pkg.price).toInt()
                            val badge = when {
                                pkg.bonusCoins > 0 -> "赠 ${pkg.bonusCoins}"
                                discount > 0 -> "省 ${discount} 元"
                                pkg.coinsAmount >= 100 -> "热销"
                                else -> null
                            }

                            GooglePackageCard(
                                modifier = Modifier.weight(1f),
                                pkg = pkg,
                                virtualCoinName = virtualCoinName,
                                isSelected = isSelected,
                                badge = badge,
                                onClick = { viewModel.selectPackage(pkg.id) }
                            )
                        }
                        if (rowPackages.size < 3) {
                            repeat(3 - rowPackages.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Information (Compact)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "充值须知与说明",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "1. 充值成功的清远币将存入个人账户中，永久有效，不设过期时间。\n2. 可用于购买同城商品、置顶推广、实物配送及增值服务。\n3. 如遇支付异常，可点击右上角【账单明细】核对或联系客服。",
                    fontSize = 10.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))
        }

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

@Composable
private fun WalletFeatureItemCompact(
    icon: ImageVector,
    iconTint: Color,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GooglePackageCard(
    modifier: Modifier = Modifier,
    pkg: RechargePackage,
    virtualCoinName: String,
    isSelected: Boolean,
    badge: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GoogleBlueLight else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) GoogleBlue else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${pkg.coinsAmount}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) GoogleBlue else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = virtualCoinName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (pkg.bonusCoins > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "+ 赠 ${pkg.bonusCoins}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoogleRed
                    )
                } else {
                    Spacer(Modifier.height(2.dp))
                    Text(text = "", fontSize = 9.sp) // placeholder for alignment
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "¥${"%.2f".format(pkg.price)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, topEnd = 12.dp))
                        .background(GoogleRed)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

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
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoogleBlue, modifier = Modifier.size(24.dp))
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
                            Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "暂无交易明细记录",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        val isPositive = log.amount > 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formatTradeType(log.tradeType),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = log.createdAt.take(16).replace("T", " "),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isPositive) "+${log.amount}" else "${log.amount}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) GoogleGreen else GoogleRed
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "结余: ${log.balanceAfter} $virtualCoinName",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
