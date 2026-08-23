package com.lianshan.lslife.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.feature.home.AsymmetricFeaturedLayout
import com.lianshan.lslife.ui.theme.Dimens

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
            .background(Color(0xFFF5F6F8))
            .statusBarsPadding()
    ) {
        // 1. 固定吸顶搜索框
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(36.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "搜索本地商户、商品、服务", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
            }
        }

        // 2. 单层 LazyVerticalGrid 跨列方案
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.categoryGroups.forEach { group ->
                // [全宽标题] Level 1 Category Header
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = group.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                
                // [网格卡片] Level 2 SubCategories
                items(
                    count = group.subCategories.size,
                    key = { index -> group.subCategories[index].id }
                ) { index ->
                    val subCategory = group.subCategories[index]
                    ProductCategoryCard(
                        category = subCategory,
                        onClick = { onCategoryClick(subCategory.id) }
                    )
                }
            }

        }
    }
}

@Composable
fun ProductCategoryCard(
    category: CategoryNode,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF333333),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .fillMaxWidth(0.65f)
            )

            val iconUrl = category.iconUrl ?: ""
            if (iconUrl.isNotEmpty()) {
                val baseUrl = com.lianshan.lslife.BuildConfig.API_BASE_URL
                val absoluteUrl = if (iconUrl.startsWith("http") || iconUrl.startsWith("android.resource://")) iconUrl else "${baseUrl.removeSuffix("/")}${if (iconUrl.startsWith("/")) "" else "/"}$iconUrl"
                
                // ⚠️ 传入该组件的 imageUrl 必须是经过 U2-Net 算法处理后的无背景透明底 PNG 图片。绝对不允许带有底色（如白色或黑色）的矩形图片破坏 UI。
                AsyncImage(
                    model = absoluteUrl,
                    contentDescription = category.name,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 0.dp, bottom = 0.dp) // 极小边距，让图片“沉底”并靠右
                        .width(84.dp) // 缩小30%，原为120.dp
                        .height(70.dp), // 缩小30%，原为100.dp
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomEnd // 关键属性：让加载出的图片在设定区域内严格贴底贴右！
                )
            }
        }
    }
}

@Composable
fun ServiceCategoryCard(
    category: CategoryNode,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .wrapContentHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val iconUrl = category.iconUrl ?: ""
            if (iconUrl.isNotEmpty()) {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = category.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color(0xFFF5F6F8))
                )
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF333333),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
