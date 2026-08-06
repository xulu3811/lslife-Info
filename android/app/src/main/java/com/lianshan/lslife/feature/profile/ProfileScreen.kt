package com.lianshan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage
import com.lianshan.lslife.ui.components.SoftCard
import com.lianshan.lslife.ui.theme.Dimens

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    onOpenMembership: () -> Unit,
    onOpenAddress: () -> Unit,
    onOpenMessage: () -> Unit,
    onOpenRealName: () -> Unit,
    onOpenMyPosts: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenMerchantCertify: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme
    var showSignIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.loggedOut) { if (state.loggedOut) onLoggedOut() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.loading) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }
        val user = state.user
        
        if (showSignIn && state.signInStatus != null) {
            SignInBottomSheet(
                status = state.signInStatus!!,
                isSigningIn = state.isSigningIn,
                onDismiss = { showSignIn = false },
                onExecuteSignIn = { viewModel.executeSignIn() }
            )
        }
        
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Joybuy Style Header (Refactored)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.lg)
                    .padding(top = Dimens.xl, bottom = Dimens.sm) // Reduced bottom padding
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NetworkImage(
                    user?.avatar, 
                    "头像", 
                    Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)) // Light gray background if no avatar
                        .clickable(onClick = onOpenPersonalInfo)
                )
                Spacer(modifier = Modifier.width(Dimens.md))
                Column(modifier = Modifier.weight(1f).clickable(onClick = onOpenPersonalInfo)) {
                    Text(
                        text = user?.nickname ?: "未登录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user?.phone ?: "点击登录/查看个人信息",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                
                // Top Right Action Area: SignIn + Notification
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sign In Icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showSignIn = true }
                            .padding(horizontal = Dimens.sm)
                    ) {
                        BadgedBox(
                            badge = { 
                                if (state.signInStatus?.isSignedToday == false) {
                                    Badge(modifier = Modifier.size(8.dp)) 
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircleOutline, // Representing SignIn
                                contentDescription = "签到",
                                tint = if (state.signInStatus?.isSignedToday == true) Color.Gray else scheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "签到", 
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (state.signInStatus?.isSignedToday == true) Color.Gray else scheme.onBackground
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(Dimens.sm))
                    
                    // Notification Icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(onClick = onOpenMessage)
                            .padding(horizontal = Dimens.sm)
                    ) {
                        if (state.unread > 0) {
                            BadgedBox(
                                badge = { Badge { Text(state.unread.toString()) } }
                            ) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = "消息",
                                    tint = scheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "消息",
                                tint = scheme.onBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "消息", 
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = scheme.onBackground
                        )
                    }
                }
            }

            // --- Data Board ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.lg)
                    .padding(bottom = Dimens.md), // Reduced padding
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DataBoardItem(label = "收藏", count = user?.favoritesCount?.toString() ?: "-")
                DataBoardItem(label = "足迹", count = user?.footprintsCount?.toString() ?: "-")
                DataBoardItem(label = "关注/粉丝", count = user?.followersCount?.toString() ?: "-")
            }
            Spacer(modifier = Modifier.height(Dimens.md))

            // --- Section 1: 我的发布 (My Posts) ---
            SectionTitle(title = "我的发布", showChevron = true, onClick = onOpenMyPosts)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.sm, vertical = Dimens.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrderGridItem(icon = Icons.Outlined.List, label = "全部发布", modifier = Modifier.weight(1f).clickable { onOpenMyPosts() })
                OrderGridItem(icon = Icons.Outlined.PendingActions, label = "审核中", modifier = Modifier.weight(1f).clickable { onOpenMyPosts() })
                OrderGridItem(icon = Icons.Outlined.Visibility, label = "展示中", modifier = Modifier.weight(1f).clickable { onOpenMyPosts() })
                OrderGridItem(icon = Icons.Outlined.Archive, label = "已下架", modifier = Modifier.weight(1f).clickable { onOpenMyPosts() })
            }

            Spacer(modifier = Modifier.height(Dimens.md))

            // --- Section 2: 商业推广 (Commercial Promotion) ---
            SectionTitle(title = "商业推广")
            ProfileMenuRow(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = "账户余额",
                rightText = "¥%.2f".format(user?.walletBalance ?: 0.0),
                onClick = onOpenWallet,
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.WorkspacePremium,
                title = "超级会员",
                rightText = tierLabel(user?.membershipTier),
                onClick = onOpenMembership,
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.TrendingUp,
                title = "推广中心",
                onClick = { /* Placeholder for promotion center */ },
                showDivider = true
            )

            // --- Section 3: 信任与服务 (Trust & Services) ---
            SectionTitle(title = "信任与服务")
            ProfileMenuRow(
                icon = Icons.Outlined.VerifiedUser,
                title = "实名认证",
                rightText = if (user?.realNameStatus == "verified") "已完成" else "去认证",
                onClick = onOpenRealName,
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.HowToReg,
                title = "商家入驻/店铺认证",
                rightText = when (state.merchantCertStatus) {
                    "PENDING" -> "审核中"
                    "APPROVED" -> "已认证"
                    "REJECTED" -> "被驳回"
                    else -> "去认证"
                },
                onClick = {
                    if (state.merchantCertStatus != "PENDING" && state.merchantCertStatus != "APPROVED") {
                        onOpenMerchantCertify()
                    }
                },
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.StarRate,
                title = "信用评价",
                rightText = "${user?.creditScore ?: 100}分",
                onClick = { /* Credit score */ },
                showDivider = false
            )

            Spacer(modifier = Modifier.height(Dimens.md))

            // --- Section 4: 更多 (More) ---
            SectionTitle(title = "更多")
            ProfileMenuRow(
                icon = Icons.Outlined.SupportAgent,
                title = "客服中心",
                onClick = { /* Customer service */ },
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.Info,
                title = "关于我们",
                onClick = { /* About us */ },
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.Settings,
                title = "设置&隐私",
                onClick = onOpenSettings,
                showDivider = true
            )
            
            // Logout Row (Styled as a menu row to fit minimalist design, or keep as a row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = viewModel::logout)
                    .padding(horizontal = Dimens.lg, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("退出登录", style = MaterialTheme.typography.titleSmall, color = Color(0xFFE52F2F))
            }

            Spacer(modifier = Modifier.height(Dimens.xl))
            Text(
                "© 2026 连山壮族瑶族自治县 · 智慧同城生活平台",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(Dimens.xxl))
        }
    }
}

@Composable
private fun SectionTitle(title: String, showChevron: Boolean = false, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = Dimens.lg, vertical = Dimens.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OrderGridItem(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onBackground)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}



@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    rightText: String? = null,
    onClick: () -> Unit = {},
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Dimens.lg, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            
            if (rightText != null) {
                Text(rightText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.lg) // Joybuy style thin full width divider
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
            )
        }
    }
}

private fun tierLabel(tier: String?) = when (tier) {
    "premium" -> "超级会员"
    "vip" -> "普通会员"
    else -> "免费用户"
}

@Composable
private fun DataBoardItem(label: String, count: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = Dimens.sm)
    ) {
        Text(
            text = count, 
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), 
            color = Color(0xFF888888)
        )
    }
}
