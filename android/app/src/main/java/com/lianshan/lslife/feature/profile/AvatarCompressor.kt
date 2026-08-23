package com.lianshan.lslife.feature.profile

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object AvatarCompressor {
    /**
     * Compress the provided cropped bitmap into a ByteArray of JPEG data,
     * ensuring the maximum size is within 100KB and maximum dimension is 500px.
     */
    fun compressAvatar(bitmap: Bitmap, maxBytes: Int = 100 * 1024, maxSide: Int = 500): ByteArray {
        var bmp = bitmap
        try {
            // Resize if needed
            val scale = max(bmp.width, bmp.height).toFloat() / maxSide
            if (scale > 1f) {
                val w = (bmp.width / scale).roundToInt().coerceAtLeast(1)
                val h = (bmp.height / scale).roundToInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
                if (scaled !== bmp && bmp !== bitmap) {
                    bmp.recycle()
                }
                bmp = scaled
            }

            // Use binary search to find the optimal quality that fits within maxBytes
            var low = 10
            var high = 100
            var bestBytes = encodeJpeg(bmp, high)

            if (bestBytes.size <= maxBytes) {
                return bestBytes
            }

            bestBytes = encodeJpeg(bmp, low)

            while (low <= high) {
                val mid = low + (high - low) / 2
                val bytes = encodeJpeg(bmp, mid)
                if (bytes.size <= maxBytes) {
                    bestBytes = bytes
                    low = mid + 1 // Try for higher quality
                } else {
                    high = mid - 1 // Need smaller size
                }
            }
            return bestBytes
        } finally {
            if (bmp !== bitmap && !bmp.isRecycled) {
                bmp.recycle()
            }
        }
    }

    private fun encodeJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(10, 100), out)
        return out.toByteArray()
    }
}
