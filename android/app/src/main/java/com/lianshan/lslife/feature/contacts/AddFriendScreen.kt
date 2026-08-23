package com.lianshan.lslife.feature.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.core.network.PublicUserResponse
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreen(
    onBack: () -> Unit,
    viewModel: AddFriendViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedUser by remember { mutableStateOf<PublicUserResponse?>(null) }
    var requestMessage by remember { mutableStateOf("") }

    LaunchedEffect(state.error, state.successMsg) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error!!)
            viewModel.clearMessage()
        }
        if (state.successMsg != null) {
            snackbarHostState.showSnackbar(state.successMsg!!)
            viewModel.clearMessage()
            selectedUser = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("添加朋友") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF4F5F7))
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("手机号/昵称") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.searchUser(searchQuery) }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (state.loading) {
                LoadingBox(modifier = Modifier.fillMaxWidth().height(100.dp))
            } else {
                LazyColumn {
                    items(state.searchResults) { user ->
                        SearchResultItem(user = user, onAddClick = { selectedUser = user })
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    if (selectedUser != null) {
        AlertDialog(
            onDismissRequest = { selectedUser = null },
            title = { Text("添加好友") },
            text = {
                Column {
                    Text("发送给 ${selectedUser?.nickname}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = requestMessage,
                        onValueChange = { requestMessage = it },
                        placeholder = { Text("我是...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.sendRequest(selectedUser!!.id, requestMessage)
                }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)) {
                    Text("发送")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUser = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SearchResultItem(
    user: PublicUserResponse,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(onClick = onAddClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!user.avatar.isNullOrBlank()) {
            AsyncImage(
                model = user.avatar,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = android.R.drawable.sym_def_app_icon)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = "Default Avatar", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.nickname ?: "未知用户", style = MaterialTheme.typography.titleMedium)
            if (!user.authLabel.isNullOrBlank()) {
                Text(user.authLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
        OutlinedButton(onClick = onAddClick, contentPadding = PaddingValues(horizontal = 12.dp)) {
            Text("添加")
        }
    }
}
