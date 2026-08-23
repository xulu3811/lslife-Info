package com.lianshan.lslife.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.data.AuthRepository
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.theme.Dimens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val loading: Boolean = false,
    val countdown: Int = 0,
    val message: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state

    fun onEmailChange(v: String) = _state.update { it.copy(email = v.take(64)) }
    fun onCodeChange(v: String) = _state.update { it.copy(code = v.take(6)) }
    fun onPasswordChange(v: String) = _state.update { it.copy(newPassword = v.take(64)) }
    fun clearMessage() = _state.update { it.copy(message = null) }

    fun sendCode() {
        val email = _state.value.email
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(message = "请输入有效的邮箱地址") }
            return
        }
        if (_state.value.countdown > 0) return

        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            authRepository.sendEmailCode(email)
                .onSuccess {
                    _state.update { it.copy(loading = false, message = "验证码已发送", countdown = 60) }
                    startCountdown()
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, message = e.message ?: "发送失败") }
                }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (_state.value.countdown > 0) {
                delay(1000)
                _state.update { it.copy(countdown = it.countdown - 1) }
            }
        }
    }

    fun submit() {
        val s = _state.value
        if (s.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            _state.update { it.copy(message = "请输入有效的邮箱地址") }
            return
        }
        if (s.code.length != 6) {
            _state.update { it.copy(message = "请输入6位验证码") }
            return
        }
        if (!s.newPassword.matches(Regex("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,}\$"))) {
            _state.update { it.copy(message = "密码要求6位以上数字+英文字母") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            authRepository.resetPassword(s.email, s.code, s.newPassword)
                .onSuccess {
                    _state.update { it.copy(loading = false, success = true, message = "密码重置成功") }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, message = e.message ?: "重置失败") }
                }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(state.success) {
        if (state.success) {
            delay(1500)
            onBack()
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = scheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = Dimens.xl, vertical = Dimens.xxl),
            ) {
                Column {
                    Text(
                        "找回密码",
                        style = MaterialTheme.typography.displaySmall,
                        color = scheme.onBackground,
                    )
                    Spacer(Modifier.height(Dimens.sm))
                    Text(
                        "通过绑定的邮箱重置您的密码",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.lg)
                    .padding(top = Dimens.xl),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.xl),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md),
                ) {
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("注册邮箱") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors(scheme),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.code,
                            onValueChange = viewModel::onCodeChange,
                            label = { Text("验证码") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            colors = fieldColors(scheme),
                        )
                        Button(
                            onClick = viewModel::sendCode,
                            enabled = state.countdown == 0 && !state.loading,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text(if (state.countdown > 0) "${state.countdown}s" else "获取验证码")
                        }
                    }

                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("新密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors(scheme),
                    )

                    Spacer(Modifier.height(8.dp))

                    PrimaryButton(
                        text = "重置密码",
                        onClick = viewModel::submit,
                        enabled = !state.loading && !state.success,
                        loading = state.loading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text("返回登录", color = scheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors(scheme: androidx.compose.material3.ColorScheme) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = scheme.primary,
        unfocusedContainerColor = scheme.surfaceVariant.copy(alpha = 0.35f),
        focusedContainerColor = scheme.surfaceVariant.copy(alpha = 0.25f),
    )
