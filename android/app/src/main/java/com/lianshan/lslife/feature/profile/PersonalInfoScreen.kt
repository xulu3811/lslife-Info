package com.qingyuan.lslife.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.qingyuan.lslife.core.data.AuthRepository
import com.qingyuan.lslife.core.model.User
import com.qingyuan.lslife.ui.components.LoadingBox
import com.qingyuan.lslife.ui.components.SoftCard
import com.qingyuan.lslife.ui.theme.Dimens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonalInfoUiState(
    val loading: Boolean = true,
    val user: User? = null,
)

@HiltViewModel
class PersonalInfoViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(PersonalInfoUiState())
    val state: StateFlow<PersonalInfoUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            authRepository.me()
                .onSuccess { u -> _state.update { it.copy(loading = false, user = u) } }
                .onFailure { _state.update { it.copy(loading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenMembership: () -> Unit = {},
    viewModel: PersonalInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人信息") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.loading) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }
        val user = state.user
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            SoftCard {
                Column(Modifier.padding(horizontal = Dimens.md)) {
                    val isReviewing = user?.profileReviewStatus == "AI_REVIEWING" || user?.profileReviewStatus == "MANUAL_REVIEWING"
                    
                    InfoRow(
                        label = "头像" + if (isReviewing && user?.pendingAvatar != null) " (审核中)" else "",
                        isEditable = true,
                        onClick = onEditProfile,
                    ) {
                        AsyncImage(
                            model = user?.avatar,
                            contentDescription = "头像",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    HorizontalDivider()
                    InfoRow(
                        label = "昵称" + if (isReviewing && user?.pendingNickname != null) " (审核中)" else "",
                        value = user?.nickname ?: "-",
                        isEditable = true,
                        onClick = onEditProfile,
                    )
                    HorizontalDivider()
                    InfoRow("用户ID", user?.id?.takeLast(8)?.let { "LS-$it" } ?: "-")
                    HorizontalDivider()
                    InfoRow("手机号", maskPhone(user?.phone))
                    HorizontalDivider()
                    InfoRow(
                        label = "会员",
                        value = when (user?.membershipTier) {
                            "vip" -> "超级会员"
                            "premium" -> "至尊会员"
                            else -> "普通用户"
                        },
                        isEditable = true,
                        onClick = onOpenMembership,
                    )
                }
            }
            Text(
                "点击头像、昵称或会员进入完整修改页",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String? = null,
    isEditable: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isEditable && onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = Dimens.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            if (trailing != null) trailing()
            else if (value != null) {
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            if (isEditable) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun maskPhone(phone: String?): String {
    if (phone.isNullOrBlank() || phone.length < 7) return phone ?: "-"
    return phone.take(3) + "****" + phone.takeLast(4)
}
