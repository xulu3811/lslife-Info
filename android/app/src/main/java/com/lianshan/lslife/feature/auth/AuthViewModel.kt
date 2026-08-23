package com.lianshan.lslife.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianshan.lslife.core.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode { Login, Register }

data class LoginUiState(
    val mode: AuthMode = AuthMode.Login,
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nickname: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v.filter(Char::isDigit).take(11)) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v.take(64)) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v.take(64)) }
    fun onConfirmPasswordChange(v: String) = _state.update { it.copy(confirmPassword = v.take(64)) }
    fun onNicknameChange(v: String) = _state.update { it.copy(nickname = v.take(20)) }
    fun clearMessage() = _state.update { it.copy(message = null) }

    fun switchMode(mode: AuthMode) {
        _state.update {
            it.copy(
                mode = mode,
                password = "",
                confirmPassword = "",
                email = "",
                message = null,
                success = false,
            )
        }
    }

    fun submit() {
        val s = _state.value
        if (!s.phone.matches(Regex("^1\\d{10}$"))) {
            _state.update { it.copy(message = "请输入正确的11位手机号") }
            return
        }
        if (!s.password.matches(Regex("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,}\$"))) {
            _state.update { it.copy(message = "密码要求6位以上数字+英文字母") }
            return
        }
        if (s.mode == AuthMode.Register) {
            if (s.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
                _state.update { it.copy(message = "请输入有效的邮箱地址") }
                return
            }
            if (s.password != s.confirmPassword) {
                _state.update { it.copy(message = "两次输入的密码不一致") }
                return
            }
            viewModelScope.launch {
                _state.update { it.copy(loading = true) }
                val nick = s.nickname.trim().ifBlank { null }
                authRepository.register(s.phone, s.email, s.password, nick)
                    .onSuccess { _state.update { it.copy(loading = false, success = true) } }
                    .onFailure { e -> _state.update { it.copy(loading = false, message = e.message ?: "注册失败") } }
            }
        } else {
            viewModelScope.launch {
                _state.update { it.copy(loading = true) }
                authRepository.login(s.phone, s.password)
                    .onSuccess { _state.update { it.copy(loading = false, success = true) } }
                    .onFailure { e -> _state.update { it.copy(loading = false, message = e.message ?: "登录失败") } }
            }
        }
    }
}
