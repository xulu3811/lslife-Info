package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lianshan.lslife.core.model.SignInStatusResponse
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInBottomSheet(
    status: SignInStatusResponse,
    isSigningIn: Boolean,
    onDismiss: () -> Unit,
    onExecuteSignIn: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.lg)
                .padding(bottom = Dimens.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "已连续签到 ${status.continuousDays} 天",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                for (day in 1..7) {
                    val isToday = day == status.continuousDays + 1
                    val isPast = day <= status.continuousDays
                    val isDay7 = day == 7
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(if (isDay7) 1.5f else 1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isDay7) 50.dp else 40.dp)
                                .clip(if (isDay7) RoundedCornerShape(12.dp) else CircleShape)
                                .background(
                                    if (isPast) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else Color(0xFFF5F5F5)
                                )
                                .border(
                                    width = if (isToday) 2.dp else 0.dp,
                                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = if (isDay7) RoundedCornerShape(12.dp) else CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isDay7) "大奖" else "+$day",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPast) MaterialTheme.colorScheme.primary 
                                        else if (isToday) MaterialTheme.colorScheme.primary
                                        else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "第${day}天",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isPast || isToday) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.xxl))

            Button(
                onClick = onExecuteSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !status.isSignedToday && !isSigningIn,
                shape = RoundedCornerShape(24.dp)
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else if (status.isSignedToday) {
                    Text("今日已签到", fontWeight = FontWeight.Bold)
                } else {
                    Text("立即签到", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
