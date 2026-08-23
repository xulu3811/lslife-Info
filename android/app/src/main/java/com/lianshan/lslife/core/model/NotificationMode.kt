package com.lianshan.lslife.core.model

enum class NotificationMode(val storageValue: String, val label: String) {
    RINGTONE("ringtone", "铃声"),
    VIBRATE("vibrate", "震动"),
    SILENT("silent", "静音");

    companion object {
        fun fromStorage(value: String?): NotificationMode =
            entries.firstOrNull { it.storageValue == value } ?: RINGTONE
    }
}
