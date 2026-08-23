package com.lianshan.lslife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
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
    PublishMenuItem("cat_2_service", "家政/护理", "cleaning-services", "android.resource://com.lianshan.lslife/drawable/ic_category_service", Color(0xFFFF9800)),
    PublishMenuItem("cat_3_repair", "便民维修", "build", "android.resource://com.lianshan.lslife/drawable/ic_category_repair", Color(0xFF607D8B)),
    PublishMenuItem("cat_4_fresh", "同城生鲜", "shopping-basket", "android.resource://com.lianshan.lslife/drawable/ic_category_fresh", Color(0xFF4CAF50)),
    PublishMenuItem("cat_5_rent", "房屋出租", "home", "android.resource://com.lianshan.lslife/drawable/ic_category_rent", Color(0xFF2196F3)),
    PublishMenuItem("cat_6_sale", "二手房产", "home", "android.resource://com.lianshan.lslife/drawable/ic_category_sale", Color(0xFF2196F3)),
    PublishMenuItem("cat_7_carpool", "拼车/租车", "local-shipping", "android.resource://com.lianshan.lslife/drawable/ic_category_carpool", Color(0xFF3351B5)),
    PublishMenuItem("cat_8_job", "招聘求职", "work", "android.resource://com.lianshan.lslife/drawable/ic_category_job", Color(0xFF00BCD4)),
    PublishMenuItem("cat_9_life", "吃喝玩乐", "restaurant", "android.resource://com.lianshan.lslife/drawable/ic_category_life", Color(0xFFFF5722)),
    PublishMenuItem("cat_10_edu", "教育培训", "school", "android.resource://com.lianshan.lslife/drawable/ic_category_edu", Color(0xFFE91E63)),
    PublishMenuItem("cat_1_idle", "个人闲置", "shopping-bag", "android.resource://com.lianshan.lslife/drawable/ic_category_idle", Color(0xFFE52F2F))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishMenuBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToPublish: (String) -> Unit
) {
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
            CommercePublishTab(onDismiss, onNavigateToPublish)
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
                val chunkedItems = publishMenuItems.chunked(4)
                chunkedItems.forEachIndexed { index, rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { item ->
                            PublishMenuItemBox(item, onDismiss, onNavigateToPublish)
                        }
                        repeat(4 - rowItems.size) {
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
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(0.5.dp, Color(0xFFEEEEEE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            coil.compose.AsyncImage(
                model = item.iconUrl,
                contentDescription = item.title,
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
