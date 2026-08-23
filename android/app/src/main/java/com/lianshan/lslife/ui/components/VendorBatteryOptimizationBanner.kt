package com.lianshan.lslife.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VendorBatteryOptimizationBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    
    var isDismissed by remember { 
        mutableStateOf(prefs.getBoolean("banner_dismissed_vendor_battery", false)) 
    }

    val manufacturer = remember { Build.MANUFACTURER.uppercase() }
    val isOppoOrVivo = manufacturer.contains("OPPO") || manufacturer.contains("VIVO") || manufacturer.contains("REALME") || manufacturer.contains("ONEPLUS")

    // Check if battery optimization is already ignored
    val isIgnoringBatteryOptimizations = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        } else {
            true
        }
    }

    // Do NOT show if already ignored, not an OPPO/VIVO device, or user manually dismissed it
    if (!isOppoOrVivo || isIgnoringBatteryOptimizations || isDismissed) {
        return
    }

    Surface(
        color = Color(0xFFFFF3E0), // Subtle warm amber tint
        contentColor = Color(0xFFE65100),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (ignored: Exception) {}
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "为保证随时收到交易消息，建议开启后台运行权限 ($manufacturer 专属优化)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD84315),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // Dismiss button ('X')
            IconButton(
                onClick = {
                    isDismissed = true
                    prefs.edit().putBoolean("banner_dismissed_vendor_battery", true).apply()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭提醒",
                    tint = Color(0xFF8D6E63),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

