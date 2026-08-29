package com.qingyuan.lslife
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Test
class JsonTest {
    @Test
    fun testJson() {
        val jsonString = """{"pendingReviews":7, "pendingKyc":1}"""
        val data = Json.parseToJsonElement(jsonString).jsonObject
        val pendingKycStr = data["pendingKyc"]?.toString()
        val pendingKycInt = pendingKycStr?.toIntOrNull() ?: 0
        println("String: " + pendingKycStr)
        println("Int: " + pendingKycInt)
    }
}
