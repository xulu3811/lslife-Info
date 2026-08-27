import re

with open('android/app/src/main/java/com/lianshan/lslife/core/network/ApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

replacement = '''    @DELETE("admin/sensitive-words/{id}")
    suspend fun deleteSensitiveWord(
        @Path("id") id: String
    ): ApiEnvelope<kotlinx.serialization.json.JsonObject>

    @GET("admin/moderation-logs")
    suspend fun getModerationLogs(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): ApiEnvelope<ModerationLogsResponse>'''

content = re.sub(r'@DELETE\("admin/sensitive-words/\{id\}"\)\s*suspend fun deleteSensitiveWord\(\s*@Path\("id"\) id: String\s*\): ApiEnvelope<kotlinx\.serialization\.json\.JsonObject>', replacement, content)

with open('android/app/src/main/java/com/lianshan/lslife/core/network/ApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
