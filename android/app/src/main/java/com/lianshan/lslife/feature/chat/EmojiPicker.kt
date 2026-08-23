package com.lianshan.lslife.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val emojiList = listOf(
    "😀","😃","😄","😁","😆","😅","😂","🤣","🥲","☺️","😊","😇","🙂","🙃","😉","😌",
    "😍","🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🥸",
    "🤩","🥳","😏","😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢",
    "😭","😤","😠","😡","🤬","🤯","😳","🥵","🥶","😱","😨","😰","😥","😓","🤗","🤔",
    "🤭","🤫","🤥","😶","😐","😑","😬","🙄","😯","😦","😧","😮","😲","🥱","😴","🤤",
    "😪","😵","🤐","🥴","🤢","🤮","🤧","😷","🤒","🤕","🤑","🤠","😈","👿","👹","👺",
    "🤡","💩","👻","💀","👽","👾","🤖","🎃","😺","😸","😹","😻","😼","😽","🙀","😿",
    "😾","❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❤️‍🔥","❤️‍🩹","❣️","💕",
    "💞","💓","💗","💖","💘","💝","👍","👎","👏","🙌","👐","🤲","🤝","🙏","✍️","💅"
)

@Composable
fun EmojiPicker(
    modifier: Modifier = Modifier,
    onEmojiSelected: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(0xFFF6F6F6))
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(44.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(emojiList) { emoji ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Bottom Right Backspace Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp, 36.dp)
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .clickable { onBackspace() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "删除",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
