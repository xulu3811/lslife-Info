package com.qingyuan.lslife.feature.publish

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.qingyuan.lslife.core.model.CategoryNode
import com.qingyuan.lslife.core.model.DynamicField
import com.qingyuan.lslife.core.model.TradeMode
import androidx.compose.ui.text.style.TextOverflow
import com.qingyuan.lslife.ui.components.CategoryIconView
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PublishScreen(
    postId: String? = null,
    viewModel: PublishViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onOpenPost: (String) -> Unit = {},
    onBackHome: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var showCategoryBottomSheet by remember { mutableStateOf(false) }
    var showAddressPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.requireCategorySelection) {
        if (state.requireCategorySelection) {
            showCategoryBottomSheet = true
            viewModel.onCategorySelectionShown()
        }
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(9)) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                val absolutePaths = uris.mapNotNull { uri ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri) ?: return@mapNotNull null
                        val fileName = "publish_temp_${java.util.UUID.randomUUID()}.jpg"
                        val cacheFile = java.io.File(context.cacheDir, fileName)
                        cacheFile.outputStream().use { output ->
                            inputStream.copyTo(output)
                        }
                        cacheFile.absolutePath
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                withContext(Dispatchers.Main) {
                    if (absolutePaths.isNotEmpty()) {
                        viewModel.onImagesSelected(absolutePaths)
                    } else {
                        viewModel.setMessage("部分图片读取失败，请重试")
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadQuota()
        if (!postId.isNullOrBlank() && postId != "{postId}") {
            viewModel.loadPost(postId)
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    val publishedPostId = state.publishedPostId
    if (state.success && publishedPostId != null) {
        PublishSuccessView(
            message = state.message ?: "操作成功",
            postId = publishedPostId,
            onViewPost = { onOpenPost(publishedPostId) },
            onBackHome = onBackHome
        )
        return
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = Color(0xFFF7F8FA),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp).clickable { onClose() }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("发布信息", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    
                    state.quota?.let { q ->
                        val isUnlimited = q.limit >= 999999
                        if (!isUnlimited) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFF7F8FA),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(color = Color(0xFFE53935))) {
                                            append("${q.used}")
                                        }
                                        append("/${q.limit}")
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { viewModel.submit() },
                        enabled = !state.submitting,
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        if (state.submitting) {
                            CircularProgressIndicator(color = scheme.onPrimary, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            val isEditMode = !postId.isNullOrBlank() && postId != "{postId}"
                            Text(if (isEditMode) "确认修改" else "确认发布", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Category Picker
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color(0x1A000000),
                        ambientColor = Color(0x1A000000)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryBottomSheet = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Folder, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("所属分类", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.selectedCategoryPath,
                                fontSize = 14.sp,
                                color = if (state.selectedCategory != null) scheme.primary else Color.Gray,
                                fontWeight = if (state.selectedCategory != null) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
                        }
                    }
                }

                // Card 2: Images, Title and Description
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color(0x1A000000),
                        ambientColor = Color(0x1A000000)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Image Picker Row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.images) { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(20.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .clickable { viewModel.removeImage(uri) }
                                    )
                                }
                            }
                            if (state.images.size < 9) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF7F8FA))
                                            .border(1.dp, Color(0xFFEFEFEF), RoundedCornerShape(8.dp))
                                            .clickable {
                                                pickMedia.launch(
                                                    androidx.activity.result.PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            },
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Add", tint = scheme.primary, modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text("添加图片", fontSize = 11.sp, color = scheme.primary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        
                        // Title
                        val titleBringIntoView = remember { BringIntoViewRequester() }
                        BasicTextField(
                            value = state.title,
                            onValueChange = viewModel::onTitle,
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = scheme.onSurface),
                            cursorBrush = SolidColor(scheme.primary),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(titleBringIntoView)
                                .onFocusChanged { f ->
                                    if (f.isFocused) {
                                        scope.launch {
                                            delay(300)
                                            titleBringIntoView.bringIntoView()
                                        }
                                    }
                                }
                                .padding(bottom = 12.dp),
                            decorationBox = { innerTextField ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (state.title.isEmpty()) {
                                            Text("填写吸引人的标题...", color = Color.LightGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                        innerTextField()
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${state.title.length}/30",
                                        color = if (state.title.length >= 30) scheme.error else Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        HorizontalDivider(color = Color(0xFFF5F5F5))
                        Spacer(Modifier.height(12.dp))

                        // Description
                        val descBringIntoView = remember { BringIntoViewRequester() }
                        BasicTextField(
                            value = state.description,
                            onValueChange = viewModel::onDescription,
                            textStyle = TextStyle(fontSize = 15.sp, color = scheme.onSurface),
                            cursorBrush = SolidColor(scheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp)
                                .bringIntoViewRequester(descBringIntoView)
                                .onFocusChanged { f ->
                                    if (f.isFocused) {
                                        scope.launch {
                                            delay(300)
                                            descBringIntoView.bringIntoView()
                                        }
                                    }
                                },
                            decorationBox = { innerTextField ->
                                if (state.description.isEmpty()) {
                                    Text("描述一下宝贝或服务的细节、成色、转手原因...", color = Color.LightGray, fontSize = 15.sp)
                                }
                                innerTextField()
                            }
                        )

                        Spacer(Modifier.height(12.dp))

                        // AI Helper Button & Urgent Tag Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(enabled = !state.aiOptimizing) { viewModel.generateAiDescription() }
                                    .background(Color(0xFFF4F0FF), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                if (state.aiOptimizing) {
                                    CircularProgressIndicator(color = Color(0xFF673AB7), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("AI 智能文案生成中...", fontSize = 12.sp, color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = "AI", tint = Color(0xFF673AB7), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("AI 智能文案润色 >", fontSize = 12.sp, color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.setUseUrgentTag(!state.useUrgentTag) }
                                    .background(if (state.useUrgentTag) Color(0xFFFFF0F0) else Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(if (state.useUrgentTag) "急售 (已开启)" else "使用急售标签", fontSize = 12.sp, color = if (state.useUrgentTag) Color(0xFFE53935) else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Dynamic Attributes Form Section (Schema-Driven)
                DynamicAttributesSection(
                    schemas = state.categorySchemas,
                    attributesMap = state.attributesMap,
                    onAttributeChange = viewModel::onAttributeChange
                )



                // Card 4: Dynamic Trade Mode Fields & Location
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color(0x1A000000),
                        ambientColor = Color(0x1A000000)
                    )
                ) {
                    Column {
                        val focusRequester = remember { FocusRequester() }
                        val bringIntoView = remember { BringIntoViewRequester() }

                        // 价格/薪资
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("价格/薪资", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(
                                value = state.price,
                                onValueChange = viewModel::onPrice,
                                textStyle = TextStyle(fontSize = 16.sp, color = Color.Red, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                decorationBox = { inner ->
                                    if (state.price.isEmpty()) Text("面议/自定", color = Color.LightGray, fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                    else inner()
                                }
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                        // 联系电话
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("联系电话", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(
                                value = state.contactPhone,
                                onValueChange = viewModel::onContactPhone,
                                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black, textAlign = androidx.compose.ui.text.style.TextAlign.End),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                decorationBox = { inner ->
                                    if (state.contactPhone.isEmpty()) Text("必填", color = Color.LightGray, fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                    else inner()
                                }
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF5F5F5))

                        // Location
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAddressPicker = true }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("发布位置", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (state.locationRegion.isEmpty()) "请选择 (必填)" else state.locationRegion, fontSize = 14.sp, color = if (state.locationRegion.isEmpty()) Color.LightGray else Color.Gray)
                                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                }
                            }
                            HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("详细地址", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(16.dp))
                                BasicTextField(
                                    value = state.locationDetail,
                                    onValueChange = viewModel::onLocationDetail,
                                    textStyle = TextStyle(fontSize = 14.sp, color = Color.Black, textAlign = androidx.compose.ui.text.style.TextAlign.End),
                                    modifier = Modifier.weight(1f),
                                    decorationBox = { inner ->
                                        if (state.locationDetail.isEmpty()) Text("街道/门牌号等 (非必填)", color = Color.LightGray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                        else inner()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(320.dp))
            }
        }
    }

    // Category Multi-level Cascading Picker (ModalBottomSheet)
    if (showCategoryBottomSheet) {
        CategoryTreeBottomSheet(
            categoryTree = state.categoryTree,
            isLoading = state.loadingCategories,
            error = state.categoryError,
            onRetry = { viewModel.retryLoadCategories() },
            onDismiss = { showCategoryBottomSheet = false },
            onSelectLeaf = { node, path ->
                viewModel.onSelectLeafCategory(node, path)
                showCategoryBottomSheet = false
            },
            preSelectedLevel1Id = state.preSelectedLevel1Id
        )
    }

    if (showAddressPicker) {
        com.qingyuan.lslife.ui.components.AddressPickerBottomSheet(
            addressNodes = state.addressNodes,
            onDismissRequest = { showAddressPicker = false },
            onAddressSelected = {
                viewModel.onLocationRegion(it)
            }
        )
    }
}



/** 多级分类级联选择 BottomSheet */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTreeBottomSheet(
    categoryTree: List<CategoryNode>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onSelectLeaf: (CategoryNode, String) -> Unit,
    preSelectedLevel1Id: String? = null
) {
    val publishableTree = remember(categoryTree) {
        categoryTree.filter { it.id != "all" }
    }

    var selectedLevel1 by remember(publishableTree, preSelectedLevel1Id) { 
        mutableStateOf<CategoryNode?>(
            publishableTree.find { it.id == preSelectedLevel1Id } ?: publishableTree.firstOrNull()
        ) 
    }
    var searchQuery by remember { mutableStateOf("") }

    val allLeavesWithPaths = remember(publishableTree) {
        val list = mutableListOf<Pair<CategoryNode, String>>()
        fun traverse(node: CategoryNode, currentPath: String) {
            if (node.isLeaf) {
                list.add(node to currentPath)
            } else {
                node.children.forEach { child ->
                    traverse(child, if (currentPath.isEmpty()) child.name else "$currentPath > ${child.name}")
                }
            }
        }
        publishableTree.forEach { root ->
            if (root.isLeaf) {
                list.add(root to root.name)
            } else {
                root.children.forEach { l2 ->
                    traverse(l2, "${root.name} > ${l2.name}")
                }
            }
        }
        list
    }

    val searchResults = remember(allLeavesWithPaths, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            allLeavesWithPaths.filter { (node, path) ->
                node.name.lowercase().contains(q) || path.lowercase().contains(q)
            }
        }
    }

    LaunchedEffect(publishableTree) {
        if (publishableTree.isNotEmpty() && (selectedLevel1 == null || publishableTree.none { it.id == selectedLevel1?.id })) {
            selectedLevel1 = publishableTree.firstOrNull()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "选择发布分类",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(19.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text("搜索分类，例: 手机 / 租房 / 兼职", fontSize = 13.sp, color = Color.Gray)
                            }
                            inner()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            when {
                isLoading && publishableTree.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            Text("正在同步全城分类目录...", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
                error != null && publishableTree.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error, fontSize = 14.sp, color = Color.Red)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = onRetry) {
                                Text("重新加载")
                            }
                        }
                    }
                }
                publishableTree.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无可用分类", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                searchQuery.isNotBlank() -> {
                    if (searchResults.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("未找到与 \"$searchQuery\" 相关的分类", fontSize = 14.sp, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item(key = "search_header") {
                                CategorySectionHeader(title = "🔍 搜索结果 (${searchResults.size})", isHot = false)
                            }
                            items(searchResults, key = { "search_" + it.first.id }) { (leaf, path) ->
                                CategoryListRow(leafNode = leaf, path = path, isHot = false, onSelectLeaf = onSelectLeaf)
                            }
                        }
                    }
                }
                else -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Column 1: Level 1 Categories
                        LazyColumn(
                            modifier = Modifier
                                .width(130.dp)
                                .fillMaxHeight()
                                .background(Color(0xFFF7F8FA), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        ) {
                            val infoTree = publishableTree
                            
                            if (infoTree.isNotEmpty()) {
                                item {
                                    Column(modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)) {
                                        Text("全部分类", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Text("信息发布·自主联系", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                                items(infoTree, key = { it.id }) { node ->
                                    val isSelected = selectedLevel1?.id == node.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedLevel1 = node }
                                            .background(if (isSelected) Color.White else Color.Transparent)
                                            .padding(horizontal = 8.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CategoryIconView(
                                            iconUrl = node.iconUrl,
                                            iconName = node.icon,
                                            categoryName = node.name,
                                            size = 20.dp,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = node.name,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Column 2: Level 2 & Leaf Categories (电商级分组瀑布流与网格导航)
                        val l2Nodes = selectedLevel1?.children.orEmpty()
                        val hotLeaves = remember(l2Nodes) {
                            val list = mutableListOf<Pair<CategoryNode, String>>()
                            l2Nodes.forEach { l2 ->
                                if (l2.isLeaf) {
                                    if (l2.isHot) list.add(l2 to "${selectedLevel1?.name ?: ""} > ${l2.name}")
                                } else {
                                    l2.children.forEach { leaf ->
                                        if (leaf.isHot) list.add(leaf to "${selectedLevel1?.name ?: ""} > ${l2.name} > ${leaf.name}")
                                    }
                                }
                            }
                            list
                        }
                        val directLeaves = remember(l2Nodes) { l2Nodes.filter { it.isLeaf } }
                        val subGroups = remember(l2Nodes) { l2Nodes.filter { !it.isLeaf } }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 1. 热门推荐专区 (顶部优先展示高频分类)
                            if (hotLeaves.isNotEmpty()) {
                                item(key = "hot_section_header") {
                                    CategorySectionHeader(title = "🔥 热门推荐", isHot = true)
                                }
                                items(hotLeaves, key = { "hot_" + it.first.id }) { (leaf, path) ->
                                    CategoryListRow(leafNode = leaf, path = path, isHot = true, onSelectLeaf = onSelectLeaf)
                                }
                                item(key = "hot_spacer") { Spacer(modifier = Modifier.height(12.dp)) }
                            }

                            // 2. 直接叶子分类专区
                            if (directLeaves.isNotEmpty()) {
                                item(key = "direct_leaves_header") {
                                    CategorySectionHeader(title = if (subGroups.isEmpty()) "全部分类" else "通用分类", isHot = false)
                                }
                                val directPairs = directLeaves.map { it to "${selectedLevel1?.name ?: ""} > ${it.name}" }
                                items(directPairs, key = { "direct_" + it.first.id }) { (leaf, path) ->
                                    CategoryListRow(leafNode = leaf, path = path, isHot = false, onSelectLeaf = onSelectLeaf)
                                }
                                item(key = "direct_spacer") { Spacer(modifier = Modifier.height(12.dp)) }
                            }

                            // 3. 分组瀑布流网格 (二级大类 Header + 三级叶子网格卡片)
                            subGroups.forEach { group ->
                                item(key = "group_header_${group.id}") {
                                    CategorySectionHeader(title = group.name, isHot = false)
                                }
                                val leafPairs = group.children.map { leaf -> leaf to "${selectedLevel1?.name ?: ""} > ${group.name} > ${leaf.name}" }
                                items(leafPairs, key = { "group_${group.id}_" + it.first.id }) { (leaf, path) ->
                                    CategoryListRow(leafNode = leaf, path = path, isHot = false, onSelectLeaf = onSelectLeaf)
                                }
                                item(key = "group_spacer_${group.id}") { Spacer(modifier = Modifier.height(12.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySectionHeader(title: String, isHot: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(if (isHot) Color(0xFFFFF7ED) else Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 14.dp)
                .background(if (isHot) Color(0xFFFF5722) else MaterialTheme.colorScheme.primary, RoundedCornerShape(1.5.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHot) Color(0xFFC2410C) else Color(0xFF374151)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryListRow(
    leafNode: CategoryNode,
    path: String,
    isHot: Boolean,
    onSelectLeaf: (CategoryNode, String) -> Unit
) {
    Card(
        onClick = { onSelectLeaf(leafNode, path) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isHot) Color(0xFFFFF7ED) else Color(0xFFF9FAFB)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isHot) Color(0xFFFFCC80) else Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                CategoryIconView(
                    iconUrl = leafNode.iconUrl,
                    iconName = leafNode.icon,
                    categoryName = leafNode.name,
                    size = 20.dp,
                    tint = if (isHot) Color(0xFFFF5722) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 10.dp)
                )
                val parenIdx = leafNode.name.indexOfAny(charArrayOf('(', '（'))
                val mainText = if (parenIdx > 0) leafNode.name.substring(0, parenIdx).trim() else leafNode.name
                val subText = if (parenIdx > 0) leafNode.name.substring(parenIdx).trim() else null

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = mainText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHot) Color(0xFFC2410C) else Color(0xFF1F2937),
                    )
                    if (subText != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isHot) Color(0xFFE65100) else Color(0xFF6B7280),
                            lineHeight = 15.sp,
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Select",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PublishSuccessView(
    message: String,
    postId: String,
    onViewPost: () -> Unit,
    onBackHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFE8F5E9),
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF43A047),
                modifier = Modifier.padding(16.dp).fillMaxSize()
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = message,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = "您可以随时在“我的发布”中编辑或下架该信息",
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onViewPost,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("查看已发布内容", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onBackHome,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("返回首页", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}

