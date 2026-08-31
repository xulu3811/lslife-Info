package com.qingyuan.lslife.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qingyuan.lslife.ui.components.NetworkImage
import com.qingyuan.lslife.ui.SessionViewModel
import com.qingyuan.lslife.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    onOpenMembership: () -> Unit,
    onOpenMessage: () -> Unit,
    onOpenRealName: () -> Unit,
    onOpenMyPosts: (String) -> Unit,
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
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user = state.user
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }
    
    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) {
            onLoggedOut()
        }
    }

    if (state.loading) {
        LoadingBox(Modifier.fillMaxSize())
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F8))
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header (User Info)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenPersonalInfo() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NetworkImage(
                        url = user?.avatar,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, Brush.sweepGradient(listOf(Color(0xFF4285F4), Color(0xFFEA4335), Color(0xFFFBBC05), Color(0xFF34A853))), CircleShape)
                            .background(Color(0xFFF3F4F6))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.nickname ?: "未登录",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (user?.role == "ADMIN" || user?.role == "SUPERADMIN") {
                                M3Badge(text = "平台管理", bgColor = Color(0xFFE0E7FF), textColor = Color(0xFF4338CA))
                            }
                            if (user?.realNameStatus == "verified") {
                                M3Badge(text = "已实名", bgColor = Color(0xFFD1FAE5), textColor = Color(0xFF059669))
                            } else {
                                M3Badge(text = "未实名", bgColor = Color(0xFFF3F4F6), textColor = Color(0xFF6B7280))
                            }
                            if (user?.membershipTier == "premium") {
                                M3Badge(text = "至尊会员", bgColor = Color(0xFFFEF3C7), textColor = Color(0xFFB45309))
                            } else if (user?.membershipTier == "vip") {
                                M3Badge(text = "VIP会员", bgColor = Color(0xFFFEF3C7), textColor = Color(0xFFB45309))
                            }
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(16.dp), tint = Color(0xFF9CA3AF))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    M3StatItem(count = "${user?.favoritesCount ?: 0}", label = "收藏", onClick = onOpenFavorites)
                    M3StatItem(count = "${user?.footprintsCount ?: 0}", label = "足迹", onClick = onOpenFootprints)
                    M3StatItem(count = "${user?.followingCount ?: 0}", label = "关注", onClick = onOpenFollowList)
                    M3StatItem(count = "${user?.followersCount ?: 0}", label = "粉丝", onClick = onOpenFollowList)
                }
            }
        }

        // 2. Admin Workspace
        if (user?.role == "ADMIN" || user?.role == "SUPERADMIN") {
            M3GroupCard(title = "运营管理") {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    M3GridItem(icon = Icons.Outlined.Dashboard, label = "平台数据", onClick = onOpenGovernanceCenter, modifier = Modifier.weight(1f))
                    M3GridItem(icon = Icons.Outlined.PendingActions, label = "待办审批", badgeCount = state.pendingReviews, onClick = onOpenAdminApprovals, modifier = Modifier.weight(1f))
                    M3GridItem(icon = Icons.Outlined.HowToReg, label = "用户治理", onClick = onOpenAdminUserList, modifier = Modifier.weight(1f))
                    M3GridItem(icon = Icons.Outlined.Gavel, label = "内容风控", onClick = onOpenAdminReportList, modifier = Modifier.weight(1f))
                }
            }
        }

        // 3. My Posts
        M3GroupCard(title = "我的发布") {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                M3GridItem(icon = Icons.Outlined.List, label = "全部发布", onClick = { onOpenMyPosts("ALL") }, modifier = Modifier.weight(1f))
                M3GridItem(icon = Icons.Outlined.PendingActions, label = "审核中", onClick = { onOpenMyPosts("PENDING") }, modifier = Modifier.weight(1f))
                M3GridItem(icon = Icons.Outlined.Visibility, label = "展示中", onClick = { onOpenMyPosts("PUBLISHED") }, modifier = Modifier.weight(1f))
                M3GridItem(icon = Icons.Outlined.Archive, label = "已下架", onClick = { onOpenMyPosts("ARCHIVED") }, modifier = Modifier.weight(1f))
            }
        }

        // 4. Commercial Promotion
        M3GroupCard(title = "商业推广") {
            M3MenuRow(icon = Icons.Outlined.AccountBalanceWallet, title = "账户余额", rightText = "¥%.2f".format(user?.walletBalance ?: 0.0), onClick = onOpenWallet)
            M3MenuRow(icon = Icons.Outlined.WorkspacePremium, title = "超级会员", rightText = if (user?.membershipTier == "premium") "至尊" else if (user?.membershipTier == "vip") "VIP" else "普通", onClick = onOpenMembership)
            M3MenuRow(icon = Icons.Outlined.TrendingUp, title = "推广中心", onClick = onOpenPromotionCenter, showDivider = false)
        }

        // 5. Trust & Services
        M3GroupCard(title = "信任与服务") {
            M3MenuRow(icon = Icons.Outlined.VerifiedUser, title = "实名认证", rightText = if (user?.realNameStatus == "verified") "已实名" else "去认证", onClick = onOpenRealName)
            M3MenuRow(icon = Icons.Outlined.Storefront, title = "商家入驻/店铺认证", rightText = when (state.merchantCertStatus) { "PENDING" -> "审核中"; "APPROVED" -> "已认证"; "REJECTED" -> "被驳回"; "SUSPENDED" -> "异常(需打卡)"; else -> "去认证" }, onClick = { if (state.merchantCertStatus != "PENDING" && state.merchantCertStatus != "APPROVED" && state.merchantCertStatus != "SUSPENDED") onOpenMerchantCertify() }, showDivider = false)
        }

        // 6. Settings & System
        M3GroupCard(title = "更多服务") {
            M3MenuRow(icon = Icons.Outlined.Settings, title = "设置与隐私", onClick = onOpenSettings, showDivider = false)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (state.merchantCertStatus == "SUSPENDED") {
        AlertDialog(
            onDismissRequest = { /* Force confirmation, so dismiss not allowed */ },
            title = { Text("资质即将到期 / 长期未打卡") },
            text = { Text("您的商家服务长时间未维护或资质即将过期。请确认您的店铺仍在正常营业中，以恢复商家的正常展示和权益。") },
            confirmButton = {
                Button(onClick = { viewModel.confirmMerchantActive() }) {
                    Text("确认正常营业")
                }
            },
            properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }
}

@Composable
fun M3GroupCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
            Text(
                text = title,
                fontSize = 15.sp, // 标题升级
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937), // 更深的颜色
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun M3GridItem(icon: ImageVector, label: String, onClick: () -> Unit, badgeCount: Int = 0, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.then(Modifier.clickable { onClick() }.padding(vertical = 4.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 移除多余的灰色圆形底座，释放图标本体，增大图标尺寸
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (badgeCount > 0) {
                BadgedBox(badge = { Badge { Text(if (badgeCount > 99) "99+" else badgeCount.toString()) } }) {
                    Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color(0xFF1F2937))
                }
            } else {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color(0xFF1F2937))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B5563))
    }
}

@Composable
fun M3StatItem(count: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        // 使用 Display 级别字体，放大主次对比
        Text(
            text = count, 
            fontSize = 24.sp, 
            fontWeight = FontWeight.ExtraBold, 
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF9CA3AF)) // 更浅的次级灰色
    }
}

@Composable
fun M3MenuRow(icon: ImageVector, title: String, rightText: String? = null, showDivider: Boolean = true, titleColor: Color = Color(0xFF374151), onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp), // 强制高度至少 56dp，保障触控热区
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = titleColor.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 15.sp, color = titleColor, modifier = Modifier.weight(1f))
            if (rightText != null) {
                Text(text = rightText, fontSize = 13.sp, color = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(14.dp), tint = Color(0xFFD1D5DB))
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 46.dp), thickness = 0.5.dp, color = Color(0xFFF3F4F6))
        }
    }
}

@Composable
fun M3Badge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier.height(20.dp).clip(RoundedCornerShape(6.dp)).background(bgColor).padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}
