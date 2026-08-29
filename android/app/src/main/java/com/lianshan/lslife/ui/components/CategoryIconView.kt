package com.qingyuan.lslife.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush

@Composable
fun CategoryIconView(
    iconUrl: String?,
    iconName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = null,
    categoryName: String? = null
) {
    val resolvedUrl = androidx.compose.runtime.remember(iconUrl, categoryName) {
        var finalIconUrl = iconUrl.takeIf { !it.isNullOrBlank() } ?: getFallbackIconUrl(categoryName)
        
        // Cache bust for updated V2 icons
        if (finalIconUrl == "android.resource://com.qingyuan.lslife/drawable/ic_category_life") {
            finalIconUrl = "android.resource://com.qingyuan.lslife/drawable/ic_category_life_v2"
        }
        if (finalIconUrl == "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_gourmet_dining") {
            finalIconUrl = "android.resource://com.qingyuan.lslife/drawable/ic_category_sub_gourmet_dining_v2"
        }
        
        val freshV2Map = mapOf(
            "ic_category_sub_fresh_fruit" to "ic_category_sub_fresh_fruit_v5",
            "ic_category_sub_fresh_veg" to "ic_category_sub_fresh_veg_v3",
            "ic_category_sub_fresh_meat" to "ic_category_sub_fresh_meat_v3",
            "ic_category_sub_fresh_seafood" to "ic_category_sub_fresh_seafood_v3",
            "ic_category_sub_frozen_food" to "ic_category_sub_frozen_food_v3",
            "ic_category_sub_fresh_grocery" to "ic_category_sub_fresh_grocery_v3",
            "ic_category_sub_fresh_deli" to "ic_category_sub_fresh_deli_v3",
            "ic_category_sub_house_rent" to "ic_category_sub_house_rent_v4",
            "ic_category_sub_office" to "ic_category_sub_office_v4",
            "ic_category_sub_warehouse_rent" to "ic_category_sub_warehouse_rent_v5",
            "ic_category_sub_short_term_rent" to "ic_category_sub_short_term_rent_v4",
            "ic_category_sub_parking_rent" to "ic_category_sub_parking_rent_v4",
            "ic_category_sub_house_sale" to "ic_category_sub_house_sale_v5",
            "ic_category_sub_land_factory_transfer" to "ic_category_sub_land_factory_transfer_v3",
            "ic_category_sub_parking_sale" to "ic_category_sub_parking_sale_v3",
            "ic_category_sub_new_property" to "ic_category_sub_new_property_v2",
            "ic_category_sub_daily_cleaning" to "ic_category_sub_daily_cleaning_v4",
            "ic_category_sub_deep_cleaning" to "ic_category_sub_deep_cleaning_v4",
            "ic_category_sub_appliance_clean" to "ic_category_sub_appliance_clean_v10",
            "ic_category_sub_nanny_hourly" to "ic_category_sub_nanny_hourly_v3",
            "ic_category_sub_maternity_childcare" to "ic_category_sub_maternity_childcare_v3",
            "ic_category_sub_caregiving" to "ic_category_sub_caregiving_v3",
            "ic_category_sub_plumbing" to "ic_category_sub_plumbing_v5",
            "ic_category_sub_repair" to "ic_category_sub_repair_v2",
            "ic_category_sub_renovation" to "ic_category_sub_renovation_v2",
            "ic_category_sub_digital_repair" to "ic_category_sub_digital_repair_v2",
            "ic_category_sub_bus_construction_rent" to "ic_category_sub_bus_construction_rent_v6",
            "ic_category_sub_driving_school" to "ic_category_sub_driving_school_v3",
            "ic_category_sub_gourmet_dining" to "ic_category_sub_local_life_v1"
        )
        freshV2Map.forEach { (old, new) ->
            if (finalIconUrl == "android.resource://com.qingyuan.lslife/drawable/$old") {
                finalIconUrl = "android.resource://com.qingyuan.lslife/drawable/$new"
            }
        }

        when {
            finalIconUrl.isNullOrBlank() -> null
            finalIconUrl.startsWith("http://") || finalIconUrl.startsWith("https://") -> finalIconUrl
            finalIconUrl.startsWith("/") -> {
                val baseUrl = com.qingyuan.lslife.BuildConfig.API_BASE_URL
                    .removeSuffix("/api/")
                    .removeSuffix("/api")
                    .removeSuffix("/")
                "$baseUrl$finalIconUrl"
            }
            else -> finalIconUrl
        }
    }

    if (iconName == "all") {
        AllCategoryCustomIcon(size = size, modifier = modifier)
        return
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (!resolvedUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                colorFilter = null, // Ensure NO monochrome tint is ever applied to 3D flat colored icons
                modifier = Modifier.size(size),
                loading = {
                    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size * 0.6f),
                            strokeWidth = 2.dp,
                            color = tint
                        )
                    }
                },
                error = {
                    Icon(
                        imageVector = resolveVectorIcon(iconName),
                        contentDescription = contentDescription,
                        tint = tint,
                        modifier = Modifier.size(size)
                    )
                }
            )
        } else if (!iconName.isNullOrBlank() && isEmoji(iconName)) {
            Text(
                text = iconName,
                fontSize = (size.value * 0.75f).sp,
                fontWeight = FontWeight.Normal
            )
        } else {
            Icon(
                imageVector = resolveVectorIcon(iconName),
                contentDescription = contentDescription ?: iconName,
                tint = tint,
                modifier = Modifier.size(size)
            )
        }
    }
}

