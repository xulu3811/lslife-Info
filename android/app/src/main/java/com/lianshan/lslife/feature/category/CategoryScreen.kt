package com.lianshan.lslife.feature.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.core.model.CategoryNode
import com.lianshan.lslife.ui.theme.Dimens

import com.lianshan.lslife.core.model.Post

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
    onSearchClick: () -> Unit,
    onOpenPost: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Top Search Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.xs)
                .height(36.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF5F6F8),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Dimens.md)
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "搜索本地商户、商品、服务", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 1. 左侧：极简微章导航 (CompactNav)
            LazyColumn(
                modifier = Modifier
                    .width(86.dp)
                    .background(Color(0xFFF7F8FA))
                    .fillMaxHeight()
            ) {
                itemsIndexed(state.topCategories.ifEmpty { listOf(CategoryNode("1", "二手", null), CategoryNode("2", "服务", null)) }) { index, category ->
                    val isSelected = state.selectedTabIndex == index
                    CompactPrimaryTab(
                        text = category.name,
                        isSelected = isSelected,
                        onClick = { viewModel.onTabSelected(index) }
                    )
                }
            }

            // 2. 右侧：紧凑型商品大厅
            val subCategories = if (state.subCategories.isEmpty()) emptyList() else listOf(CategoryNode("all", "全部", null)) + state.subCategories
            
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.White),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp
            ) {
                // 吸顶：横向滚动的胶囊标签 + 筛选器
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column {
                        // 胶囊二级分类
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(subCategories) { subCat ->
                                val selected = state.selectedSubCategory == subCat.id || (state.selectedSubCategory.isEmpty() && subCat.id == "all")
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .background(
                                            color = if (selected) Color(0xFFFFEBEE) else Color(0xFFF5F6F8),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.onSubCategory(subCat.id) }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = subCat.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color(0xFFE53935) else Color(0xFF555555)
                                    )
                                }
                            }
                        }

                        // 筛选栏 (在真实环境中通常使用 stickyHeader, 但 StaggeredGrid 暂不支持 stickyHeader, 故放置于顶部 item)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterText(text = "推荐", selected = state.sort == "default" || state.sort.isEmpty()) { viewModel.onSort("default") }
                            FilterText(text = "最新", selected = state.sort == "newest") { viewModel.onSort("newest") }
                            FilterText(text = "价格", selected = state.sort == "price_asc") { viewModel.onSort("price_asc") }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // 微缩商品瀑布流
                items(state.posts, key = { it.id }) { product ->
                    CompactProductCard(product = product, onClick = { onOpenPost(product.id) })
                }
            }
        }
    }
}

@Composable
private fun CompactPrimaryTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .background(Color(0xFFE53935), RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                    .align(Alignment.CenterStart)
            )
        }
        Text(
            text = text,
            fontSize = if (isSelected) 14.sp else 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color(0xFF666666)
        )
    }
}

@Composable
private fun FilterText(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) Color(0xFFE53935) else Color(0xFF777777),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun CompactProductCard(product: Post, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 0.5.dp
    ) {
        Column {
            val imageUrl = product.images.firstOrNull() ?: ""
            AsyncImage(
                model = if (imageUrl.isNotEmpty()) imageUrl + "?x-oss-process=image/resize,w_300" else "",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f/3f)
                    .background(Color(0xFFF5F6F8))
            )
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val priceText = if (product.price != null && product.price > 0.0) "¥ ${product.price}" else "面议"
                var tag = product.attributes.keys.firstOrNull()?.let { product.attributes[it].toString().replace("\"", "") } ?: product.category
                if (tag == "面议") tag = product.category
                
                if (tag.isNotBlank() && tag != priceText) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFF0F0), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(text = tag, fontSize = 10.sp, color = Color(0xFFE53935))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                
                Text(
                    text = priceText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoryScreen() {
    CategoryScreen(onSearchClick = {}, onOpenPost = {})
}
