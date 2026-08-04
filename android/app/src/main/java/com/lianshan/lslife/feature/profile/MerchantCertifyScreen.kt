package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantCertifyScreen(
    navController: NavController,
    viewModel: MerchantCertifyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商家入驻") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 类型选择
            Text("入驻类型", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = viewModel.certType.value == "ENTERPRISE",
                    onClick = { viewModel.certType.value = "ENTERPRISE" }
                )
                Text("企业/个体户")
                Spacer(Modifier.width(16.dp))
                RadioButton(
                    selected = viewModel.certType.value == "INDIVIDUAL",
                    onClick = { viewModel.certType.value = "INDIVIDUAL" }
                )
                Text("个人手艺人")
            }
            
            Spacer(Modifier.height(8.dp))
            
            // 表单字段
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
            
            Spacer(Modifier.height(16.dp))
            
            // 资质上传区 (营业执照 & 门店照片)
            Text("上传资质证明", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            Text("请上传营业执照或相关门店照片", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // TODO: Picture selection UI integration
            
            if (uiState is CertifyState.Error) {
                Text(
                    text = (uiState as CertifyState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (uiState is CertifyState.Success) {
                Text(
                    text = "提交成功！正在审核中...",
                    color = Color(0xFF07C160),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            // 提交防抖按钮
            Button(
                onClick = { viewModel.submitCertification() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = uiState !is CertifyState.Loading && uiState !is CertifyState.Success
            ) {
                if (uiState is CertifyState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("提交审核", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
