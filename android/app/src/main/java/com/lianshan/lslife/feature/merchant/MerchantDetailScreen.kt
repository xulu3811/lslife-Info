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

import com.lianshan.lslife.ui.components.*
import com.lianshan.lslife.ui.theme.Dimens
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDetailScreen(
    merchantId: String,
    onBack: () -> Unit,
    onChatClick: (String, String) -> Unit,
    viewModel: MerchantDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

    LaunchedEffect(merchantId) { viewModel.load(merchantId) }
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
                        modifier = Modifier.fillMaxSize()
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
                                            if (m.ownerId != null && m.ownerId != viewModel.currentUserId) {
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
                            val tabs = listOf("全部发布", "评价", "商家")
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
        }
    }
}


