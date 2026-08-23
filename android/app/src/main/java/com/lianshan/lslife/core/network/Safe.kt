package com.lianshan.lslife.core.network

import com.lianshan.lslife.core.model.ApiEnvelope

import retrofit2.HttpException
import org.json.JSONObject

class ApiException(val code: Int, message: String) : Exception(message)

/** 解包 ApiEnvelope, 业务码非 0 抛 ApiException, 网络异常向上抛出。 拦截 HTTP 错误并解析后端返回的 JSON 错误信息。 */
suspend fun <T> safeCall(block: suspend () -> ApiEnvelope<T>): Result<T> = runCatching {
    val env = block()
    if (env.code != 0 || env.data == null) throw ApiException(env.code, env.message)
    env.data
}.recoverCatching { e ->
    if (e is HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        if (!errorBody.isNullOrBlank()) {
            val json = runCatching { JSONObject(errorBody) }.getOrNull()
            val message = json?.optString("message")?.takeIf { it.isNotBlank() } ?: e.message()
            throw ApiException(e.code(), message)
        }
    }
    throw e
}
