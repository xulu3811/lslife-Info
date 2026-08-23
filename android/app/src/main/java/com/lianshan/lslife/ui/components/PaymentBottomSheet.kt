package com.lianshan.lslife.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    amount: Double,
    onDismissRequest: () -> Unit,
    onPaymentMethodSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.md, vertical = Dimens.sm)
            ) {
                Text(
                    text = "确认付款",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭")
                }
            }

            Spacer(modifier = Modifier.height(Dimens.lg))

            Text(
                text = "¥%.2f".format(amount),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.xl)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {
                item {
                    PaymentMethodRow(
                        title = "微信支付",
                        subtitle = "推荐使用",
                        icon = Icons.Filled.Payment,
                        iconColor = Color(0xFF07C160),
                        onClick = { onPaymentMethodSelected("WECHAT") }
                    )
                }
                item {
                    PaymentMethodRow(
                        title = "支付宝",
                        subtitle = "数亿用户都在用，安全可托付",
                        icon = Icons.Filled.AccountBalanceWallet,
                        iconColor = Color(0xFF1677FF),
                        onClick = { onPaymentMethodSelected("ALIPAY") }
                    )
                }
                item {
                    PaymentMethodRow(
                        title = "积分支付",
                        subtitle = "可用积分抵扣",
                        icon = Icons.Filled.Star,
                        iconColor = Color(0xFFFFB800),
                        onClick = { onPaymentMethodSelected("POINTS") }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.md))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
