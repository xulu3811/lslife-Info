package com.qingyuan.lslife.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.data.AuthRepository
import com.qingyuan.lslife.core.data.TokenStore
import com.qingyuan.lslife.core.database.MerchantDao
import com.qingyuan.lslife.core.model.ThemeMode
import com.qingyuan.lslife.core.model.NotificationMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationMode: NotificationMode = NotificationMode.RINGTONE,
    val clearingCache: Boolean = false,
    val loggedOut: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val merchantDao: MerchantDao,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    init {
        viewModelScope.launch {
            combine(
                tokenStore.themeModeFlow,
                tokenStore.notificationModeFlow,
            ) { themeMode, notificationMode ->
                themeMode to notificationMode
            }.collect { (themeMode, notificationMode) ->
                _state.update {
                    it.copy(
                        themeMode = themeMode,
                        notificationMode = notificationMode,
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { tokenStore.setThemeMode(mode) }
    }

    fun setNotificationMode(mode: NotificationMode) {
        viewModelScope.launch {
            tokenStore.setNotificationMode(mode)
            _state.update {
                it.copy(message = "消息通知模式已设为 ${mode.label}")
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _state.update { it.copy(clearingCache = true) }
            runCatching { merchantDao.clear() }
                .onSuccess {
                    _state.update { it.copy(clearingCache = false, message = "本地缓存已清理") }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            clearingCache = false,
                            message = error.message ?: "缓存清理失败",
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.update { it.copy(loggedOut = true) }
        }
    }
}
