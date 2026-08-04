package com.lianshan.lslife.feature.merchant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.model.Product
import com.lianshan.lslife.ui.components.*
import com.lianshan.lslife.ui.theme.Dimens
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDetailScreen(
    merchantId: String,
    onBack: () -> Unit,
    onCheckedOut: (String) -> Unit,
    onChatClick: (String, String) -> Unit,
    viewModel: MerchantDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

    LaunchedEffect(merchantId) { viewModel.load(merchantId) }
    LaunchedEffect(state.checkedOutOrderId) { state.checkedOutOrderId?.let(onCheckedOut) }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = scheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.loading -> LoadingBox(Modifier.padding(padding).fillMaxSize())
                state.error != null -> ErrorBox(
                    state.error!!,
                    onRetry = { viewModel.load(merchantId) },
                    modifier = Modifier.padding(padding).fillMaxSize(),
                )
                else -> {
                    val m = state.merchant!!
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp) // padding for cart
                    ) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                NetworkImage(
                                    url = m.banner,
                                    contentDescription = m.name,
                                    modifier = Modifier.fillMaxWidth().height(240.dp),
                                    contentScale = ContentScale.Crop,
                                )
                                
                                // Overlapping Info Card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Dimens.md)
                                        .padding(top = 180.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(Dimens.lg),
                                        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(m.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                            if (m.ownerId != null) {
                                                OutlinedButton(
                                                    onClick = { onChatClick(m.ownerId, m.name) },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("联系商家", style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            ) {
                                                Icon(Icons.Filled.Star, null, Modifier.size(16.dp), tint = Color(0xFFFBC02D))
                                                Text("${m.rating}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFFFBC02D))
                                            }
                                            Text(
                                                "月售${m.sales} · ${m.distance}km",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = scheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            if (m.deliveryFee == 0.0) "免配送费 · 约 ${m.deliveryTime} 分钟送达"
                                            else "配送费 ¥${m.deliveryFee} · 约 ${m.deliveryTime} 分钟",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = scheme.onSurfaceVariant,
                                        )
                                        Text(m.description, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                                            m.tags.take(4).forEach { TagPill(it) }
                                        }
                                    }
                                }
                            }
                        }

                        // Tabs
                        item {
                            var selectedTab by remember { mutableIntStateOf(0) }
                            val tabs = listOf("点单", "评价", "商家")
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = scheme.background,
                                modifier = Modifier.padding(top = Dimens.md)
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = { Text(title, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(Dimens.sm))
                        }

                        // Products
                        items(m.items, key = { it.id }) { p ->
                            ProductRow(
                                product = p,
                                qty = state.quantities[p.id] ?: 0,
                                onAdd = { viewModel.changeQty(p.id, 1) },
                                onRemove = { viewModel.changeQty(p.id, -1) },
                            )
                        }
                    }
                }
            }

            // Dynamic TopAppBar
            val scrollOffset = listState.firstVisibleItemScrollOffset
            val topBarAlpha = min(1f, scrollOffset / 300f)
            val topBarColor = scheme.surface.copy(alpha = topBarAlpha)
            val onTopBarColor = if (topBarAlpha > 0.5f) scheme.onSurface else Color.White

            TopAppBar(
                title = {
                    if (topBarAlpha > 0.8f && state.merchant != null) {
                        Text(state.merchant!!.name, maxLines = 1, overflow = TextOverflow.Ellipsis, color = scheme.onSurface)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(if (topBarAlpha > 0.5f) Color.Transparent else Color.Black.copy(alpha=0.3f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = onTopBarColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor)
            )

            // Floating Cart Pill
            if (state.merchant != null && com.lianshan.lslife.core.config.AppConfig.ENABLE_COMMERCE_CART) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Dimens.lg)
                ) {
                    FloatingCartPill(
                        totalCount = state.totalCount,
                        payable = state.payable,
                        deliveryFee = state.merchant!!.deliveryFee,
                        checkingOut = state.checkingOut,
                        onCheckout = viewModel::checkout
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductRow(product: Product, qty: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.md, vertical = Dimens.sm)
            .background(scheme.surface, RoundedCornerShape(12.dp))
            .padding(Dimens.sm),
        horizontalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
        NetworkImage(
            url = product.image,
            contentDescription = product.name,
            modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Column(Modifier.weight(1f).height(90.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(product.desc, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text("月售${product.sales}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("¥", style = MaterialTheme.typography.labelMedium, color = scheme.error, fontWeight = FontWeight.Bold)
                Text("${product.price}", style = MaterialTheme.typography.titleMedium, color = scheme.error, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.weight(1f))
                QuantityControl(qty, onAdd, onRemove)
            }
        }
    }
}

@Composable
private fun QuantityControl(qty: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (qty > 0) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp).background(scheme.surfaceVariant, CircleShape),
            ) {
                Icon(Icons.Filled.Remove, "减", Modifier.size(16.dp), tint = scheme.onSurfaceVariant)
            }
            Text(
                text = "$qty",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Dimens.md)
            )
        }
        IconButton(
            onClick = onAdd,
            modifier = Modifier.size(28.dp).background(scheme.primary, CircleShape),
        ) {
            Icon(Icons.Filled.Add, "加", Modifier.size(16.dp), tint = scheme.onPrimary)
        }
    }
}

@Composable
private fun FloatingCartPill(
    totalCount: Int,
    payable: Double,
    deliveryFee: Double,
    checkingOut: Boolean,
    onCheckout: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isEnabled = totalCount > 0 && !checkingOut

    Surface(
        color = Color(0xFF1E1E1E), // Dark pill
        shape = RoundedCornerShape(50),
        shadowElevation = 8.dp,
        modifier = Modifier.padding(horizontal = Dimens.md).fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp).height(50.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cart Icon with Badge
            Box(modifier = Modifier.padding(end = Dimens.md)) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "购物车",
                    tint = if (totalCount > 0) scheme.primary else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
                if (totalCount > 0) {
                    Badge(
                        containerColor = scheme.error,
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Text("$totalCount", color = scheme.onError)
                    }
                }
            }

            // Price Info
            Column(modifier = Modifier.weight(1f)) {
                if (totalCount > 0) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("¥", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        Text("$payable", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Text("预估另需配送费 ¥$deliveryFee", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                } else {
                    Text("未选购商品", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Checkout Button
            Button(
                onClick = onCheckout,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    disabledContainerColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxHeight().padding(vertical = 2.dp)
            ) {
                if (checkingOut) {
                    CircularProgressIndicator(color = scheme.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("去结算", fontWeight = FontWeight.Bold, color = if(isEnabled) scheme.onPrimary else Color.Gray)
                }
            }
        }
    }
}
