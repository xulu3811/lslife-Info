package com.qingyuan.lslife.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.qingyuan.lslife.feature.publish.PublishMomentState
import com.qingyuan.lslife.feature.publish.PublishMomentViewModel
import com.qingyuan.lslife.ui.theme.Dimens
import kotlinx.coroutines.launch

private data class PublishMenuItem(
    val id: String,
    val title: String,
    val iconName: String,
    val iconUrl: String,
    val tintColor: Color
)

private val publishMenuItems = listOf(
    PublishMenuItem("cat_2_service", "家政/护理", "cleaning-services", "android.resource://com.qingyuan.lslife/drawable/ic_category_service", Color(0xFFFF9800)),
    PublishMenuItem("cat_3_repair", "便民维修", "build", "android.resource://com.qingyuan.lslife/drawable/ic_category_repair", Color(0xFF607D8B)),
    PublishMenuItem("cat_4_fresh", "同城生鲜", "shopping-basket", "android.resource://com.qingyuan.lslife/drawable/ic_category_fresh", Color(0xFF4CAF50)),
    PublishMenuItem("cat_5_rent", "房屋出租", "home", "android.resource://com.qingyuan.lslife/drawable/ic_category_rent", Color(0xFF2196F3)),
    PublishMenuItem("cat_6_sale", "二手房产", "home", "android.resource://com.qingyuan.lslife/drawable/ic_category_sale", Color(0xFF2196F3)),
    PublishMenuItem("cat_7_carpool", "拼车/租车", "local-shipping", "android.resource://com.qingyuan.lslife/drawable/ic_category_carpool", Color(0xFF3351B5)),
    PublishMenuItem("cat_8_job", "招聘求职", "work", "android.resource://com.qingyuan.lslife/drawable/ic_category_job", Color(0xFF00BCD4)),
    PublishMenuItem("cat_9_life", "吃喝玩乐", "restaurant", "android.resource://com.qingyuan.lslife/drawable/ic_category_life", Color(0xFFFF5722)),
    PublishMenuItem("cat_10_edu", "教育培训", "school", "android.resource://com.qingyuan.lslife/drawable/ic_category_edu", Color(0xFFE91E63)),
    PublishMenuItem("cat_1_idle", "个人闲置", "shopping-bag", "android.resource://com.qingyuan.lslife/drawable/ic_category_idle", Color(0xFFE52F2F))
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
        containerColor = Color(0xFFF3F5F8),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFD1D5DB)) }
    ) {
        val pagerState = rememberPagerState(pageCount = { 2 })
        val coroutineScope = rememberCoroutineScope()
        val tabs = listOf("分类发布", "发动态")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color(0xFFF3F5F8),
                contentColor = Color(0xFF4285F4),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .padding(horizontal = 48.dp)
                            .clip(RoundedCornerShape(50)),
                        height = 4.dp,
                        color = Color(0xFF4285F4)
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = pagerState.currentPage == index
                    Tab(
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = if (isSelected) 16.sp else 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF111111) else Color(0xFF666666)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(380.dp)
            ) { page ->
                when (page) {
                    0 -> CommercePublishTab(onDismiss, onNavigateToPublish)
                    1 -> MomentPublishTab(onDismiss)
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
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
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
                Surface(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF0F4F9)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = item.iconUrl,
                    contentDescription = item.title,
                    modifier = Modifier.size(46.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF444444),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MomentPublishTab(
    onDismiss: () -> Unit,
    viewModel: PublishMomentViewModel = hiltViewModel()
) {
    var text by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aiGenerating by viewModel.aiGenerating.collectAsStateWithLifecycle()

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(3)) { uris ->
        if (uris.isNotEmpty()) {
            val newImages = (selectedImages + uris).take(3)
            selectedImages = newImages
        }
    }
    
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) {
            if (selectedImages.size < 3) {
                selectedImages = selectedImages + tempCameraUri!!
            }
        }
    }
    
    var showSourceMenu by remember { mutableStateOf(false) }
    var showAddressPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is PublishMomentState.Success -> {
                Toast.makeText(context, "动态发布成功", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onDismiss()
            }
            is PublishMomentState.Error -> {
                Toast.makeText(context, (uiState as PublishMomentState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    if (showSourceMenu) {
        AlertDialog(
            onDismissRequest = { showSourceMenu = false },
            title = { Text("添加图片") },
            text = { Text("请选择图片来源") },
            confirmButton = {
                TextButton(onClick = {
                    showSourceMenu = false
                    val file = java.io.File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    tempCameraUri = uri
                    takePhoto.launch(uri)
                }) {
                    Text("拍摄")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSourceMenu = false
                    pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("相册")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.lg)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("分享新鲜事...", color = Color.Gray, fontSize = 15.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )
                
                val locationRegion by viewModel.locationRegion.collectAsStateWithLifecycle()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Location Pill
                    Surface(
                        onClick = { showAddressPicker = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F3F4)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color(0xFF1A73E8), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (locationRegion.isBlank()) "添加位置" else locationRegion.split("-").lastOrNull() ?: "添加位置",
                                fontSize = 12.sp,
                                color = Color(0xFF1A73E8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // AI Assist Pill
                    Surface(
                        onClick = {
                            viewModel.generateAiDescription(text) { newText ->
                                text = newText
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE8DEF8),
                        enabled = text.isNotBlank() && !aiGenerating
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (aiGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color(0xFF1D192B), strokeWidth = 2.dp)
                            } else {
                                Text("✨", fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (aiGenerating) "AI润色中..." else "AI帮你写",
                                fontSize = 12.sp,
                                color = Color(0xFF1D192B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(selectedImages) { uri ->
                Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable {
                                selectedImages = selectedImages.filter { it != uri }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "删除", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (selectedImages.size < 3) {
                item {
                    Column(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF7F8FA))
                            .border(1.dp, Color(0xFFEFEFEF), RoundedCornerShape(8.dp))
                            .clickable {
                                showSourceMenu = true
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "添加图片", tint = Color(0xFF4285F4), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("${selectedImages.size}/3", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.publishMoment(context, text, selectedImages)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = uiState !is PublishMomentState.Publishing && (text.isNotBlank() || selectedImages.isNotEmpty()),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4285F4),
                disabledContainerColor = Color(0xFF4285F4).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            if (uiState is PublishMomentState.Publishing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("发布", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    if (showAddressPicker) {
        val nodes by viewModel.addressNodes.collectAsStateWithLifecycle()
        AddressPickerBottomSheet(
            addressNodes = nodes,
            onDismissRequest = { showAddressPicker = false },
            onAddressSelected = { selectedRegion ->
                viewModel.locationRegion.value = selectedRegion
            }
        )
    }
}
