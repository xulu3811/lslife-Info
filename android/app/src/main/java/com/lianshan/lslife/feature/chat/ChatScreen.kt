package com.lianshan.lslife.feature.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import android.util.Base64
import android.util.Log
import androidx.compose.ui.res.painterResource
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.database.LocalMessageEntity
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.theme.PrimaryRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: String,
    targetUserId: String,
    targetName: String,
    initPostId: String? = null,
    onNavigateToProfile: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val text by viewModel.inputText.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedMessageIds.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val recorderHelper = remember { AudioRecorderHelper(context) }
    var isVoiceMode by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isCancelled by remember { mutableStateOf(false) }
    var amplitude by remember { mutableStateOf(0) }
    val audioManager = remember { AudioManager(context) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var showEmojiPanel by remember { mutableStateOf(false) }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(100)
            amplitude = recorderHelper.getMaxAmplitude()
        }
    }
    
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isVoiceMode = true
        } else {
            android.widget.Toast.makeText(context, "需要麦克风权限才能发送语音", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose { audioManager.release() }
    }

    var previewUris by remember { mutableStateOf<List<android.net.Uri>?>(null) }
    var cropIndex by remember { mutableIntStateOf(-1) }

    val cropImageLauncher = rememberLauncherForActivityResult(com.canhub.cropper.CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            if (uriContent != null && cropIndex >= 0 && previewUris != null) {
                val mutList = previewUris!!.toMutableList()
                mutList[cropIndex] = uriContent
                previewUris = mutList
            }
        }
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)) { uris ->
        if (uris.isNotEmpty()) {
            previewUris = uris
        }
    }

    LaunchedEffect(sessionId, targetUserId, initPostId) {
        viewModel.initSession(sessionId, targetUserId, initPostId)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    var selectedMsgForAction by remember { mutableStateOf<LocalMessageEntity?>(null) }
    
    if (selectedMsgForAction != null) {
        AlertDialog(
            onDismissRequest = { selectedMsgForAction = null },
            title = { Text("消息操作") },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (selectedMsgForAction?.senderId == state.currentUserId && isWithinOneMinute(selectedMsgForAction?.createdAt ?: 0)) {
                        Text(
                            text = "撤回消息",
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedMsgForAction?.let { viewModel.recallMessage(it.msgId) }
                                selectedMsgForAction = null
                            }.padding(8.dp),
                            color = PrimaryRed,
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = "多选",
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedMsgForAction?.let { viewModel.toggleSelection(it.msgId) }
                            selectedMsgForAction = null
                        }.padding(8.dp),
                        fontSize = 18.sp
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedMsgForAction = null }) {
                    Text("取消", color = Color.Gray)
                }
            }
        )
    }

    var showClearConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error!!)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedIds.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "取消选择")
                        }
                        Text(
                            text = "已选择 ${selectedIds.size} 条",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.deleteSelectedMessages() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = PrimaryRed)
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = targetName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFFFF0E5),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "五星店铺",
                                        color = PrimaryRed,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    color = Color(0xFFF4F5F7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "10年老店",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { onNavigateToProfile(targetUserId) }) {
                            Icon(Icons.Filled.Home, contentDescription = "店铺")
                        }
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "设置")
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFE0E0E0)))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mic Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF6F6F6), RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            return@awaitEachGesture
                                        }
                                        
                                        isRecording = true
                                        isCancelled = false
                                        recorderHelper.startRecording()
                                        
                                        var upEvent: androidx.compose.ui.input.pointer.PointerInputChange? = null
                                        do {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull()
                                            if (change != null) {
                                                if (change.position.y < -150f) {
                                                    isCancelled = true
                                                } else {
                                                    isCancelled = false
                                                }
                                                if (!change.pressed) {
                                                    upEvent = change
                                                }
                                            }
                                        } while (upEvent == null)
                                        
                                        isRecording = false
                                        val result = recorderHelper.stopRecording()
                                        if (isCancelled) {
                                            result?.first?.delete()
                                        } else if (result != null) {
                                            viewModel.sendVoice(result.first.absolutePath, result.second)
                                        } else {
                                            // Handle < 1s scenario natively (toast on UI thread if possible, wait, Toast is fine here since it's composition side effects, but better in Main thread)
                                            // Oh wait, pointerInput is in UI thread context so Toast is fine.
                                            android.widget.Toast.makeText(context, "说话时间太短", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Mic, contentDescription = "语音", tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                        }
                        
                        // Main Input Pill
                        androidx.compose.foundation.text.BasicTextField(
                            value = text,
                            onValueChange = { viewModel.updateInputText(it) },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        showEmojiPanel = false
                                    }
                                },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(Color(0xFFF6F6F6), CircleShape)
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (text.isEmpty()) {
                                            Text("请输入...", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                                        }
                                        innerTextField()
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.Face,
                                        contentDescription = "表情",
                                        tint = if (showEmojiPanel) PrimaryRed else Color.Gray,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) { 
                                                if (showEmojiPanel) {
                                                    showEmojiPanel = false
                                                    focusRequester.requestFocus()
                                                    keyboardController?.show()
                                                } else {
                                                    keyboardController?.hide()
                                                    showEmojiPanel = true
                                                }
                                            }
                                    )
                                }
                            }
                        )
                        
                        // Bag Icon
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { 
                                android.widget.Toast.makeText(context, "选择商品/服务", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "商品", tint = Color.DarkGray, modifier = Modifier.size(24.dp))
                            Text("商品", fontSize = 10.sp, color = Color.DarkGray)
                        }
                        
                        // Plus / Send Button
                        Crossfade(targetState = text.isNotBlank(), label = "send_anim", animationSpec = tween(200)) { hasText ->
                            if (hasText) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PrimaryRed, CircleShape)
                                        .clickable { 
                                            viewModel.sendMessage(text)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = "发送", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFF6F6F6), CircleShape)
                                        .clickable { 
                                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "更多", tint = Color.DarkGray, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                    AnimatedVisibility(visible = showEmojiPanel) {
                        EmojiPicker(
                            onEmojiSelected = { emoji ->
                                viewModel.updateInputText(text + emoji)
                            },
                            onBackspace = {
                                if (text.isNotEmpty()) {
                                    val codePoint = java.lang.Character.codePointBefore(text, text.length)
                                    val charCount = java.lang.Character.charCount(codePoint)
                                    viewModel.updateInputText(text.substring(0, text.length - charCount))
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (state.loading && state.messages.isEmpty()) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF4F5F7)), // 极浅灰
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
            reverseLayout = true
        ) {
            val reversedMessages = state.messages.reversed()
            itemsIndexed(reversedMessages) { index, message ->
                val isMe = message.senderId == state.currentUserId
                val showTime = if (index == reversedMessages.size - 1) {
                    true
                } else {
                    val prevMsg = reversedMessages[index + 1]
                    val currTime = message.createdAt
                    val prevTime = prevMsg.createdAt
                    (currTime - prevTime) > 5 * 60 * 1000 // 5 minutes gap
                }
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (showTime) {
                        Text(
                            text = formatTime(message.createdAt),
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 12.dp)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (selectedIds.isNotEmpty()) {
                            Checkbox(
                                checked = selectedIds.contains(message.msgId),
                                onCheckedChange = { viewModel.toggleSelection(message.msgId) },
                                modifier = Modifier.padding(end = 8.dp),
                                colors = CheckboxDefaults.colors(checkedColor = PrimaryRed)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            if (message.msgType.equals("RECALLED", ignoreCase = true)) {
                                RecalledMessagePill(text = message.content)
                            } else {
                                ChatBubble(
                                    message = message,
                                    isMe = isMe,
                                    audioManager = audioManager,
                                    onClick = {
                                        if (selectedIds.isNotEmpty()) {
                                            viewModel.toggleSelection(message.msgId)
                                        }
                                    },
                                    onLongPress = {
                                        if (selectedIds.isEmpty()) {
                                            selectedMsgForAction = message
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (!state.isFriend) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xFFFFF0E5),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "对方还不是你的好友，请注意交易安全",
                                color = PrimaryRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清空聊天记录") },
            text = { Text("确定要清空与该用户的聊天记录吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    // viewModel.clearChatHistory()
                    showClearConfirm = false
                }) { Text("确定", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("取消") }
            }
        )
    }

    if (isRecording) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val h = (amplitude / 32767f * 60).toInt().coerceIn(24, 60).dp
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(h))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isCancelled) "松开 取消" else "手指上滑 取消",
                        color = if (isCancelled) PrimaryRed else Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    }

    if (previewUris != null) {
        SendImagePreviewScreen(
            uris = previewUris!!,
            onClose = { previewUris = null },
            onCropRequest = { index, uri ->
                cropIndex = index
                val cropOptions = com.canhub.cropper.CropImageContractOptions(
                    uri, 
                    com.canhub.cropper.CropImageOptions(
                        imageSourceIncludeGallery = false,
                        imageSourceIncludeCamera = false,
                        activityTitle = "裁剪图片",
                        cropMenuCropButtonTitle = "完成"
                    )
                )
                cropImageLauncher.launch(cropOptions)
            },
            onSend = { finalUris ->
                previewUris = null
                scope.launch(Dispatchers.IO) {
                    finalUris.forEach { uri ->
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri) ?: return@forEach
                            val fileName = "chat_temp_${java.util.UUID.randomUUID()}.jpg"
                            val cacheFile = java.io.File(context.cacheDir, fileName)
                            cacheFile.outputStream().use { output ->
                                inputStream.copyTo(output)
                            }
                            val absolutePath = cacheFile.absolutePath
                            withContext(Dispatchers.Main) {
                                viewModel.uploadAndSendImage(absolutePath)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun RecalledMessagePill(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.Gray.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: LocalMessageEntity,
    isMe: Boolean,
    audioManager: AudioManager,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit
) {
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMe) 16.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 16.dp
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(color = if (isMe) PrimaryRed else MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (message.msgType == "IMAGE") {
                var model: Any = message.content
                if (message.content.startsWith("base64:")) {
                    try {
                        model = Base64.decode(message.content.removePrefix("base64:"), Base64.DEFAULT)
                    } catch (e: Exception) {
                        Log.e("ChatScreen", "Base64 decode failed", e)
                    }
                }
                coil.compose.AsyncImage(
                    model = model,
                    contentDescription = "图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.stat_notify_error)
                )
            } else if (message.msgType == "POST_CARD") {
                // Parse JSON
                val cardData = try {
                    org.json.JSONObject(message.content)
                } catch (e: Exception) {
                    null
                }
                if (cardData != null) {
                    Column(
                        modifier = Modifier.width(220.dp)
                    ) {
                        val imageUrl = cardData.optString("image")
                        if (imageUrl.isNotBlank()) {
                            coil.compose.AsyncImage(
                                model = imageUrl,
                                contentDescription = "商品缩略图",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = cardData.optString("title"),
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${cardData.optDouble("price", 0.0)}",
                            color = if (isMe) Color.White else PrimaryRed,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                } else {
                    Text("不支持的卡片消息", color = Color.Gray)
                }
            } else if (message.msgType == "LOCATION") {
                val locData = try { org.json.JSONObject(message.content) } catch (e: Exception) { null }
                if (locData != null) {
                    Column(modifier = Modifier.width(220.dp)) {
                        Text(
                            text = locData.optString("name", "位置信息"),
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = locData.optString("address", "未知地址"),
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Place, contentDescription = "Map Placeholder", tint = PrimaryRed, modifier = Modifier.size(36.dp))
                        }
                    }
                } else {
                    Text("不支持的位置卡片", color = Color.Gray)
                }
            } else if (message.msgType == "VOICE" || message.msgType == "AUDIO") {
                val voiceData = try {
                    org.json.JSONObject(message.content)
                } catch (e: Exception) { null }
                if (voiceData != null) {
                    val duration = voiceData.optInt("duration", 1)
                    val url = voiceData.optString("url")
                    val currentPlayingUrl by audioManager.currentPlayingUrl.collectAsStateWithLifecycle()
                    val isPlaying by audioManager.isPlaying.collectAsStateWithLifecycle()
                    val isThisPlaying = (currentPlayingUrl == url && isPlaying)
                    
                    Row(
                        modifier = Modifier
                            .widthIn(min = 60.dp, max = (60 + duration * 5).dp)
                            .clickable { audioManager.playAudio(url) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        if (isMe) {
                            Text("${duration}\"", color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                if (isThisPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                if (isThisPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${duration}\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Text("语音格式错误", color = Color.Gray)
                }
            } else {
                Text(
                    text = message.content,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private fun isWithinOneMinute(createdAt: Long): Boolean {
    val elapsed = System.currentTimeMillis() - createdAt
    return elapsed in 0..60_000
}

private fun formatTime(createdAt: Long): String {
    return try {
        val date = java.util.Date(createdAt)
        val outFormat = SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = TimeZone.getDefault() }
        outFormat.format(date)
    } catch (e: Exception) { "" }
}
