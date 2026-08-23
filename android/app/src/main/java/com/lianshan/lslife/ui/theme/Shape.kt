package com.lianshan.lslife.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material3 弹层（ModalBottomSheet / AlertDialog）默认使用 shapes.extraLarge。
 * 切勿写成 RoundedCornerShape(50)：无 .dp 时表示 percent=50，宽容器会变成巨大半圆遮罩。
 * 胶囊按钮请在组件内显式使用 RoundedCornerShape(999.dp)。
 */
val LsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
