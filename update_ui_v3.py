import re

with open("android/app/src/main/java/com/lianshan/lslife/feature/publish/PublishScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

start_marker = "/** 多级分类级联选择 BottomSheet */"
start_idx = content.find(start_marker)

match = re.search(r'@Composable\s*fun PublishSuccessView\(', content)
if not match or start_idx == -1:
    print("Could not find markers")
    exit(1)
end_idx = match.start()

new_ui = """/** 多级分类级联选择 BottomSheet (Material 3 视觉优化版) */
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
    val publishableTree = remember(categoryTree) { categoryTree.filter { it.id != "all" } }
    var selectedLevel1 by remember(publishableTree, preSelectedLevel1Id) { 
        mutableStateOf<CategoryNode?>(publishableTree.find { it.id == preSelectedLevel1Id } ?: publishableTree.firstOrNull()) 
    }
    var searchQuery by remember { mutableStateOf("") }

    val allLeavesWithPaths = remember(publishableTree) {
        val list = mutableListOf<Pair<CategoryNode, String>>()
        fun traverse(node: CategoryNode, currentPath: String) {
            if (node.isLeaf) {
                list.add(node to currentPath)
            } else {
                node.children.forEach { child -> traverse(child, if (currentPath.isEmpty()) child.name else "$currentPath > ${child.name}") }
            }
        }
        publishableTree.forEach { root ->
            if (root.isLeaf) list.add(root to root.name)
            else root.children.forEach { l2 -> traverse(l2, "${root.name} > ${l2.name}") }
        }
        list
    }

    val searchResults = remember(allLeavesWithPaths, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            allLeavesWithPaths.filter { (node, path) -> node.name.lowercase().contains(q) || path.lowercase().contains(q) }
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
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f) // 适当调高一点防止底部被遮挡
                .padding(horizontal = 16.dp)
        ) {
            // Header (字体调小到 18.sp，间距微调)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("选择发布分类", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.6f), CircleShape)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            // Compact Search Bar (高度从默认 56dp 降至 36dp，圆角胶囊形)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("搜索分类，例: 手机 / 租房 / 兼职", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f))
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Content
            when {
                isLoading && publishableTree.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null && publishableTree.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = onRetry) { Text("重新加载") }
                        }
                    }
                }
                publishableTree.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无可用分类", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                searchQuery.isNotBlank() -> {
                    if (searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("未找到相关分类", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 48.dp)
                        ) {
                            item { CategorySectionHeader(title = "🔍 搜索结果 (${searchResults.size})", isHot = false) }
                            items(searchResults, key = { "search_" + it.first.id }) { (leaf, path) ->
                                CategoryListRow(leafNode = leaf, path = path, isHot = false, onSelectLeaf = onSelectLeaf)
                            }
                        }
                    }
                }
                else -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left Sidebar (Level 1)
                        LazyColumn(
                            modifier = Modifier
                                .width(96.dp) // 稍微变窄，给右侧留更多空间
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(publishableTree, key = { it.id }) { node ->
                                val isSelected = selectedLevel1?.id == node.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable { selectedLevel1 = node }
                                        .padding(vertical = 12.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = node.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Right Content (Level 2 & 3)
                        val l2Nodes = selectedLevel1?.children.orEmpty()
                        val directLeaves = remember(l2Nodes) { l2Nodes.filter { it.isLeaf } }
                        val subGroups = remember(l2Nodes) { l2Nodes.filter { !it.isLeaf } }

                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(bottom = 64.dp) // 增加底部 padding 防止截断
                        ) {
                            if (directLeaves.isNotEmpty()) {
                                item { CategorySectionHeader(title = "直达分类", isHot = false) }
                                val directPairs = directLeaves.map { it to "${selectedLevel1?.name ?: ""} > ${it.name}" }
                                items(directPairs, key = { "direct_" + it.first.id }) { (leaf, path) ->
                                    CategoryListRow(leafNode = leaf, path = path, isHot = false, onSelectLeaf = onSelectLeaf)
                                }
                                item { Spacer(modifier = Modifier.height(12.dp)) }
                            }

                            subGroups.forEach { group ->
                                item(key = "group_header_${group.id}") { CategorySectionHeader(title = group.name, isHot = false) }
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
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (isHot) Color(0xFFE65100) else MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
    )
}

@Composable
private fun CategoryListRow(
    leafNode: CategoryNode,
    path: String,
    isHot: Boolean,
    onSelectLeaf: (CategoryNode, String) -> Unit
) {
    Surface(
        onClick = { onSelectLeaf(leafNode, path) },
        shape = RoundedCornerShape(12.dp),
        color = if (isHot) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconView(
                iconUrl = leafNode.iconUrl,
                iconName = leafNode.icon,
                categoryName = leafNode.name,
                size = 20.dp, // 图片稍微减小，让布局更紧凑
                tint = if (isHot) Color(0xFFE65100) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 10.dp)
            )
            
            val parenIdx = leafNode.name.indexOfAny(charArrayOf('(', '\uFF08'))
            val mainText = if (parenIdx > 0) leafNode.name.substring(0, parenIdx).trim() else leafNode.name
            val subText = if (parenIdx > 0) leafNode.name.substring(parenIdx).trim() else null

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mainText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isHot) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                )
                if (subText != null) {
                    Text(
                        text = subText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

"""

new_content = content[:start_idx] + new_ui + content[end_idx:]

with open("android/app/src/main/java/com/lianshan/lslife/feature/publish/PublishScreen.kt", "w", encoding="utf-8") as f:
    f.write(new_content)

print("UI successfully updated!")
