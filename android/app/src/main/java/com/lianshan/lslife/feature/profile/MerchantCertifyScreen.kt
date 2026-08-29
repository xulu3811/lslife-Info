package com.qingyuan.lslife.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantCertifyScreen(
    navController: NavController,
    viewModel: MerchantCertifyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep
    val context = LocalContext.current
    
    LaunchedEffect(uiState) {
        if (uiState is CertifyState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商家入驻") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) {
                            viewModel.prevStep()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Stepper indicator
            Stepper(currentStep)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                when (currentStep) {
                    1 -> Step1TypeSelection(viewModel)
                    2 -> Step2BasicInfo(viewModel)
                    3 -> Step3Qualifications(viewModel)
                }
            }
            
            // Bottom Actions
            BottomBarActions(
                currentStep = currentStep,
                uiState = uiState,
                onNext = { viewModel.nextStep() },
                onSubmit = { viewModel.submitCertification() },
                isValid = checkStepValid(currentStep, viewModel)
            )
        }
    }
}

@Composable
fun Stepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepDot(step = 1, current = currentStep, label = "主体类型")
        HorizontalDivider(Modifier.weight(1f).padding(horizontal = 8.dp), color = if (currentStep > 1) Color(0xFF4285F4) else Color(0xFFDADCE0))
        StepDot(step = 2, current = currentStep, label = "基础信息")
        HorizontalDivider(Modifier.weight(1f).padding(horizontal = 8.dp), color = if (currentStep > 2) Color(0xFF4285F4) else Color(0xFFDADCE0))
        StepDot(step = 3, current = currentStep, label = "资质上传")
    }
}

@Composable
fun StepDot(step: Int, current: Int, label: String) {
    val isCompletedOrCurrent = step <= current
    val color = if (isCompletedOrCurrent) Color(0xFF4285F4) else Color(0xFFDADCE0) // Google Blue & Grey
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(step.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp), color = color)
    }
}

@Composable
fun Step1TypeSelection(viewModel: MerchantCertifyViewModel) {
    Text("入驻类型", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    
    val certType by viewModel.certType
    
    TypeSelectionCard(
        title = "企业/个体户",
        description = "拥有合法营业执照的实体商户",
        selected = certType == "ENTERPRISE",
        onClick = { viewModel.certType.value = "ENTERPRISE" }
    )
    Spacer(Modifier.height(16.dp))
    TypeSelectionCard(
        title = "个人手艺人",
        description = "无营业执照，凭技能提供上门服务",
        selected = certType == "INDIVIDUAL",
        onClick = { viewModel.certType.value = "INDIVIDUAL" }
    )
}

@Composable
fun TypeSelectionCard(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFE8F0FE) else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4285F4)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = if (selected) Color(0xFF4285F4) else MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), color = if (selected) Color(0xFF4285F4).copy(alpha=0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun Step2BasicInfo(viewModel: MerchantCertifyViewModel) {
    Text("基础信息", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    
    OutlinedTextField(
        value = viewModel.storeName.value,
        onValueChange = { viewModel.storeName.value = it },
        label = { Text("店铺名称 (必填)") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = viewModel.contactName.value,
        onValueChange = { viewModel.contactName.value = it },
        label = { Text("联系人姓名 (必填)") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = viewModel.contactPhone.value,
        onValueChange = { viewModel.contactPhone.value = it },
        label = { Text("联系电话 (必填)") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    
    var showAddressPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    OutlinedTextField(
        value = viewModel.storeAddressRegion.value,
        onValueChange = {},
        label = { Text("所在地区 (必填)") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showAddressPicker = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Select Region")
            }
        },
        interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource().also { interactionSource ->
            androidx.compose.runtime.LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect {
                    if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                        showAddressPicker = true
                    }
                }
            }
        }
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = viewModel.storeAddressDetail.value,
        onValueChange = { viewModel.storeAddressDetail.value = it },
        label = { Text("详细地址 (门牌号等)") },
        modifier = Modifier.fillMaxWidth()
    )

    if (showAddressPicker) {
        com.qingyuan.lslife.ui.components.AddressPickerBottomSheet(
            addressNodes = viewModel.addressNodes.value,
            onDismissRequest = { showAddressPicker = false },
            onAddressSelected = {
                viewModel.storeAddressRegion.value = it
            }
        )
    }
}

@Composable
fun Step3Qualifications(viewModel: MerchantCertifyViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadLicenseAndPerformOcr(it, context) }
    }
    
    Text("上传资质证明", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    Text("请上传营业执照原件", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(16.dp))
    
    val licenseUrl = viewModel.businessLicenseUrl.value
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (licenseUrl.isNotBlank()) {
            AsyncImage(
                model = licenseUrl,
                contentDescription = "Business License",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(48.dp), tint = Color.Gray)
                Text("点击上传", color = Color.Gray)
            }
        }
    }
    
    if (viewModel.legalPerson.value.isNotBlank()) {
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("✅ OCR 智能识别成功", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("法人代表: ${viewModel.legalPerson.value}", style = MaterialTheme.typography.bodyMedium)
                Text("信用代码: ${viewModel.creditCode.value}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun BottomBarActions(
    currentStep: Int,
    uiState: CertifyState,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    isValid: Boolean
) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (uiState is CertifyState.Error) {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Button(
            onClick = { if (currentStep < 3) onNext() else onSubmit() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = isValid && uiState !is CertifyState.Loading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
        ) {
            if (uiState is CertifyState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (currentStep < 3) "下一步" else "提交审核", fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp))
            }
        }
    }
}

fun checkStepValid(currentStep: Int, viewModel: MerchantCertifyViewModel): Boolean {
    return when (currentStep) {
        1 -> true
        2 -> viewModel.storeName.value.isNotBlank() && viewModel.contactName.value.isNotBlank() && viewModel.contactPhone.value.isNotBlank()
        3 -> if (viewModel.certType.value == "ENTERPRISE") viewModel.businessLicenseUrl.value.isNotBlank() else true
        else -> false
    }
}
