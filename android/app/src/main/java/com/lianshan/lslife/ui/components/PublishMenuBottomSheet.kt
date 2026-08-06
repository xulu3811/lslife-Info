package com.lianshan.lslife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lianshan.lslife.ui.theme.Dimens
import kotlinx.coroutines.launch

private data class PublishMenuItem(
    val id: String,
    val title: String,
    val iconName: String,
    val iconUrl: String,
    val tintColor: Color
)

private val publishMenuItems = listOf(
    PublishMenuItem("cat_idle", "闲置物品", "shopping-bag", "/assets/icons/3d_flat_secondhand.png", Color(0xFFE52F2F)),
    PublishMenuItem("cat_house_sale", "二手房源", "home", "/assets/icons/3d_flat_house_sale.png?v=7", Color(0xFF2196F3)),
    PublishMenuItem("cat_house_rent", "房屋出租", "home", "/assets/icons/3d_flat_house_rent.png?v=7", Color(0xFF2196F3)),
    PublishMenuItem("cat_service", "家政保洁", "cleaning-services", "/assets/icons/3d_flat_cleaning.png", Color(0xFFFF9800)),
    PublishMenuItem("cat_veggies", "同城生鲜", "shopping-basket", "/assets/icons/3d_flat_fresh_food.png", Color(0xFF4CAF50)),
    PublishMenuItem("cat_job", "求职招聘", "work", "/assets/icons/3d_flat_jobs.png", Color(0xFF00BCD4)),
    PublishMenuItem("cat_car_rental", "拼车租车", "local-shipping", "/assets/icons/3d_flat_car_rental.png", Color(0xFF3351B5)),
    PublishMenuItem("cat_maintenance", "水电维修", "build", "/assets/icons/3d_flat_repair.png", Color(0xFF607D8B)),
    PublishMenuItem("cat_education", "教育培训", "school", "/assets/icons/3d_flat_education.png", Color(0xFFE91E63)),
    PublishMenuItem("cat_dining", "餐饮娱乐", "restaurant", "/assets/icons/3d_flat_dining.png", Color(0xFFFF5722))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishMenuBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToPublish: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("商品/服务", "同城逛逛")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .padding(horizontal = 40.dp)
                            .height(3.dp),
                        color = Color(0xFFE52F2F)
                    )
                },
                divider = { }
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = pagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = if (selected) 16.sp else 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color(0xFFE52F2F) else Color(0xFF999999)
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.xl)
            ) { page ->
                when (page) {
                    0 -> CommercePublishTab(onDismiss, onNavigateToPublish)
                    1 -> MomentPublishTab(onDismiss, onNavigateToPublish)
                }
            }
        }
    }
}

@Composable
private fun CommercePublishTab(
    onDismiss: () -> Unit,
    onNavigateToPublish: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.lg)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F6F8),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = Dimens.lg)) {
                val chunkedItems = publishMenuItems.chunked(5)
                chunkedItems.forEachIndexed { index, rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { item ->
                            PublishMenuItemBox(item, onDismiss, onNavigateToPublish)
                        }
                        repeat(5 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    if (index < chunkedItems.size - 1) {
                        Spacer(Modifier.height(Dimens.lg))
                    }
                }
            }
        }
        Spacer(Modifier.height(Dimens.xxl))
    }
}

data class MomentScenario(
    val title: String,
    val subtitle: String,
    val type: String,
    val topic: String,
    val emoji: String
)

@Composable
private fun MomentPublishTab(
    onDismiss: () -> Unit,
    onNavigateToPublish: (String) -> Unit
) {
    val scenarios = listOf(
        MomentScenario("日常分享", "记录生活点滴", "DAILY", "#日常分享", "📸"),
        MomentScenario("探店打卡", "发现同城好店", "STORE_VISIT", "#探店打卡", "🍽️"),
        MomentScenario("好物种草", "推荐心水好物", "RECOMMEND", "#好物种草", "💡"),
        MomentScenario("发布需求", "海量同城响应", "DEMAND", "#发布需求", "🙋")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.lg)
    ) {
        val chunkedScenarios = scenarios.chunked(2)
        chunkedScenarios.forEachIndexed { index, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onDismiss()
                                onNavigateToPublish("moment_publish?topic=${java.net.URLEncoder.encode(item.topic, "UTF-8")}&momentType=${item.type}")
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(text = item.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = item.subtitle, fontSize = 12.sp, color = Color(0xFF888888))
                        }
                    }
                }
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (index < chunkedScenarios.size - 1) {
                Spacer(modifier = Modifier.height(Dimens.md))
            }
        }
        Spacer(Modifier.height(Dimens.xxl))
    }
}

@Composable
private fun RowScope.PublishMenuItemBox(
    item: PublishMenuItem,
    onDismiss: () -> Unit,
    onNavigateToPublish: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable {
                onDismiss()
                onNavigateToPublish(item.id)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(item.tintColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CategoryIconView(
                iconUrl = item.iconUrl,
                iconName = item.iconName,
                size = 36.dp,
                tint = item.tintColor
            )
        }
        Spacer(Modifier.height(Dimens.sm))
        Text(
            text = item.title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
