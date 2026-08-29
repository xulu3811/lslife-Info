package com.qingyuan.lslife.feature.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.components.PrimaryButton
import com.qingyuan.lslife.ui.components.SoftCard
import com.qingyuan.lslife.ui.theme.PrimaryRed
import com.qingyuan.lslife.ui.theme.Dimens
import kotlinx.coroutines.launch

/**
 * 推广中心页面 (Joybuy 欧美简约风格 3D Soft UI 重构版)
 * 遵循规范:
 * 1. 12dp 优雅圆角、0.5dp 细致高亮线框
 * 2. 18dp 纯净线性矢量图标，摒弃厚重 Emoji 与高饱和杂色渐变
 * 3. 13.5sp / 15sp 中等字重层次排版
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionCenterScreen(
    onBack: () -> Unit,
    viewModel: PromotionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showPostSelectorFor by remember { mutableStateOf<String?>(null) } // "TOP", "BUMP", "TAG"
    var showRechargeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "推广中心",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.myPosts.isNotEmpty()) {
                Surface(
                    shadowElevation = 6.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF2F2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "帖子《${state.myPosts.first().title.take(8)}...》曝光趋缓",
                                fontSize = 12.5.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Button(
                            onClick = { showPostSelectorFor = "BUMP" },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                "一键擦亮",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading && state.stats == null) {
            LoadingBox()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                // 1. 数据概览面板 (Joybuy 欧美极简白底软卡片)
                item {
                    SoftCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // 头部说明
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Insights,
                                        contentDescription = null,
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "昨日流量概览",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                                Text(
                                    "每日 00:00 校准",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // 3 列核心指标平铺
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                MinimalStatItem("总浏览量", state.stats?.totalViews ?: 0)
                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(0.5.dp)
                                        .background(Color(0xFFE2E8F0))
                                        .align(Alignment.CenterVertically)
                                )
                                MinimalStatItem("联系意向", state.stats?.contactViews ?: 0)
                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(0.5.dp)
                                        .background(Color(0xFFE2E8F0))
                                        .align(Alignment.CenterVertically)
                                )
                                MinimalStatItem("收藏次数", state.stats?.totalFavorites ?: 0)
                            }

                            Spacer(Modifier.height(14.dp))

                            // 底部同行对比微胶囊
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.TrendingUp,
                                    contentDescription = null,
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "浏览量已超过同城 ${state.stats?.beatRate ?: 0}% 同行，开启推广最高提升 800% 曝光",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 2. 我的曝光卡资产卡片
                item {
                    SoftCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFEF2F2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = PrimaryRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            "曝光卡余额",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "${state.stats?.bumpCards ?: 0}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryRed
                                        )
                                        Text(
                                            " 张",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryRed
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "优先抵扣置顶、擦亮与急售消耗",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Button(
                                onClick = { showRechargeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    "去充值",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // 3. 核心变现工具矩阵 (Joybuy 欧美极简 3 栏网格)
                item {
                    Column {
                        Text(
                            "核心变现工具",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            JoybuyToolCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.VerticalAlignTop,
                                iconBg = Color(0xFFEFF6FF),
                                iconTint = Color(0xFF2563EB),
                                title = "同城置顶",
                                desc = "强行固定前排",
                                tag = "5 张卡 / 天",
                                onClick = { showPostSelectorFor = "TOP" }
                            )

                            JoybuyToolCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.RocketLaunch,
                                iconBg = Color(0xFFF0FDF4),
                                iconTint = Color(0xFF16A34A),
                                title = "智能擦亮",
                                desc = "瞬间回流首位",
                                tag = "1 张卡 / 次",
                                onClick = { showPostSelectorFor = "BUMP" }
                            )

                            JoybuyToolCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.LocalFireDepartment,
                                iconBg = Color(0xFFFEF2F2),
                                iconTint = Color(0xFFDC2626),
                                title = "急售/爆款",
                                desc = "专属高亮底色",
                                tag = "2 张卡 / 次",
                                onClick = { showPostSelectorFor = "TAG" }
                            )
                        }
                    }
                }

                // 4. 近期推广记录
                if (state.myTasks.isNotEmpty()) {
                    item {
                        Text(
                            "近期推广记录",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 2.dp)
                        )
                    }

                    items(state.myTasks) { task ->
                        SoftCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        task.post?.title ?: "已推广帖子",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E293B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "类型: ${formatTaskType(task.type)}",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                val isActive = task.status == "ACTIVE"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isActive) "生效中" else "已完成",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isActive) Color(0xFF16A34A) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 选帖推广 BottomSheet
    if (showPostSelectorFor != null) {
        PostSelectorBottomSheet(
            posts = state.myPosts,
            type = showPostSelectorFor!!,
            cardBalance = state.stats?.bumpCards ?: 0,
            onDismiss = { showPostSelectorFor = null },
            onRecharge = { 
                showPostSelectorFor = null
                showRechargeDialog = true 
            },
            onConfirm = { postId, days ->
                viewModel.buyPromotion(
                    postId = postId,
                    type = showPostSelectorFor!!,
                    days = days,
                    onSuccess = {
                        showPostSelectorFor = null
                        scope.launch { snackbarHostState.showSnackbar("推广开通成功！") }
                    },
                    onError = { err ->
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                )
            }
        )
    }
    
    // Joybuy 风格曝光卡充值面板
    if (showRechargeDialog) {
        RechargeCardsDialog(
            onDismiss = { showRechargeDialog = false },
            onSelectPackage = { qty ->
                viewModel.rechargeCards(
                    quantity = qty,
                    onSuccess = {
                        showRechargeDialog = false
                        scope.launch { snackbarHostState.showSnackbar("曝光卡充值成功！") }
                    },
                    onError = { err ->
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                )
            }
        )
    }
}

/**
 * 极简指标显示项
 */
