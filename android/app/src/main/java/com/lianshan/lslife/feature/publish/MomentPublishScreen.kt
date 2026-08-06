package com.lianshan.lslife.feature.publish

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentPublishScreen(
    initialTopic: String? = null,
    momentType: String? = null,
    onClose: () -> Unit,
    onBackHome: () -> Unit,
    viewModel: MomentPublishViewModel = hiltViewModel()
) {
    LaunchedEffect(initialTopic) {
        if (initialTopic != null) {
            viewModel.setTopic(initialTopic)
        }
    }
    LaunchedEffect(momentType) {
        viewModel.setMomentType(momentType)
    }
    
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val images by viewModel.images.collectAsState()
    val isPublishing by viewModel.isPublishing.collectAsState()
    val currentTopic by viewModel.topic.collectAsState()
    val currentLinkedCommerceId by viewModel.linkedCommerceId.collectAsState()
    val currentMomentType by viewModel.momentType.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val budget by viewModel.budget.collectAsState()
    val isUrgent by viewModel.isUrgent.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris.map { it.toString() })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color(0xFF333333)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.publish(
                                onSuccess = {
                                    Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show()
                                    onBackHome()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52F2F)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = Dimens.md),
                        enabled = !isPublishing
                    ) {
                        Text(
                            text = if (isPublishing) "发布中" else "发布",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            var activePanel by remember { mutableStateOf<String?>(null) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .imePadding()
            ) {
                // Dynamic Panels
                if (activePanel == "THEMES") {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(Dimens.md),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                    ) {
                        val themes = listOf("拍立得", "电影感", "马卡龙底")
                        items(themes) { theme ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedTheme == theme) Color(0xFFFFE5E5) else Color(0xFFF5F6F8),
                                modifier = Modifier
                                    .size(80.dp, 100.dp)
                                    .clickable { viewModel.setTheme(if (selectedTheme == theme) null else theme) },
                                border = if (selectedTheme == theme) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE52F2F)) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(theme, fontSize = 12.sp, color = if (selectedTheme == theme) Color(0xFFE52F2F) else Color(0xFF333333))
                                }
                            }
                        }
                    }
                } else if (activePanel == "STICKERS") {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(Dimens.md),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                    ) {
                        val stickers = listOf("避雷" to "💣避雷", "绝绝子" to "✨绝绝子", "宝藏" to "🎁宝藏", "种草" to "🌱种草")
                        items(stickers) { sticker ->
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF5F6F8),
                                modifier = Modifier.clickable { 
                                    viewModel.updateContent(content + sticker.second)
                                }
                            ) {
                                Text(
                                    text = sticker.first,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else if (activePanel == "TOPICS") {
                    val topics = listOf("#日常分享", "#探店打卡", "#好物种草", "#同城求助", "#二手转让", "#宠物寻回")
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(Dimens.md)
                    ) {
                        items(topics) { itemTopic ->
                            val isSelected = currentTopic == itemTopic
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Color(0xFFFFE5E5) else Color(0xFFF5F6F8),
                                modifier = Modifier.clickable {
                                    viewModel.setTopic(itemTopic)
                                }
                            ) {
                                Text(
                                    text = itemTopic,
                                    color = if (isSelected) Color(0xFFE52F2F) else Color(0xFF666666),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
                
                // Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.md, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    val items = listOf("THEMES" to "🖼️ 主题", "STICKERS" to "😀 表情", "TOPICS" to "🏷️ 话题")
                    items.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { 
                                    activePanel = if (activePanel == item.first) null else item.first 
                                }
                                .padding(8.dp)
                        ) {
                            Text(text = item.second, fontSize = 14.sp, color = if (activePanel == item.first) Color(0xFFE52F2F) else Color(0xFF333333))
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Photos Grid (Media First)
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images.size + 1) { index ->
                        if (index == images.size) {
                            if (images.size < 9) {
                                // Add button
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF5F6F8))
                                        .clickable {
                                            photoPickerLauncher.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "添加图片",
                                        tint = Color(0xFFB0B0B0),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        } else {
                            // Image Preview
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = images[index],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Delete icon
                                IconButton(
                                    onClick = { viewModel.removeImage(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .padding(2.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "删除",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimens.sm))

                // 2. Title Input
                BasicTextField(
                    value = title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        lineHeight = 28.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFFE52F2F)),
                    decorationBox = { innerTextField ->
                        Box {
                            if (title.isEmpty()) {
                                Text("填写标题会有更多赞哦~", color = Color(0xFFB0B0B0), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            innerTextField()
                        }
                    }
                )

                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                // 3. Content Input
                BasicTextField(
                    value = content,
                    onValueChange = { viewModel.updateContent(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xFF333333),
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFFE52F2F)),
                    decorationBox = { innerTextField ->
                        Box {
                            if (content.isEmpty()) {
                                Text(
                                    text = when(currentMomentType) {
                                        "STORE_VISIT" -> "这家店味道如何？环境怎么样？分享你的探店体验..."
                                        "RECOMMEND" -> "这个好物到底好在哪里？分享你的真实体验..."
                                        "DEMAND" -> "详细描述你需要什么帮助或服务..."
                                        else -> "添加正文"
                                    },
                                    color = Color(0xFFB0B0B0),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                var showCommerceSheet by remember { mutableStateOf(false) }

                // 4. Settings List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    // Location
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF666666), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加地点", fontSize = 15.sp, color = Color(0xFF333333), modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    // Topic
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏷️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (currentTopic.isNullOrEmpty()) "参与话题" else currentTopic!!, fontSize = 15.sp, color = if (currentTopic.isNullOrEmpty()) Color(0xFF333333) else Color(0xFFE52F2F), modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    // Linked Commerce
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showCommerceSheet = true }.padding(vertical = Dimens.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛍️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (currentLinkedCommerceId.isNullOrEmpty()) "关联我的商品/服务" else "已关联商品", fontSize = 15.sp, color = Color(0xFF333333))
                            if (currentLinkedCommerceId.isNullOrEmpty()) {
                                Text("在动态中挂载商品卡片，获取更多曝光", fontSize = 12.sp, color = Color(0xFF888888))
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    if (currentMomentType == "STORE_VISIT") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("评价打分", fontSize = 15.sp, color = Color(0xFF333333), modifier = Modifier.weight(1f))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                (1..5).forEach { star ->
                                    val isSelected = star <= rating
                                    Text(
                                        text = if (isSelected) "★" else "☆",
                                        fontSize = 18.sp,
                                        color = if (isSelected) Color(0xFFFF9800) else Color(0xFFCCCCCC),
                                        modifier = Modifier.clickable { viewModel.setRating(star) }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    } else if (currentMomentType == "DEMAND") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.md)
                        ) {
                            Text("💰", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("期望预算", fontSize = 15.sp, color = Color(0xFF333333))
                            Spacer(modifier = Modifier.weight(1f))
                            BasicTextField(
                                value = budget,
                                onValueChange = { viewModel.setBudget(it) },
                                modifier = Modifier.width(100.dp).background(Color(0xFFF5F6F8), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF333333), textAlign = androidx.compose.ui.text.style.TextAlign.End),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterEnd) {
                                        if (budget.isEmpty()) Text("填入金额", color = Color(0xFFB0B0B0), fontSize = 14.sp)
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.md)
                        ) {
                            Text("🔥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("加急", fontSize = 15.sp, color = Color(0xFF333333), modifier = Modifier.weight(1f))
                            Switch(
                                checked = isUrgent,
                                onCheckedChange = { viewModel.setUrgent(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFE52F2F)),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.xxl))

                var myCommerceItems by remember { mutableStateOf<List<com.lianshan.lslife.core.model.Post>>(emptyList()) }
                var selectedCommerceItem by remember { mutableStateOf<com.lianshan.lslife.core.model.Post?>(null) }

                LaunchedEffect(showCommerceSheet) {
                    if (showCommerceSheet) {
                        myCommerceItems = viewModel.fetchMyCommerceItems()
                    }
                }

                if (showCommerceSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showCommerceSheet = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        containerColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.lg)
                                .padding(bottom = Dimens.xxl)
                        ) {
                            Text(
                                text = "选择要关联的商品",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(vertical = Dimens.lg)
                            )
                            if (myCommerceItems.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("暂无可关联的商品", color = Color(0xFF888888))
                                }
                            } else {
                                androidx.compose.foundation.lazy.LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                                ) {
                                    items(myCommerceItems) { post ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedCommerceItem = post
                                                    viewModel.setLinkedCommerceId(post.id)
                                                    showCommerceSheet = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = post.images.firstOrNull(),
                                                contentDescription = null,
                                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(post.title ?: "", maxLines = 1, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("¥${post.price ?: 0}", color = Color(0xFFE52F2F), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Commerce Linker (O2O/C2C 带货锚点)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9FAFB),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCommerceSheet = true }
                ) {
                    if (selectedCommerceItem != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            AsyncImage(
                                model = selectedCommerceItem!!.images.firstOrNull(),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCommerceItem!!.title ?: "",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "¥${selectedCommerceItem!!.price ?: 0}",
                                    fontSize = 14.sp,
                                    color = Color(0xFFE52F2F),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { 
                                selectedCommerceItem = null
                                viewModel.setLinkedCommerceId(null)
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "取消关联", tint = Color(0xFFCCCCCC))
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFFE5E5), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🛍️",
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "关联我的商品/服务",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "在动态中挂载商品卡片，获取更多曝光",
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                            Text(
                                text = ">",
                                color = Color(0xFFCCCCCC),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Floating Location Tag
            Surface(
                shape = CircleShape,
                color = Color(0xFFF5F6F8),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Dimens.lg)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = Dimens.md, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "定位",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "连山壮族瑶族自治县",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}
