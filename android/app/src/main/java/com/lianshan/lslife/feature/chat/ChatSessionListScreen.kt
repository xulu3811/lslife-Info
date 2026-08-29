package com.qingyuan.lslife.feature.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.qingyuan.lslife.core.database.LocalConversationEntity
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.theme.PrimaryRed
import androidx.compose.foundation.clickable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatSessionListScreen(
    onNavigateToChat: (sessionId: String, targetUserId: String, targetName: String) -> Unit,
    viewModel: ChatSessionListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedSessionForAction by remember { mutableStateOf<LocalConversationEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    

    Scaffold(
        
        containerColor = androidx.compose.ui.graphics.Color(0xFFF3F5F8),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "消息", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFF3F5F8),
                    scrolledContainerColor = androidx.compose.ui.graphics.Color(0xFFF3F5F8)
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (state.loading && state.sessions.isEmpty()) {
                LoadingBox(Modifier.weight(1f).fillMaxWidth())
                return@Column
            }

            if (state.sessions.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("暂无消息", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                return@Column
            }

            androidx.compose.material3.Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp).padding(bottom = 16.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = androidx.compose.ui.graphics.Color.White,
                shadowElevation = 0.dp
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                items(state.sessions, key = { it.conversationId }) { session ->
                    ChatSessionItem(
                        session = session, 
                        isPinned = state.pinnedIds.contains(session.conversationId),
                        onClick = {
                            // In real app, we need to know the peer user ID. For LocalConversationEntity, 
                            // we don't have peerId directly if we didn't save it. 
                            // Wait, LocalConversationEntity doesn't have `peerId`. Let's just pass targetId or some ID.
                            // Assuming `conversationId` is composed of `conv_${myId}_${peerId}_...` for now, 
                            // or better yet we should add `peerId` to `LocalConversationEntity`.
                            // For compilation, let's just use empty string or parse it.
                            val targetId = session.peerId
                            val targetName = session.peerName
                            onNavigateToChat(session.conversationId, targetId, targetName)
                        },
                        onLongClick = {
                            selectedSessionForAction = session
                        }
                    )
                }
            }
            } // End of Surface
        }
    }

    if (selectedSessionForAction != null) {
        val session = selectedSessionForAction!!
        ModalBottomSheet(
            onDismissRequest = { selectedSessionForAction = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "会话操作: ${session.peerName}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                val isPinned = state.pinnedIds.contains(session.conversationId)
                ListItem(
                    headlineContent = { Text(if (isPinned) "取消置顶" else "置顶会话") },
                    leadingContent = { 
                        Icon(
                            imageVector = Icons.Filled.PushPin, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    modifier = Modifier.clickable {
                        viewModel.togglePinSession(session.conversationId)
                        selectedSessionForAction = null
                    }
                )
                ListItem(
                    headlineContent = { Text("删除会话", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { 
                        Icon(
                            imageVector = Icons.Filled.Delete, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        ) 
                    },
                    modifier = Modifier.clickable {
                        viewModel.deleteSession(session.conversationId)
                        selectedSessionForAction = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatSessionItem(
    session: LocalConversationEntity, 
    isPinned: Boolean, 
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isPinned) androidx.compose.ui.graphics.Color(0xFFF8F9FA) else androidx.compose.ui.graphics.Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        com.qingyuan.lslife.ui.components.GoogleAvatar(
            url = session.peerAvatar,
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.peerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isPinned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            val displayMessage = if (session.lastMessage.startsWith("http") && (session.lastMessage.contains("chat_imgs") || session.lastMessage.matches(Regex(".*\\.(jpg|jpeg|png|gif|webp)(\\?.*)?", RegexOption.IGNORE_CASE)))) {
                "[图片]"
            } else if (session.lastMessage.startsWith("http") && (session.lastMessage.contains("chat_audio") || session.lastMessage.matches(Regex(".*\\.(mp3|m4a|wav|aac|ogg)(\\?.*)?", RegexOption.IGNORE_CASE)))) {
                "[语音]"
            } else if (session.lastMessage.trim().startsWith("{") && session.lastMessage.contains("\"id\"") && session.lastMessage.contains("\"title\"")) {
                "[商品/服务]"
            } else if (session.lastMessage.trim().startsWith("{") && session.lastMessage.contains("\"lat\"") && session.lastMessage.contains("\"lng\"")) {
                "[位置]"
            } else {
                session.lastMessage
            }
            Text(
                text = displayMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Time and Unread
        Column(horizontalAlignment = Alignment.End) {
            val date = java.util.Date(session.lastMessageAt)
            val timeStr = SimpleDateFormat("HH:mm", Locale.US).format(date)
            Text(
                text = timeStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (session.unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(PrimaryRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (session.unreadCount > 99) "99+" else session.unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