private fun isEmoji(str: String): Boolean {
    val trimmed = str.trim()
    if (trimmed.isEmpty()) return false
    // If it is a short string (1-4 characters) and contains no standard ASCII letters, treat as Emoji
    if (trimmed.length <= 4 && trimmed.none { it in 'a'..'z' || it in 'A'..'Z' }) {
        return true
    }
    return false
}

private fun resolveVectorIcon(name: String?): ImageVector {
    val key = name?.trim()?.lowercase() ?: ""
    return when {
        key == "all" || key == "鍏ㄩ儴" -> Icons.Filled.GridView
        key.contains("shopping-bag") || key.contains("second_hand") || key.contains("idle") || key.contains("闂茬疆") -> Icons.Filled.ShoppingBag
        key.contains("briefcase") || key.contains("job") || key.contains("work") || key.contains("鎷涜仒") || key.contains("姹傝亴") -> Icons.Filled.Work
        key.contains("timer") || key.contains("time") || key.contains("part_time") || key.contains("clock") || key.contains("schedule") || key.contains("鍏艰亴") -> Icons.Filled.Schedule
        key.contains("apartment") || key.contains("building") || key.contains("secondhand_house") || key.contains("resale") || key.contains("浜屾墜鎴�") -> Icons.Filled.Apartment
        key.contains("store") || key.contains("shop") || key.contains("shop_rent") || key.contains("commercial") || key.contains("鏃洪摵") -> Icons.Filled.Storefront
        key.contains("home") || key.contains("house") || key.contains("housing") || key.contains("鎴垮眿") || key.contains("绉熷敭") -> Icons.Filled.Home
        key.contains("wrench") || key.contains("service") || key.contains("repair") || key.contains("maintenance") || key.contains("缁翠慨") || key.contains("姘寸數") -> Icons.Filled.Build
        key.contains("housekeeping") || key.contains("clean") || key.contains("瀹舵斂") || key.contains("淇濇磥") -> Icons.Filled.CleaningServices
        key.contains("moving") || key.contains("shipping") || key.contains("truck") || key.contains("car") || key.contains("car_rental") || key.contains("绉熻溅") || key.contains("椤洪�杞�") -> Icons.Filled.LocalShipping
        key.contains("apple") || key.contains("veggies") || key.contains("fruit") || key.contains("food") || key.contains("produce") || key.contains("鐢熼矞") || key.contains("姘存灉") || key.contains("钄�彍") -> Icons.Filled.ShoppingBasket
        key.contains("phone") || key.contains("electronics") -> Icons.Filled.Smartphone
        key.contains("laptop") || key.contains("computer") -> Icons.Filled.Laptop
        key.contains("dress") || key.contains("clothing") || key.contains("shoes") -> Icons.Filled.Checkroom
        key.contains("book") || key.contains("novel") -> Icons.AutoMirrored.Filled.MenuBook
        key.contains("sparkles") || key.contains("awesome") || key.contains("ai") -> Icons.Filled.AutoAwesome
        else -> Icons.Filled.Folder
    }
}

@Composable
fun AllCategoryCustomIcon(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 4.dp, 
                shape = RoundedCornerShape(size * 0.22f),
                spotColor = Color(0xFFE52F2F).copy(alpha = 0.5f),
                ambientColor = Color(0xFFE52F2F).copy(alpha = 0.1f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B),
                        Color(0xFFE52F2F)
                    )
                ),
                shape = RoundedCornerShape(size * 0.22f)
            ),
        contentAlignment = Alignment.Center
    ) {
        val gridSize = size * 0.55f
        val spacing = size * 0.08f
        val itemSize = (gridSize - spacing) / 2
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                Box(modifier = Modifier.size(itemSize).background(Color.White, RoundedCornerShape(itemSize * 0.3f)))
                Box(modifier = Modifier.size(itemSize).background(Color.White, RoundedCornerShape(itemSize * 0.3f)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                Box(modifier = Modifier.size(itemSize).background(Color.White, RoundedCornerShape(itemSize * 0.3f)))
                Box(modifier = Modifier.size(itemSize).background(Color.White, RoundedCornerShape(itemSize * 0.3f)))
            }
        }
    }
}

private fun getFallbackIconUrl(name: String?): String? {
    val key = name?.trim() ?: return null
    return when (key) {
        "数码 3C", "服饰箱包", "日用/家电", "美妆个护", "母婴儿童", "运动 & 交通工具", "文娱爱好", "其它", "其他" -> "/assets/icons/3d_flat_secondhand.png"
        "新鲜水果" -> "/assets/icons/3d_flat_veg_fresh.png"
        "时令蔬菜" -> "/assets/icons/3d_flat_produce.png"
        "肉禽蛋品" -> "/assets/icons/3d_flat_veg_meat.png"
        "海鲜水产" -> "/assets/icons/3d_flat_veg_local.png"
        "冷藏冻货" -> "/assets/icons/3d_flat_veg_wholesale.png"
        "粮油调味" -> "/assets/icons/3d_flat_veg_grocery.png"
        "熟食卤味" -> "/assets/icons/3d_flat_dining.png"
        "快餐便当" -> "/assets/icons/3d_flat_pt_errand.png"
        "地方菜系" -> "/assets/icons/3d_flat_job_hospitality.png"
        "烧烤海鲜" -> "/assets/icons/3d_flat_pt_hotel.png"
        "火锅小吃" -> "/assets/icons/3d_flat_job_other.png"
        else -> null
    }
}
