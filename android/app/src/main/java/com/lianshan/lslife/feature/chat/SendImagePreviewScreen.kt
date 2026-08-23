package com.lianshan.lslife.feature.chat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.lianshan.lslife.ui.theme.PrimaryRed

@Composable
fun SendImagePreviewScreen(
    uris: List<Uri>,
    onClose: () -> Unit,
    onCropRequest: (index: Int, uri: Uri) -> Unit,
    onSend: (List<Uri>) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { uris.size })

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = uris[page],
                    contentDescription = "预览图片",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭", tint = Color.White)
                }
                Text(
                    text = "${pagerState.currentPage + 1} / ${uris.size}",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            // Bottom Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onCropRequest(pagerState.currentPage, uris[pagerState.currentPage]) }
                        .padding(8.dp)
                ) {
                    Icon(Icons.Filled.Crop, contentDescription = "裁剪", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑", color = Color.White, fontSize = 16.sp)
                }

                Button(
                    onClick = { onSend(uris) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text("发送 (${uris.size})", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}
