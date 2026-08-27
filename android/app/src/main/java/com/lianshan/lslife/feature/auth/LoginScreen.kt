package com.lianshan.lslife.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.theme.PrimaryRed

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme
    val isRegister = state.mode == AuthMode.Register
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) { if (state.success) onLoggedIn() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = scheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 黄金纵向留白：顶部与状态栏舒适间距
            Spacer(Modifier.height(32.dp))

            // 1. 居中品牌形象展示区 (Google Style)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                // 新的 Google 风格图标
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.lianshan.lslife.R.drawable.img_google_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(72.dp)
                )

                Spacer(Modifier.height(16.dp))

                // 品牌主标题
                Text(
                    text = "同城•连山",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = scheme.onBackground
                )

                Spacer(Modifier.height(8.dp))

                // 品牌副标题
                Text(
                    text = "智慧分类信息 · 贴心同城生活服务",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF5F6368) // Google Gray
                )
            }

            Spacer(Modifier.height(32.dp))

            // 2. Google Style Login Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(8.dp),
                color = scheme.surface,
                border = BorderStroke(1.dp, Color(0xFFDADCE0))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    // 登录 / 注册 分段 Tab 切换栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 登录 Tab
                        Column(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { viewModel.switchMode(AuthMode.Login) }
                                .padding(end = 24.dp)
                        ) {
                            Text(
                                text = "账号登录",
                                fontSize = if (!isRegister) 16.5.sp else 14.5.sp,
                                fontWeight = if (!isRegister) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isRegister) scheme.onBackground else Color(0xFF9E9E9E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(2.5.dp)
                                    .clip(CircleShape)
                                    .background(if (!isRegister) Color(0xFF1A73E8) else Color.Transparent)
                            )
                        }

                        // 注册 Tab
                        Column(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { viewModel.switchMode(AuthMode.Register) }
                        ) {
                            Text(
                                text = "快速注册",
                                fontSize = if (isRegister) 16.5.sp else 14.5.sp,
                                fontWeight = if (isRegister) FontWeight.Bold else FontWeight.Normal,
                                color = if (isRegister) scheme.onBackground else Color(0xFF9E9E9E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(2.5.dp)
                                    .clip(CircleShape)
                                    .background(if (isRegister) Color(0xFF1A73E8) else Color.Transparent)
                            )
                        }
                    }

                    // 手机号输入框
                    JoybuyLuxuryInputField(
                        value = state.phone,
                        onValueChange = viewModel::onPhoneChange,
                        placeholder = "请输入11位手机号",
                        leadingIcon = Icons.Outlined.Phone,
                        keyboardType = KeyboardType.Phone
                    )

                    // 注册专属字段 (邮箱、昵称)
                    if (isRegister) {
                        JoybuyLuxuryInputField(
                            value = state.email,
                            onValueChange = viewModel::onEmailChange,
                            placeholder = "请输入有效邮箱 (用于找回密码)",
                            leadingIcon = Icons.Outlined.Email,
                            keyboardType = KeyboardType.Email
                        )

                        JoybuyLuxuryInputField(
                            value = state.nickname,
                            onValueChange = viewModel::onNicknameChange,
                            placeholder = "昵称 (可选)",
                            leadingIcon = Icons.Outlined.Person
                        )
                    }

                    // 密码输入框
                    JoybuyLuxuryInputField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = if (isRegister) "设置密码 (6位以上字母+数字)" else "请输入登录密码",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        keyboardType = KeyboardType.Password
                    )

                    // 确认密码输入框 (仅注册模式)
                    if (isRegister) {
                        JoybuyLuxuryInputField(
                            value = state.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChange,
                            placeholder = "请再次输入登录密码",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            passwordVisible = confirmPasswordVisible,
                            onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                            keyboardType = KeyboardType.Password
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 主提交行动按钮 (Google Blue Pill Button)
                    Button(
                        onClick = viewModel::submit,
                        enabled = !state.loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A73E8),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (isRegister) "注册并登录" else "登录",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // 辅助跳转与提示操作
                    if (!isRegister) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "没有账号？",
                                    fontSize = 13.sp,
                                    color = Color(0xFF5F6368)
                                )
                                Text(
                                    text = "立即注册",
                                    fontSize = 13.sp,
                                    color = Color(0xFF1A73E8),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { viewModel.switchMode(AuthMode.Register) }
                                )
                            }

                            Text(
                                text = "忘记密码？",
                                fontSize = 13.sp,
                                color = Color(0xFF1A73E8),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { onForgotPasswordClick() }
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "已有账号？",
                                fontSize = 13.sp,
                                color = Color(0xFF5F6368)
                            )
                            Text(
                                text = "直接登录",
                                fontSize = 13.sp,
                                color = Color(0xFF1A73E8),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { viewModel.switchMode(AuthMode.Login) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // 3. 底部协议 (精巧低噪、平衡视觉落点)
            Text(
                text = "登录或注册即表示同意《用户协议》与《隐私政策》",
                fontSize = 11.5.sp,
                color = Color(0xFFA0A0A5),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(36.dp))
        }
    }
}

/**
 * Joybuy 风格高质感线性微图标输入框
 */
@Composable
private fun JoybuyLuxuryInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.hashCode() != Color.White.hashCode()

    val bgColor = Color.Transparent
    val borderColor = if (isDark) Color(0xFF5F6368) else Color(0xFFDADCE0)
    val iconColor = if (isDark) Color(0xFF9AA0A6) else Color(0xFF5F6368)
    val textColor = scheme.onBackground
    val hintColor = if (isDark) Color(0xFF9AA0A6) else Color(0xFF80868B)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = hintColor,
                    fontSize = 15.sp,
                    maxLines = 1
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color(0xFF1A73E8)),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (isPassword && onPasswordToggle != null) {
            IconButton(
                onClick = onPasswordToggle,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
