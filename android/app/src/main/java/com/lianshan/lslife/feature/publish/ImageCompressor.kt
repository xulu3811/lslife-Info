package com.qingyuan.lslife.feature.publish

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 选图后上传前压缩：目标 ≤ [maxBytes]（默认 1MB），最长边不超过 [maxSide]。
 */
object ImageCompressor {
    fun compress(
        context: Context,
        filePath: String,
        maxBytes: Int = 500 * 1024,
        maxSide: Int = 1920,
    ): ByteArray {
        val file = java.io.File(filePath)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        java.io.FileInputStream(file).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth == -1 || bounds.outHeight == -1) error("无法读取图片")

        var sample = 1
        val longer = max(bounds.outWidth.coerceAtLeast(1), bounds.outHeight.coerceAtLeast(1))
        while (longer / sample > maxSide * 2) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        var bitmap = java.io.FileInputStream(file).use { BitmapFactory.decodeStream(it, null, opts) }
            ?: error("无法解码图片")

        try {
            val scale = max(bitmap.width, bitmap.height).toFloat() / maxSide
            if (scale > 1f) {
                val w = (bitmap.width / scale).roundToInt().coerceAtLeast(1)
                val h = (bitmap.height / scale).roundToInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
                if (scaled !== bitmap) {
                    bitmap.recycle()
                    bitmap = scaled
                }
            }

            var quality = 90
            var bytes = encodeJpeg(bitmap, quality)
            while (bytes.size > maxBytes && quality > 45) {
                quality -= 10
                bytes = encodeJpeg(bitmap, quality)
            }

            var guard = 0
            while (bytes.size > maxBytes && guard < 5) {
                guard++
                val w = (bitmap.width * 0.8f).roundToInt().coerceAtLeast(640)
                val h = (bitmap.height * 0.8f).roundToInt().coerceAtLeast(640)
                if (w >= bitmap.width && h >= bitmap.height) break
                val next = Bitmap.createScaledBitmap(bitmap, w, h, true)
                bitmap.recycle()
                bitmap = next
                bytes = encodeJpeg(bitmap, 72)
            }
            return bytes
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    private fun encodeJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(40, 95), out)
        return out.toByteArray()
    }
}
