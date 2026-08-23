import os

code = """package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.ui.components.LoadingBox

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
                title = { Text("关注/粉丝", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                    text = { Text("关注 (${state.followingList.size})", fontWeight = if (state.tabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = state.tabIndex == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("粉丝 (${state.followersList.size})", fontWeight = if (state.tabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (state.loading) {
                LoadingBox()
            } else {
                val list = if (state.tabIndex == 0) state.followingList else state.followersList
                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无数据", color = Color.Gray)
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
                                AsyncImage(
                                    model = item.avatar,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.nickname ?: "连山用户", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    if (!item.authLabel.isNullOrEmpty()) {
                                        Text(item.authLabel, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                
                                Button(
                                    onClick = { viewModel.toggleFollow(item.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (item.isFollowing) Color(0xFFF5F5F5) else Color(0xFFFF2442),
                                        contentColor = if (item.isFollowing) Color(0xFF999999) else Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (item.isFollowing) "已关注" else "关注", fontSize = 12.sp)
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
"""

with open(r'D:\LsLife\android\app\src\main\java\com\lianshan\lslife\feature\profile\FollowListScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)