@Composable
private fun MinimalStatItem(label: String, value: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        AnimatedContent(targetState = value, label = "") { targetCount ->
            Text(
                targetCount.toString(),
                color = Color(0xFF0F172A),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = Color(0xFF64748B),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * Joybuy 欧美极简变现工具卡片
 */
@Composable
private fun JoybuyToolCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    desc: String,
    tag: String,
    onClick: () -> Unit
) {
    SoftCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = desc,
                fontSize = 10.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = tag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
            }
        }
    }
}

/**
 * 选帖推广 BottomSheet (Joybuy 极简风)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostSelectorBottomSheet(
    posts: List<Post>,
    type: String,
    cardBalance: Int,
    onDismiss: () -> Unit,
    onRecharge: () -> Unit,
    onConfirm: (postId: String, days: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPostId by remember { mutableStateOf(posts.firstOrNull()?.id) }
    
    val cost = when (type) {
        "TOP" -> 5
        "TAG" -> 2
        else -> 1
    }
    
    val typeName = when (type) {
        "TOP" -> "同城置顶"
        "TAG" -> "急售/爆款"
        else -> "智能擦亮"
    }

    val isBalanceEnough = cardBalance >= cost
    
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
                    text = "选择要开通【$typeName】的帖子",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(Modifier.height(14.dp))
            
            if (posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无已发布的帖子，请先发布帖子",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts) { post ->
                        val isSelected = selectedPostId == post.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                                .border(
                                    BorderStroke(
                                        if (isSelected) 1.dp else 0.5.dp,
                                        if (isSelected) PrimaryRed else Color(0xFFE2E8F0)
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedPostId = post.id }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = post.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) PrimaryRed else Color(0xFF1E293B),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPostId = post.id },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PrimaryRed,
                                    unselectedColor = Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(18.dp))
            
            // 底部结算与确认栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "消耗: ",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "$cost",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isBalanceEnough) PrimaryRed else Color(0xFF1E293B)
                        )
                        Text(
                            text = " 张曝光卡",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Text(
                        text = "当前可用: $cardBalance 张",
                        fontSize = 11.sp,
                        color = if (!isBalanceEnough) PrimaryRed else Color(0xFF94A3B8)
                    )
                }

                if (!isBalanceEnough) {
                    Button(
                        onClick = onRecharge,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            "余额不足，去获取",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (selectedPostId != null) {
                                onConfirm(selectedPostId!!, 1)
                            }
                        },
                        enabled = selectedPostId != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            disabledContainerColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            "确认开通",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * 曝光卡套餐充值弹窗 (Joybuy 极简 3 档卡片)
 */
@Composable
fun RechargeCardsDialog(
    onDismiss: () -> Unit,
    onSelectPackage: (quantity: Int) -> Unit
) {
    var selectedQty by remember { mutableStateOf(10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(14.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AddCard,
                        contentDescription = null,
                        tint = PrimaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "充值曝光卡",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "购买后永久有效，可按需用于帖子置顶、擦亮与急售推广",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(Modifier.height(4.dp))

                RechargePackageItem(
                    qty = 1,
                    price = "¥5",
                    originPrice = null,
                    badge = null,
                    isSelected = selectedQty == 1,
                    onClick = { selectedQty = 1 }
                )

                RechargePackageItem(
                    qty = 10,
                    price = "¥45",
                    originPrice = "¥50",
                    badge = "热销 · 省5元",
                    isSelected = selectedQty == 10,
                    onClick = { selectedQty = 10 }
                )

                RechargePackageItem(
                    qty = 30,
                    price = "¥100",
                    originPrice = "¥150",
                    badge = "特惠 · 立省50元",
                    isSelected = selectedQty == 30,
                    onClick = { selectedQty = 30 }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectPackage(selectedQty) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    "立即支付并充值",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    "取消",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    )
}

@Composable
private fun RechargePackageItem(
    qty: Int,
    price: String,
    originPrice: String?,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
            .border(
                BorderStroke(
                    if (isSelected) 1.dp else 0.5.dp,
                    if (isSelected) PrimaryRed else Color(0xFFE2E8F0)
                ),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$qty 张曝光卡",
                fontSize = 13.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryRed else Color(0xFF1E293B)
            )
            if (badge != null) {
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PrimaryRed)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        badge,
                        fontSize = 9.5.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            if (originPrice != null) {
                Text(
                    originPrice,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Text(
                price,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryRed else Color(0xFF1E293B)
            )
        }
    }
}

private fun formatTaskType(type: String): String = when (type) {
    "TOP" -> "同城置顶"
    "BUMP" -> "智能擦亮"
    "TAG" -> "急售/爆款"
    else -> type
}
