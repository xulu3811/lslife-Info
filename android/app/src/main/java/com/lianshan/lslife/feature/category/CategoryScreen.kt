package com.qingyuan.lslife.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.qingyuan.lslife.core.model.CategoryNode
import com.qingyuan.lslife.ui.theme.Dimens

@Composable
fun CategoryScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
    onSearchClick: () -> Unit,
    onCategoryClick: (categoryId: String) -> Unit,
    onOpenPost: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F8)) // Google 经典的微冷灰背景，增强与白色卡片的对比，消除疲劳感
            .statusBarsPadding()
    ) {
        // 1. 悬浮态搜索栏 (M3 Floating Search Bar)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = CircleShape,
                    spotColor = Color(0x1A000000),
                    ambientColor = Color(0x08000000)
                )
                .clickable { onSearchClick() },
            shape = CircleShape,
            color = Color.White,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 18.dp)
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary, // 品牌色提亮
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "搜索本地商户、商品、服务",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. 主内容区 (高质量留白与精致排版)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.categoryGroups.forEach { group ->
                item(key = group.category.id) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 1.dp // 增加微微的投影，使其脱离背景，增加立体层次
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 22.dp, start = 12.dp, end = 12.dp)
                        ) {
                            // 带有品牌色标记的精致标题
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 18.dp, start = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 4.dp, height = 16.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = group.category.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937) // 深灰，更有品质感
                                )
                            }
                            
                            // 4列布局
                            val items = group.subCategories
                            val chunks = items.chunked(4)
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                chunks.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        for (i in 0 until 4) {
                                            if (i < rowItems.size) {
                                                val subCategory = rowItems[i]
                                                ProductCategoryItem(
                                                    category = subCategory,
                                                    modifier = Modifier.weight(1f),
                                                    onClick = { onCategoryClick(subCategory.id) }
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCategoryItem(
    category: CategoryNode,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // M3 规范的清爽底座 (放大尺寸以对齐首页视觉比例)
        Surface(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF0F4F9) // Google M3 Signature Light Blue-Gray
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                val iconUrl = category.iconUrl ?: ""
                if (iconUrl.isNotEmpty()) {
                    val baseUrl = com.qingyuan.lslife.BuildConfig.API_BASE_URL
                    val absoluteUrl = if (iconUrl.startsWith("http") || iconUrl.startsWith("android.resource://")) iconUrl else "${baseUrl.removeSuffix("/")}${if (iconUrl.startsWith("/")) "" else "/"}$iconUrl"
                    
                    AsyncImage(
                        model = absoluteUrl,
                        contentDescription = category.name,
                        modifier = Modifier.size(46.dp), // 增大实物图尺寸，与首页金刚区保持一致
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4B5563), // 柔和的深灰
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
