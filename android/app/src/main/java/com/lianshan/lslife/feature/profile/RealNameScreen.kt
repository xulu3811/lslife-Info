package com.lianshan.lslife.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lianshan.lslife.ui.components.PrimaryButton
import com.lianshan.lslife.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealNameScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val user = state.user
    val isPending = user?.realNameStatus == "pending"
    val isVerified = user?.realNameStatus == "verified"

    var realName by remember { mutableStateOf(if (isVerified || isPending) user?.realName ?: "" else "") }
    var idNumber by remember { mutableStateOf(if (isVerified || isPending) "*****************" else "") }
    
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var backUri by remember { mutableStateOf<Uri?>(null) }
    var agreed by remember { mutableStateOf(false) }
    var showAgreement by remember { mutableStateOf(false) }

    val pickFrontMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> 
        if (uri != null) {
            frontUri = uri
        }
    }
    val pickBackMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> 
        if (uri != null) {
            backUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("实名认证") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Dimens.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            if (isVerified) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)), // Google Green Light
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(Dimens.lg)) {
                        Text("认证状态：已实名", style = MaterialTheme.typography.titleMedium, color = Color(0xFF34A853), fontWeight = FontWeight.Bold) // Google Green
                        Text("您已通过实名认证，可享受平台的完整功能。", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF34A853).copy(alpha = 0.9f))
                    }
                }
            } else if (isPending) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7E0)), // Google Yellow Light
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(Dimens.lg)) {
                        Text("认证状态：人工审核中", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE37400), fontWeight = FontWeight.Bold) // Google Dark Yellow/Orange
                        Text("您提交的身份资料正在人工审核中，请耐心等待1-3个工作日。", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE37400).copy(alpha = 0.9f))
                    }
                }
            } else {
                Text(
                    "请拍摄并上传身份证的正反面照片，并手动填写您的真实姓名与身份证号码。我们郑重承诺您的信息安全，所有信息将经由专员人工审核。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isVerified && !isPending) {
                // 1. 正面 (带人像线框提示)
                IdCardPickerBox(
                    label = "点击拍摄/上传身份证人像面",
                    subLabel = "确保边框完整，字体清晰，亮度均匀",
                    uri = frontUri,
                    onClick = { pickFrontMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
                
                // 2. 反面 (带国徽线框提示)
                IdCardPickerBox(
                    label = "点击拍摄/上传身份证国徽面",
                    subLabel = "确保边框完整，字体清晰，无反光",
                    uri = backUri,
                    onClick = { pickBackMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
            }

            // 智能回填区 (OCR 后自动填充)
            OutlinedTextField(
                value = realName,
                onValueChange = { realName = it },
                label = { Text("真实姓名") },
                placeholder = { Text("请输入真实姓名") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = true
            )

            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = { Text("身份证号码") },
                placeholder = { Text("支持手动输入或上传人像面自动回填") },
                leadingIcon = { Icon(Icons.Filled.Badge, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = true
            )

            if (!isVerified && !isPending) {
                Spacer(modifier = Modifier.height(Dimens.sm))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = agreed,
                        onCheckedChange = { agreed = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Row(modifier = Modifier.clickable { showAgreement = true }) {
                        Text("我已阅读并完全同意", fontSize = 14.sp)
                        Text(
                            "《实名认证免责协议》", 
                            fontSize = 14.sp, 
                            color = Color(0xFF4285F4), // Google Blue
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
                
                val canSubmit = realName.isNotBlank() && idNumber.length == 18 && frontUri != null && backUri != null && agreed && !state.realNameSubmitting
                
                Button(
                    onClick = { 
                        if (canSubmit) {
                            viewModel.submitRealName(realName, idNumber, frontUri!!, backUri!!, context)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = canSubmit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4), // Google Blue
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        if (state.realNameSubmitting) "提交中..." else "确认无误，提交认证",
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showAgreement) {
        AlertDialog(
            onDismissRequest = { showAgreement = false },
            title = { Text("实名认证免责协议", fontWeight = FontWeight.Bold) },
            text = {
                val scrollState = rememberScrollState()
                Text(
                    text = "甲方（本平台）与乙方（用户：包含商家和消费者）就实名认证达成协议：\n\n" +
                            "1. 真实性承诺：乙方承诺所提供信息真实、合法、有效。\n" +
                            "2. 隐私保护：甲方承诺对实名信息严格保密，绝不泄露给任何无权第三方。\n" +
                            "3. 交易免责：本平台作为信息发布平台，不对私下交易担保。发生纠纷时提供电子存证协查。\n" +
                            "4. 违规处置：如发现非法活动，甲方有权随时封停账号。",
                    modifier = Modifier.verticalScroll(scrollState),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    agreed = true
                    showAgreement = false 
                }) {
                    Text("同意并继续")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAgreement = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun IdCardPickerBox(label: String, subLabel: String, uri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto, 
                    contentDescription = null, 
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    label, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subLabel, 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
