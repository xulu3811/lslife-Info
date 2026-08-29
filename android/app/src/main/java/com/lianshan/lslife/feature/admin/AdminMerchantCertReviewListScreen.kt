package com.qingyuan.lslife.feature.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingyuan.lslife.core.network.ApiService
import com.qingyuan.lslife.core.network.MerchantCertification
import com.qingyuan.lslife.ui.components.EmptyState
import com.qingyuan.lslife.ui.components.LoadingBox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MerchantCertReviewListState(
    val loading: Boolean = true,
    val certs: List<MerchantCertification> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminMerchantCertReviewViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(MerchantCertReviewListState())
    val state: StateFlow<MerchantCertReviewListState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val res = api.getAdminMerchantCerts()
                if (res.code == 0) {
                    _state.update { it.copy(loading = false, certs = res.data ?: emptyList()) }
                } else {
                    _state.update { it.copy(loading = false, error = res.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun audit(id: String, action: String) {
        viewModelScope.launch {
            try {
                api.auditAdminMerchantCert(id, mapOf("action" to action))
                load()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMerchantCertReviewListScreen(
    onBack: () -> Unit,
    viewModel: AdminMerchantCertReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "商家入驻审核", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        if (state.loading) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
        } else if (state.certs.isEmpty()) {
            EmptyState(title = "暂无待审核的商家入驻申请", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.certs) { cert ->
                    MerchantCertReviewCard(
                        cert = cert,
                        onApprove = { viewModel.audit(cert.id, "approve") },
                        onReject = { viewModel.audit(cert.id, "reject") }
                    )
                }
            }
        }
    }
}

@Composable
private fun MerchantCertReviewCard(
    cert: MerchantCertification,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (cert.certType == "ENTERPRISE") "企业商家认证" else "个人商家认证", 
                    fontSize = 12.sp, 
                    color = Color(0xFFE64A19),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(6.dp))
            Text("店铺名称: ${cert.storeName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text("联系人: ${cert.contactName}", fontSize = 13.sp, color = Color(0xFF666666))
            Text("联系电话: ${cert.contactPhone}", fontSize = 13.sp, color = Color(0xFF666666), modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onReject,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("驳回", color = Color(0xFFD32F2F), fontSize = 12.5.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onApprove, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("通过 (建店)", color = Color.White, fontSize = 12.5.sp)
                }
            }
        }
    }
}
