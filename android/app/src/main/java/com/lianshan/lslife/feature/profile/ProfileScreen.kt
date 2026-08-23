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
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkspacePremium
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.lianshan.lslife.ui.components.LoadingBox
import com.lianshan.lslife.ui.components.NetworkImage

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    onOpenMembership: () -> Unit,
    onOpenMessage: () -> Unit,
    onOpenRealName: () -> Unit,
    onOpenMyPosts: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenFootprints: () -> Unit,
    onOpenFollowList: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenMerchantCertify: () -> Unit,
    onOpenAdminApprovals: () -> Unit = {},
    onOpenAdminUserList: () -> Unit = {},
    onOpenAdminReportList: () -> Unit = {},
    onOpenGovernanceCenter: () -> Unit = {},
    onOpenPromotionCenter: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme
    var showSignIn by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            viewModel.load(isSilent = true)
        }
    }
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
            // 一、 Joybuy 极简 Header (三层式排版与高级微胶囊美学)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenPersonalInfo)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 12.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. 头像区 (Joybuy 60dp 纯净圆环 + 细微边框与底色)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    NetworkImage(
                        url = user?.avatar, 
                        contentDescription = "头像", 
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                // 2. 信息区 (三层式结构：昵称主身份 -> 实名与会员属性流 -> 账号辅助信息)
                Column(modifier = Modifier.weight(1f)) {
                    // 第一层：昵称 + 主身份微渐变胶囊
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user?.nickname ?: "点击登录",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user != null) {
                            val isMerchant = user.role.contains("MERCHANT") || state.merchantCertStatus == "APPROVED"
                            if (user.role == "ADMIN" || user.role == "SUPERADMIN" || isMerchant) {
                                Spacer(modifier = Modifier.width(6.dp))
                                JoybuyPrimaryRoleBadge(role = user.role, isMerchant = isMerchant)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 第二层：实名状态胶囊 + 会员等级胶囊
                    if (user != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            JoybuyRealNameBadge(isVerified = user.realNameStatus == "verified")
                            Spacer(modifier = Modifier.width(6.dp))
                            JoybuyVipBadge(tier = user.membershipTier)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // 第三层：辅助账号信息
                    Text(
                        text = if (user != null) "账号: ${user.phone ?: "未绑定"}" else "登录查看更多同城服务",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Normal
                    )
                }
                
                // 3. 右侧“个人主页”胶囊引导按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "个人主页",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // 数据面板 (放大数字与文案、字体加粗、优化间距)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DataBoardItem(
                    label = "\u6536\u85CF", 
                    count = user?.favoritesCount?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenFavorites
                )
                DataBoardItem(
                    label = "\u8DB3\u8FF9", 
                    count = user?.footprintsCount?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenFootprints
                )
                DataBoardItem(
                    label = "\u5173\u6CE8/\u7C89\u4E1D", 
                    count = "${user?.followingCount ?: 0}/${user?.followersCount ?: 0}",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenFollowList
                )
            }

            // 二、 运营管理模块 (Joybuy 4栏对称工作台)
            if (user?.role == "ADMIN" || user?.role == "SUPERADMIN") {
                AdminWorkspaceCard(
                    pendingReviews = state.pendingReviews,
                    onOpenAdminApprovals = onOpenAdminApprovals,
                    onOpenUserManagement = onOpenAdminUserList,
                    onOpenReportHandling = onOpenAdminReportList,
                    onOpenGovernanceCenter = onOpenGovernanceCenter,
                )
            }

            // 三、 “我的发布” 模块
            SectionTitle(
                title = "我的发布", 
                showChevron = true, 
                onClick = onOpenMyPosts,
                topPadding = 12.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PostStatusGridItem(icon = Icons.Outlined.List, label = "全部发布", modifier = Modifier.weight(1f).clickable { onOpenMyPosts() })
                PostStatusGridItem(icon = Icons.Outlined.PendingActions, label = "审核中", modifier = Modifier.weight(1f).clickable { onOpenMyPosts() })
                PostStatusGridItem(icon = Icons.Outlined.Visibility, label = "展示中", modifier = Modifier.weight(1f).clickable { onOpenMyPosts() })
                PostStatusGridItem(icon = Icons.Outlined.Archive, label = "已下架", modifier = Modifier.weight(1f).clickable { onOpenMyPosts() })
            }

            // 三、 商业推广 模块
            SectionTitle(title = "商业推广", topPadding = 16.dp)
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
                onClick = onOpenPromotionCenter,
                showDivider = false
            )

            // 信任与服务 模块
            SectionTitle(title = "信任与服务", topPadding = 16.dp)
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
                onClick = { /* 信用评价 */ },
                showDivider = false
            )

            // 更多 模块
            SectionTitle(title = "更多", topPadding = 16.dp)
            ProfileMenuRow(
                icon = Icons.Outlined.SupportAgent,
                title = "客服中心",
                onClick = { /* 客服中心 */ },
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.Info,
                title = "关于我们",
                onClick = { /* 关于我们 */ },
                showDivider = true
            )
            ProfileMenuRow(
                icon = Icons.Outlined.Settings,
                title = "设置与隐私",
                onClick = onOpenSettings,
                showDivider = false
            )

            // 四、 底栏版权 (Joybuy 极简规范)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "© 2026 连山壮族瑶族自治县 · 智慧同城生活平台",
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(
    title: String, 
    showChevron: Boolean = false, 
    onClick: (() -> Unit)? = null,
    topPadding: androidx.compose.ui.unit.Dp = 20.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(
                Icons.Filled.ChevronRight, 
                contentDescription = null, 
                tint = Color(0xFF999999),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PostStatusGridItem(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, 
            contentDescription = label, 
            modifier = Modifier.size(24.dp), 
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon, 
                contentDescription = title, 
                tint = MaterialTheme.colorScheme.onBackground, 
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                title, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium, 
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            
            if (rightText != null) {
                Text(
                    rightText, 
                    fontSize = 13.sp, 
                    color = Color(0xFF999999)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                Icons.Filled.ChevronRight, 
                contentDescription = null, 
                tint = Color(0xFFCCCCCC), 
                modifier = Modifier.size(18.dp)
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 16.dp)
                    .height(0.5.dp)
                    .background(Color(0xFFF0F0F0)),
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
private fun DataBoardItem(
    label: String, 
    count: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 0.dp)
    ) {
        Text(
            text = count, 
            fontSize = 17.sp, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun AdminWorkspaceCard(
    pendingReviews: Int,
    onOpenAdminApprovals: () -> Unit,
    onOpenUserManagement: () -> Unit,
    onOpenReportHandling: () -> Unit,
    onOpenGovernanceCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(
            title = "运营管理",
            showChevron = true,
            onClick = onOpenAdminApprovals,
            topPadding = 14.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminStatusGridItem(
                icon = Icons.Outlined.PendingActions,
                label = "待办审批",
                badgeCount = pendingReviews,
                modifier = Modifier.weight(1f).clickable { onOpenAdminApprovals() }
            )
            AdminStatusGridItem(
                icon = Icons.Outlined.HowToReg,
                label = "用户管理",
                modifier = Modifier.weight(1f).clickable { onOpenUserManagement() }
            )
            AdminStatusGridItem(
                icon = Icons.Outlined.Info,
                label = "举报处理",
                modifier = Modifier.weight(1f).clickable { onOpenReportHandling() }
            )
            AdminStatusGridItem(
                icon = Icons.Outlined.Dashboard,
                label = "管理中枢",
                modifier = Modifier.weight(1f).clickable { onOpenGovernanceCenter() }
            )
        }
    }
}

@Composable
private fun AdminStatusGridItem(
    icon: ImageVector,
    label: String,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge { Text(if (badgeCount > 99) "99+" else badgeCount.toString()) }
                }
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun JoybuyPrimaryRoleBadge(role: String, isMerchant: Boolean) {
    if (role == "ADMIN" || role == "SUPERADMIN") {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                    )
                )
                .padding(horizontal = 7.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "平台管理员",
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    } else if (isMerchant) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFF5252), Color(0xFFE91E63))
                    )
                )
                .padding(horizontal = 7.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "商家用户",
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun JoybuyRealNameBadge(isVerified: Boolean) {
    if (isVerified) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFECFDF5))
                .padding(horizontal = 6.dp, vertical = 1.5.dp)
        ) {
            Text(
                text = "已实名 ✓",
                fontSize = 10.sp,
                color = Color(0xFF059669),
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 6.dp, vertical = 1.5.dp)
        ) {
            Text(
                text = "未实名",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun JoybuyVipBadge(tier: String?) {
    when (tier) {
        "premium" -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFDE68A), Color(0xFFF59E0B))
                        )
                    )
                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = "👑 超级会员",
                    fontSize = 10.sp,
                    color = Color(0xFF78350F),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        "vip" -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = "VIP 会员",
                    fontSize = 10.sp,
                    color = Color(0xFFB45309),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = "普通会员",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
