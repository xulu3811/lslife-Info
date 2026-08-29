package com.qingyuan.lslife.feature.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qingyuan.lslife.core.model.AdminKycUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycAuditScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val kycUsers by viewModel.kycUsers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadKycUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("实名认证审核", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(kycUsers) { user ->
                KycUserCard(
                    user = user,
                    onApprove = { viewModel.auditKycUser(user.id, true) { _, _ -> } },
                    onReject = { viewModel.auditKycUser(user.id, false) { _, _ -> } }
                )
            }
        }
    }
}

@Composable
fun KycUserCard(
    user: AdminKycUser,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("真实姓名: ${user.realName ?: "未知"}", style = MaterialTheme.typography.titleMedium)
            Text("手机号: ${user.phone}", style = MaterialTheme.typography.bodyMedium)
            Text("身份证号: ${user.idCardHash ?: "未知"}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                user.idCardFrontImage?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Front",
                        modifier = Modifier.weight(1f).height(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                user.idCardBackImage?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Back",
                        modifier = Modifier.weight(1f).height(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                user.idCardHandheldImage?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Handheld",
                        modifier = Modifier.weight(1f).height(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onReject) {
                    Text("驳回")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onApprove) {
                    Text("通过")
                }
            }
        }
    }
}
