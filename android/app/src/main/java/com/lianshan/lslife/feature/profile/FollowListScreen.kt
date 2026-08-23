package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    userId: String,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: FollowListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = SnackbarHostState()

    LaunchedEffect(userId) {
        viewModel.initialize(userId)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\u5173\u6CE8/\u7C89\u4E1D", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u8FD4\u56DE")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = state.tabIndex,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[state.tabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = state.tabIndex == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text("\u5173\u6CE8 (${state.followingList.size})", fontWeight = if (state.tabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = state.tabIndex == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("\u7C89\u4E1D (${state.followersList.size})", fontWeight = if (state.tabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (state.loading) {
                LoadingBox()
            } else {
                val list = if (state.tabIndex == 0) state.followingList else state.followersList
                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("\u6682\u65E0\u6570\u636E", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(list) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .clickable { onOpenProfile(item.id) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    url = item.avatar,
                                    nickname = item.nickname,
                                    size = 50.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.nickname ?: "\u8FDE\u5C71\u7528\u6237", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    val label = item.authLabel ?: when (item.identityType) {
                                        "NORMAL", "none" -> "\u666E\u901A\u7528\u6237"
                                        "verified" -> "\u8BA4\u8BC1\u4E2A\u4EBA\u7528\u6237"
                                        "MERCHANT" -> "\u8BA4\u8BC1\u5546\u5BB6"
                                        else -> item.identityType ?: "\u666E\u901A\u7528\u6237"
                                    }
                                    Text(label, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                        }
                    }
                }
            }
        }
    }
}
