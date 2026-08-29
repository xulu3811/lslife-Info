package com.qingyuan.lslife.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.qingyuan.lslife.feature.publish.CategorySchemaRegistry
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.qingyuan.lslife.ui.components.ErrorBox
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.components.UserAvatar
import com.qingyuan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PostDetailScreen(
    postId: String,
    isAdminMode: Boolean = false,
    reportId: String? = null,
    onBack: () -> Unit,
    onChatClick: (targetId: String, targetName: String) -> Unit,
    onPhoneClick: (phone: String) -> Unit,
    onPublisherClick: (publisherId: String, isMerchant: Boolean) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商品详情", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.surface,
                    titleContentColor = scheme.onSurface
                )
            )
        },
        bottomBar = {
            if (state.post != null) {
                Surface(
                    color = scheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAdminMode) {
                        var showRejectSheet by remember { mutableStateOf(false) }
                        if (showRejectSheet) {
                            ModalBottomSheet(onDismissRequest = { showRejectSheet = false }) {
                                Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                                    Text("驳回/拒绝原因", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    val reasons = listOf("包含违规词汇", "图片不清晰", "分类选择错误", "其他原因")
                                    reasons.forEach { reason ->
                                        Text(
                                            text = reason,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showRejectSheet = false
                                                    viewModel.auditAdminPost("reject", reason) { msg ->
                                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                                        onBack()
                                                    }
                                                }
                                                .padding(vertical = 12.dp)
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(horizontal = Dimens.lg, vertical = Dimens.sm)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (reportId != null) {
                                // 举报处理模式
                                OutlinedButton(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "已忽略此举报", android.widget.Toast.LENGTH_SHORT).show()
                                        onBack()
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, scheme.outline)
                                ) {
                                    Text("忽略举报", color = scheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "已确认违规并下架", android.widget.Toast.LENGTH_SHORT).show()
                                        onBack()
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                ) {
                                    Text("确认违规", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else if (isAdminMode) {
                                // 待审核模式
                                OutlinedButton(
                                    onClick = { showRejectSheet = true },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, scheme.outline)
                                ) {
                                    Text("驳回 / 拒绝", color = scheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        viewModel.auditAdminPost("approve", null) { msg ->
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                                ) {
                                    Text("审核通过", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(horizontal = Dimens.lg, vertical = Dimens.sm)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Light Info/IM Contact Mode: 收藏, 在线私聊, 拨打电话
                            OutlinedButton(
                                onClick = {
                                    viewModel.toggleFavorite { msg ->
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                val isFav = state.post?.isFavorite == true
                                val icon = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
                                val tint = if (isFav) Color.Red else scheme.onSurface
                                Icon(icon, null, modifier = Modifier.size(20.dp), tint = tint)
                            }
                            val targetId = state.post?.user?.id
                            Button(
                                onClick = {
                                    if (targetId != null && targetId != viewModel.currentUserId) {
                                        onChatClick(targetId, state.post?.user?.nickname ?: "发布者")
                                    } else if (targetId == viewModel.currentUserId) {
                                        android.widget.Toast.makeText(context, "这是您自己发布的商品/服务", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "无法获取发布者信息", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)), // Google Blue
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Filled.Chat, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("私聊", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            }
                            Button(
                                onClick = {
                                    val phone = state.post?.contactPhone
                                    if (!phone.isNullOrBlank()) {
                                        onPhoneClick(phone)
                                    } else {
                                        android.widget.Toast.makeText(context, "暂无电话联系方式", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853)), // Google Green
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Filled.Phone, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("电话", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background // Slightly gray background
    ) { padding ->
        when {
            state.loading && state.post == null -> {
                LoadingBox(modifier = Modifier.padding(padding).fillMaxSize())
            }
            state.error != null -> {
                ErrorBox(message = state.error!!, onRetry = { viewModel.loadPost(postId) }, modifier = Modifier.padding(padding))
            }
            state.post != null -> {
                val post = state.post!!
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize()
                ) {
                    // Images Carousel
                    if (post.images.isNotEmpty()) {
                        item {
                            val pagerState = rememberPagerState(pageCount = { post.images.size })
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(MaterialTheme.colorScheme.surface)) {
                                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                    AsyncImage(
                                        model = post.images[page],
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                // Page Indicator
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = Dimens.sm),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(post.images.size) { index ->
                                        val color = if (pagerState.currentPage == index) scheme.primary else Color.White.copy(alpha = 0.5f)
                                        Box(
                                            modifier = Modifier
                                                .padding(2.dp)
                                                .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (reportId != null) {
                        item {
                            androidx.compose.material3.Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.md, vertical = Dimens.sm)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        spotColor = Color(0x1F000000),
                                        ambientColor = Color(0x0A000000)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant // Light amber
                            ) {
                                Row(
                                    modifier = Modifier.padding(Dimens.lg),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = "举报工单",
                                        tint = Color(0xFFF57F17),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(Dimens.md))
                                    Text(
                                        text = "🚨 举报卷宗 (ID: $reportId)",
                                        color = Color(0xFFF57F17),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    if (isAdminMode) {
                        item {
                            androidx.compose.material3.Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.md, vertical = Dimens.sm)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        spotColor = Color(0x1F000000),
                                        ambientColor = Color(0x0A000000)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(Dimens.lg)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Info,
                                            contentDescription = "AI Report",
                                            tint = scheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "🤖 DeepSeek AI 引擎初审报告",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = scheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = post.reviewNote ?: "AI 系统已检测该贴文本，未发现明显涉政、涉黄内容。但由于包含高风险词汇（如二手交易高频词），已被挂起，请人工复核。",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = scheme.onSurfaceVariant,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    // Price and Title Card
                    item {
                        SoftUiCard(modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (post.price != null && post.price > 0) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("¥ ", style = MaterialTheme.typography.titleMedium, color = scheme.error, fontWeight = FontWeight.Bold)
                                        Text("${post.price}", fontSize = 24.sp, color = scheme.error, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("面议", fontSize = 20.sp, color = scheme.error, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = post.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = post.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // Attributes Card
                    if (post.attributes.isNotEmpty()) {
                        item {
                            val schemas = remember(post.category) { CategorySchemaRegistry.getCategorySchema(post.category) }
                            val schemaMap = remember(schemas) { schemas.associateBy { it.key } }
                            
                            SoftUiCard(modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = Dimens.md)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = scheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("商品规格 / 参数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                                    }
                                    
                                    // 带有极细边框的表格容器
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    ) {
                                        post.attributes.entries.forEachIndexed { index, (key, element) ->
                                            val schema = schemaMap[key]
                                            val label = schema?.label ?: when(key) {
                                                "brand" -> "品牌"
                                                "condition" -> "成色"
                                                "purchaseDate" -> "购买时间"
                                                "shipping" -> "邮费"
                                                "parameters" -> "详细参数"
                                                else -> key.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                            }
                                            
                                            val isArray = element is kotlinx.serialization.json.JsonArray
                                            val arrayValues = if (isArray) {
                                                (element as kotlinx.serialization.json.JsonArray)
                                                    .mapNotNull { if (it is kotlinx.serialization.json.JsonPrimitive) it.content else null }
                                                    .filter { it.isNotBlank() }
                                            } else emptyList()
                                            
                                            val valueStr = when (element) {
                                                is kotlinx.serialization.json.JsonPrimitive -> element.content
                                                else -> element.toString()
                                            }
                                            
                                            if ((isArray && arrayValues.isNotEmpty()) || (!isArray && valueStr.isNotBlank())) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(IntrinsicSize.Min)
                                                ) {
                                                    // 左侧：浅灰色底色，固定宽度
                                                    Box(
                                                        modifier = Modifier
                                                            .width(100.dp)
                                                            .fillMaxHeight()
                                                            .background(MaterialTheme.colorScheme.background)
                                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                                        contentAlignment = Alignment.CenterStart
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                    
                                                    // 右侧：白色底色，展示数据
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                                        contentAlignment = Alignment.CenterStart
                                                    ) {
                                                        if (isArray) {
                                                            @OptIn(ExperimentalLayoutApi::class)
                                                            androidx.compose.foundation.layout.FlowRow(
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                arrayValues.forEach { tag ->
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .border(0.5.dp, Color(0x662962FF), RoundedCornerShape(4.dp))
                                                                            .background(Color(0x0A2962FF), RoundedCornerShape(4.dp))
                                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = tag,
                                                                            fontSize = 12.sp,
                                                                            color = Color(0xFF2962FF),
                                                                            fontWeight = FontWeight.Medium
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            val displayValue = if (schema?.unit?.isNotBlank() == true) "$valueStr ${schema.unit}" else valueStr
                                                            Text(
                                                                text = displayValue,
                                                                color = MaterialTheme.colorScheme.onBackground,
                                                                fontWeight = FontWeight.Bold,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontSize = 13.sp
                                                            )
                                                        }
                                                    }
                                                }
                                                if (index < post.attributes.size - 1) {
                                                    androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Publisher Card
                    item {
                        SoftUiCard(modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm).padding(bottom = Dimens.md)) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.qingyuan.lslife.ui.components.GoogleAvatar(url = post.user?.avatar, size = 36.dp)
                                Spacer(Modifier.width(Dimens.md))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(post.user?.nickname ?: "清远用户", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    val authLabel = post.user?.authLabel ?: "认证个人用户"
                                    val isMerchant = post.user?.isMerchant == true || post.publisherType == "MERCHANT"
                                    Text(
                                        text = authLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isMerchant) Color(0xFFD4AF37) else scheme.onSurfaceVariant
                                    )
                                }
                                
                                val isFollowing = post.isFollowing
                                Button(
                                    onClick = { viewModel.toggleFollow(post.user?.id ?: "") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing) Color(0xFFF5F5F5) else Color(0xFFFF2442),
                                        contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f) else Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (isFollowing) "已关注" else "关注", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                val isMerchant = post.user?.isMerchant == true || post.publisherType == "MERCHANT"
                                val actualMerchantId = post.merchantId ?: post.user?.merchantId
                                val isMerchantClick = isMerchant && actualMerchantId != null
                                
                                if (isMerchant) {
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            val targetId = if (isMerchantClick) actualMerchantId else post.user?.id
                                            if (targetId != null) {
                                                onPublisherClick(targetId, isMerchantClick)
                                            }
                                        },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = scheme.error),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Home,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "进店逛逛",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            val targetId = post.user?.id
                                            if (targetId != null) {
                                                onPublisherClick(targetId, false)
                                            }
                                        },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                        border = BorderStroke(1.dp, scheme.primary),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(
                                            text = "TA的发布",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = scheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = scheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(Dimens.xxl))
                    }
                }
            }
        }
    }
}

@Composable
fun SoftUiCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x0F000000),
                ambientColor = Color(0x0A000000)
            ),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        content()
    }
}
