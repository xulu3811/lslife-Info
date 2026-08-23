package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/** Mock 数据状态 */
data class MerchantStoreState(
    val merchantName: String = "京东家电专卖店 (连山店)",
    val merchantLogo: String = "https://picsum.photos/200",
    val merchantBanner: String = "https://picsum.photos/800/400",
    val rating: Double = 4.9,
    val isCertified: Boolean = true,
    val items: List<MockStoreItem> = List(10) { 
        MockStoreItem(
            id = it.toString(),
            title = "高品质商品或服务 $it",
            price = 100.0 + it * 10,
            imageUrl = "https://picsum.photos/seed/$it/300/300",
            sales = 100 + it * 5
        ) 
    }
)

data class MockStoreItem(
    val id: String,
    val title: String,
    val price: Double,
    val imageUrl: String,
    val sales: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantStoreScreen(
    merchantId: String,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
    state: MerchantStoreState = remember { MerchantStoreState() }
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("首页", "全部商品/服务", "商家资质")
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("微店铺", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = scheme.onBackground,
                    navigationIconContentColor = scheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onChatClick,
                containerColor = scheme.error,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "联系商家")
            }
        },
        containerColor = Color(0xFFF4F5F7)
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header (Banner + Info)
            item(span = { GridItemSpan(2) }) {
                StoreHeaderSection(state)
            }

            // Sticky Tabs
            item(span = { GridItemSpan(2) }) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = scheme.error,
                                height = 3.dp
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title, 
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = if (selectedTabIndex == index) 16.sp else 14.sp,
                                    color = if (selectedTabIndex == index) scheme.onSurface else scheme.onSurfaceVariant
                                ) 
                            }
                        )
                    }
                }
            }

            // Content Feed
            items(state.items) { item ->
                StoreItemCard(item = item, onClick = { /* TODO route to detail */ })
            }
        }
    }
}

@Composable
private fun StoreHeaderSection(state: MerchantStoreState) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.fillMaxWidth().height(220.dp)
    ) {
        // Banner Background
        AsyncImage(
            model = state.merchantBanner,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(160.dp)
        )
        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                    startY = 100f
                ))
        )
        
        // Logo & Info Card overlapping banner
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo (shifted up)
                Box(
                    modifier = Modifier
                        .offset(y = (-32).dp)
                        .size(64.dp)
                        .background(Color.White, CircleShape)
                        .padding(4.dp)
                ) {
                    AsyncImage(
                        model = state.merchantLogo,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
                
                Text(
                    text = state.merchantName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.offset(y = (-16).dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(y = (-8).dp)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${state.rating} 分", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text("|", fontSize = 12.sp, color = Color.LightGray)
                    Spacer(Modifier.width(8.dp))
                    if (state.isCertified) {
                        Text("资质已核验", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(32.dp))
}

@Composable
fun StoreItemCard(item: MockStoreItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¥", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    Text(
                        text = item.price.toString(),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.weight(1f))
                    Text("已售 ${item.sales}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
