package com.lianshan.lslife.core.network

import kotlinx.serialization.Serializable

@Serializable
data class SensitiveWord(
    val id: String,
    val word: String,
    val category: String,
    val level: Int,
    val createdAt: String
)

@Serializable
data class SensitiveWordsResponse(
    val list: List<SensitiveWord>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

@Serializable
data class SensitiveWordRequest(
    val word: String,
    val category: String = "GENERAL",
    val level: Int = 1
)

@Serializable
data class ImportSensitiveWordsRequest(
    val words: List<SensitiveWordRequest>
)

@Serializable
data class ImportSensitiveWordsResponse(
    val added: Int
)
@Serializable
data class ModerationLogUser(
    val nickname: String,
    val phone: String
)

@Serializable
data class ModerationLog(
    val id: String,
    val action: String,
    val content: String,
    val matchedWords: String,
    val level: Int,
    val result: String,
    val createdAt: String,
    val user: ModerationLogUser? = null
)

@Serializable
data class ModerationLogsResponse(
    val list: List<ModerationLog>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
