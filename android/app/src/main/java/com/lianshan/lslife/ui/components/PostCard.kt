package com.qingyuan.lslife.ui.components

import androidx.compose.runtime.Composable
import com.qingyuan.lslife.core.model.Post
import com.qingyuan.lslife.core.model.TradeMode

@Composable
fun PostListCard(
    post: Post,
    onClick: () -> Unit = {},
    onPhoneClick: () -> Unit = onClick,
    onChatClick: () -> Unit = onClick
) {
    InfoPublishCard(
        post = post,
        onClick = onClick,
        onPhoneClick = onPhoneClick,
        onChatClick = onChatClick
    )
}
