package com.lianshan.lslife.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.io.File
import java.security.MessageDigest
import java.util.UUID

object DeviceFingerprintUtil {

    /**
     * 获取设备唯一指纹 (Device ID)
     * 优先使用 Android ID，如果获取失败则生成 UUID
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val rawId = if (androidId.isNullOrEmpty() || androidId == "9774d56d682e549c") {
            // "9774d56d682e549c" is a known bugged Android ID on old devices
            getFallbackId(context)
        } else {
            androidId
        }
        return hash(rawId)
    }

    private fun getFallbackId(context: Context): String {
        val prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        var uuid = prefs.getString("device_uuid", null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            prefs.edit().putString("device_uuid", uuid).apply()
        }
        return uuid
    }

    /**
     * 检查设备风险等级 (模拟器或 Root)
     * 返回 "high" 表示高风险，"normal" 表示正常
     */
    fun getDeviceRisk(): String {
        return if (isEmulator() || isRooted()) "high" else "normal"
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    private fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
