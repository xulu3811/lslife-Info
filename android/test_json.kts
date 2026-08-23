import kotlinx.serialization.json.*

fun main() {
    val jsonString = """{"pendingKyc": 1}"""
    val data = Json.parseToJsonElement(jsonString).jsonObject
    val pendingKycElement = data["pendingKyc"]
    println("pendingKycElement: ${pendingKycElement}")
    println("isPrimitive: ${pendingKycElement is JsonPrimitive}")
    println("content: ${pendingKycElement?.jsonPrimitive?.content}")
    println("toIntOrNull: ${pendingKycElement?.jsonPrimitive?.content?.toIntOrNull()}")
}
