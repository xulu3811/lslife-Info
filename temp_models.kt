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
