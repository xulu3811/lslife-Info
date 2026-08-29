package com.qingyuan.lslife.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AddressNode(
    val code: String,
    val name: String,
    val children: List<AddressNode>? = null
)


@Singleton
class AddressManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rootNodes: List<AddressNode>? = null
    
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getAddresses(): List<AddressNode> = withContext(Dispatchers.IO) {
        if (rootNodes != null) {
            return@withContext rootNodes!!
        }

        try {
            context.assets.open("pcas.json").use { inputStream ->
                val jsonString = InputStreamReader(inputStream).readText()
                rootNodes = json.decodeFromString<List<AddressNode>>(jsonString)
                rootNodes!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
