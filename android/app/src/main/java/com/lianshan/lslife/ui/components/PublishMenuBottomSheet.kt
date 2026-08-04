package com.lianshan.lslife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lianshan.lslife.ui.theme.Dimens

private data class PublishMenuItem(
    val id: String,
    val title: String,
    val iconName: String,
    val iconUrl: String,
    val tintColor: Color
)

private val publishMenuItems = listOf(
    PublishMenuItem("cat_idle", "闲置物品", "shopping-bag", "/assets/icons/3d_flat_secondhand.png", Color(0xFFE52F2F)),
    PublishMenuItem("cat_house_sale", "二手房源", "home", "/assets/icons/3d_flat_housing.png", Color(0xFF2196F3)),
    PublishMenuItem("cat_house_rent", "房屋出租", "home", "/assets/icons/3d_flat_house_short.png", Color(0xFF2196F3)),
    PublishMenuItem("cat_service", "家政保洁", "cleaning-services", "/assets/icons/3d_flat_cleaning.png", Color(0xFFFF9800)),
    PublishMenuItem("cat_veggies", "同城生鲜", "shopping-basket", "/assets/icons/3d_flat_fresh_food.png", Color(0xFF4CAF50)),
    PublishMenuItem("cat_job", "求职招聘", "work", "/assets/icons/3d_flat_jobs.png", Color(0xFF00BCD4)),
    PublishMenuItem("cat_car_rental", "拼车租车", "local-shipping", "/assets/icons/3d_flat_car_rental.png", Color(0xFF3F51B5)),
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.xxl, start = Dimens.lg, end = Dimens.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "发布商业与生活服务",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Dimens.md)
            )
            Text(
                text = "严禁发布违规及泛社交动态信息",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = Dimens.xl)
            )

            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
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
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
