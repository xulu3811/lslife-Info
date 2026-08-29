package com.qingyuan.lslife.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qingyuan.lslife.core.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.qingyuan.lslife.core.model.NotificationMode
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "ls_session")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val tokenKey = stringPreferencesKey("token")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val notificationModeKey = stringPreferencesKey("notification_mode")
    private val keepAliveKey = booleanPreferencesKey("keep_alive_im")

    var cachedToken: String? = null
        private set

    init {
        // Collect token immediately on application start to ensure interceptor has it
        kotlinx.coroutines.DelicateCoroutinesApi::class
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch {
            context.dataStore.data.map { it[tokenKey] }.collect { cachedToken = it }
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[tokenKey] }
    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map {
        ThemeMode.fromStorage(it[themeModeKey])
    }
    val notificationModeFlow: Flow<NotificationMode> = context.dataStore.data.map {
        NotificationMode.fromStorage(it[notificationModeKey])
    }
    val keepAliveFlow: Flow<Boolean> = context.dataStore.data.map {
        it[keepAliveKey] ?: true // default to true
    }

    suspend fun current(): String? = tokenFlow.first()

    suspend fun save(token: String) {
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.remove(tokenKey) }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeModeKey] = mode.storageValue }
    }

    suspend fun setNotificationMode(mode: NotificationMode) {
        context.dataStore.edit { it[notificationModeKey] = mode.storageValue }
    }
    
    suspend fun setKeepAlive(enabled: Boolean) {
        context.dataStore.edit { it[keepAliveKey] = enabled }
    }
}
