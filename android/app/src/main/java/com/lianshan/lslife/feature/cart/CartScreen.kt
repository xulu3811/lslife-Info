package com.lianshan.lslife.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.lianshan.lslife.core.model.TradeMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.CartEntry
import com.lianshan.lslife.ui.components.*
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onOpenMerchant: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onCheckout: (merchantId: String?, sellerId: String?, entryIds: String?, deliveryMethod: String) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color(0xFFF5F6F8),
        topBar = {
            TopAppBar(
                title = { 
                    Row(modifier = Modifier.padding(bottom = 2.dp)) {
                        Text("购物车", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        if (state.entries.isNotEmpty()) {
                            Spacer(Modifier.width(4.dp))
                            Text("(${state.entries.size})", fontSize = 16.sp, modifier = Modifier.alignByBaseline())
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.surface),
                actions = {
                    if (state.entries.isNotEmpty()) {
                        TextButton(onClick = { viewModel.toggleManageMode() }) {
                            Text(if (state.isManaging) "完成" else "管理", color = Color.Black, fontSize = 16.sp)
                        }
                    }
                }
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingBox(Modifier.padding(padding).fillMaxSize())
            state.error != null -> ErrorBox(state.error!!, onRetry = viewModel::load, modifier = Modifier.padding(padding).fillMaxSize())
            state.entries.isEmpty() -> EmptyState(
                title = "购物车还是空的",
                subtitle = "去首页逛逛心仪的本地服务吧",
                icon = Icons.Filled.ShoppingCart,
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
            else -> {
                Column(Modifier.padding(padding).fillMaxSize()) {
                    // Delivery/Pickup Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = Dimens.lg, vertical = Dimens.md),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                    ) {
                        TabButton(
                            text = "配送",
                            selected = state.deliveryMethod == "DELIVERY",
                            onClick = { viewModel.setDeliveryMethod("DELIVERY") },
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            text = "自提",
                            selected = state.deliveryMethod == "PICKUP",
                            onClick = { viewModel.setDeliveryMethod("PICKUP") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Promotional Banner
                    if (state.deliveryMethod == "PICKUP") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF4F4))
                                .padding(vertical = Dimens.sm, horizontal = Dimens.lg)
                        ) {
                            Text(
                                "满29.00元享自提包邮, 还差12.00元",
                                color = Color(0xFFE52F2F),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    // Cart Items (Filter out INFO_PUBLISH)
                    val filteredEntries = state.entries.filter { 
                        it.post?.tradeMode != TradeMode.INFO_PUBLISH && 
                        it.post?.tradeMode != TradeMode.INFO 
                    }
                    val grouped = filteredEntries.groupBy { it.merchantId ?: it.sellerId ?: "unknown" }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(Dimens.md),
                        verticalArrangement = Arrangement.spacedBy(Dimens.md),
                    ) {
                        grouped.forEach { (groupId, entries) ->
                            val shopName = entries.firstOrNull()?.product?.merchant?.name ?: entries.firstOrNull()?.post?.user?.nickname ?: "未知卖家"
                            
                            item(key = "header-$groupId") {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(16.dp),
                                    shadowElevation = 0.dp
                                ) {
                                    Column(Modifier.padding(Dimens.md), verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val groupEntryIds = entries.map { it.id }.toSet()
                                            val allSelected = state.selectedEntryIds.containsAll(groupEntryIds) && groupEntryIds.isNotEmpty()
                                            JoybuyCheckbox(
                                                checked = allSelected,
                                                onCheckedChange = { viewModel.toggleGroupSelection(groupId) }
                                            )
                                            Spacer(modifier = Modifier.width(Dimens.sm))
                                            
                                            val firstEntry = entries.firstOrNull()
                                            val isMerchant = firstEntry?.merchantId != null
                                            if (isMerchant) {
                                                Icon(Icons.Filled.Storefront, contentDescription = "Store", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                            } else {
                                                val avatar = firstEntry?.post?.user?.avatar
                                                if (!avatar.isNullOrEmpty()) {
                                                    NetworkImage(avatar, "Avatar", Modifier.size(20.dp).clip(CircleShape))
                                                } else {
                                                    Icon(Icons.Filled.Person, contentDescription = "User", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            
                                            Text(
                                                shopName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                        }
                                        entries.forEach { entry ->
                                            CartRow(
                                                entry = entry,
                                                selected = state.selectedEntryIds.contains(entry.id),
                                                onToggleSelect = { viewModel.toggleEntrySelection(entry) },
                                                onAdd = { viewModel.changeQty(entry, 1) },
                                                onRemove = { viewModel.changeQty(entry, -1) },
                                                onClick = {
                                                    entry.post?.id?.let { onOpenPost(it) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Bar
                    Surface(color = Color.White, shadowElevation = 8.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = Dimens.md, vertical = Dimens.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val isAllSelected = if (state.isManaging) {
                                val allIds = state.entries.map { it.id }.toSet()
                                state.selectedEntryIds.containsAll(allIds) && allIds.isNotEmpty()
                            } else {
                                val activeGroupId = state.entries.firstOrNull { it.id in state.selectedEntryIds }?.let { it.merchantId ?: it.sellerId ?: "unknown" } 
                                    ?: state.entries.firstOrNull()?.let { it.merchantId ?: it.sellerId ?: "unknown" }
                                val groupIds = state.entries.filter { (it.merchantId ?: it.sellerId ?: "unknown") == activeGroupId }.map { it.id }.toSet()
                                state.selectedEntryIds.containsAll(groupIds) && groupIds.isNotEmpty()
                            }

                            JoybuyCheckbox(
                                checked = isAllSelected,
                                onCheckedChange = { viewModel.selectAll() }
                            )
                            Spacer(modifier = Modifier.width(Dimens.xs))
                            Text("全选", style = MaterialTheme.typography.bodyMedium)
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            if (!state.isManaging) {
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = Dimens.md)) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("合计: ", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${"%.2f".format(state.total)}元",
                                            color = Color(0xFFFF5000), // Taobao orange
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    if (state.savedAmount > 0) {
                                        Text("已省: ${"%.2f".format(state.savedAmount)}元", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                            
                            val selectedCount = state.selectedEntryIds.size
                            if (state.isManaging) {
                                Button(
                                    onClick = { viewModel.removeSelectedItems() },
                                    enabled = selectedCount > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE52F2F)),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text("删除", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val selectedEntries = state.entries.filter { it.id in state.selectedEntryIds }
                                        val firstSelected = selectedEntries.firstOrNull()
                                        if (firstSelected != null) {
                                            val entryIdsStr = state.selectedEntryIds.joinToString(",")
                                            onCheckout(firstSelected.merchantId, firstSelected.sellerId, entryIdsStr, state.deliveryMethod)
                                        }
                                    },
                                    enabled = selectedCount > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5000)), // Taobao orange
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text("结算($selectedCount)", fontWeight = FontWeight.Bold, color = Color.White)
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
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (selected) Color(0xFFFFEBEB) else Color(0xFFF5F5F5)
    val textColor = if (selected) Color(0xFFE52F2F) else Color(0xFF333333)
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontWeight = fontWeight, fontSize = 15.sp)
    }
}

@Composable
fun JoybuyCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Icon(
        imageVector = if (checked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
        contentDescription = null,
        tint = if (checked) Color(0xFFE52F2F) else Color.LightGray,
        modifier = Modifier
            .size(22.dp)
            .clickable { onCheckedChange(!checked) }
    )
}

@Composable
private fun CartRow(
    entry: CartEntry,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val name = entry.product?.name ?: entry.post?.title ?: "商品"
    val price = entry.product?.price ?: entry.post?.price ?: 0.0
    val originalPrice = entry.product?.originalPrice ?: entry.post?.price
    val image = entry.product?.image ?: entry.post?.images?.firstOrNull() ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Dimens.sm),
        verticalAlignment = Alignment.Top,
    ) {
        JoybuyCheckbox(
            checked = selected,
            onCheckedChange = { onToggleSelect() }
        )
        Spacer(modifier = Modifier.width(Dimens.sm))
        NetworkImage(image, name, Modifier.size(80.dp))
        Spacer(modifier = Modifier.width(Dimens.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2)
            Spacer(modifier = Modifier.height(Dimens.xs))
            
            // Tag placeholder
            Box(modifier = Modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("不支持7天无理由", fontSize = 10.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(Dimens.sm))
            
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("${"%.2f".format(price)}元", color = Color(0xFFE52F2F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (originalPrice != null && originalPrice > price) {
                        Text(
                            "${"%.2f".format(originalPrice)}元", 
                            color = Color.Gray, 
                            fontSize = 12.sp,
                            style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
                ) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(26.dp)) {
                        Icon(if (entry.quantity <= 1) Icons.Filled.DeleteOutline else Icons.Filled.Remove, "减", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                    Box(modifier = Modifier.height(26.dp).padding(horizontal = Dimens.sm), contentAlignment = Alignment.Center) {
                        Text("${entry.quantity}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    IconButton(onClick = onAdd, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Filled.Add, "加", tint = Color.Black, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
