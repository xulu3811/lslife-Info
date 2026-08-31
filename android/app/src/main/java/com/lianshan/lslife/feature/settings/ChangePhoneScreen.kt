package com.qingyuan.lslife.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePhoneScreen(
    onBack: () -> Unit,
) {
    var newPhone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    
    var countdown by remember { mutableStateOf(0) }
    
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown -= 1
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F5F8),
        topBar = {
            TopAppBar(
                title = { Text("更换手机号", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFF374151))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF3F5F8)),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "请输入您要绑定的新手机号，并进行短信验证。",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = newPhone,
                onValueChange = { if (it.length <= 11) newPhone = it },
                label = { Text("新手机号", color = Color(0xFF9CA3AF)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Outlined.PhoneAndroid, contentDescription = null, tint = Color(0xFF9CA3AF)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4285F4),
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                label = { Text("验证码", color = Color(0xFF9CA3AF)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFF9CA3AF)) },
                trailingIcon = {
                    TextButton(
                        onClick = { countdown = 60 },
                        enabled = countdown == 0 && newPhone.length == 11,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (countdown > 0) "${countdown}s 后重试" else "获取验证码",
                            color = if (countdown > 0 || newPhone.length != 11) Color(0xFF9CA3AF) else Color(0xFF4285F4),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4285F4),
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            val isFormValid = newPhone.length == 11 && code.length >= 4
            
            Button(
                onClick = { /* TODO: Implement API */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4),
                    disabledContainerColor = Color(0xFF9CA3AF)
                )
            ) {
                Text("确认更换", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
