package com.qingyuan.lslife.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.Email
import com.qingyuan.lslife.feature.chat.ChatSessionListScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qingyuan.lslife.R
import com.qingyuan.lslife.feature.auth.ForgotPasswordScreen
import com.qingyuan.lslife.feature.auth.LoginScreen
import com.qingyuan.lslife.feature.home.HomeScreen
import com.qingyuan.lslife.feature.category.CategoryScreen
import com.qingyuan.lslife.feature.search.SearchScreen
import com.qingyuan.lslife.feature.merchant.MerchantDetailScreen

import com.qingyuan.lslife.feature.profile.CropScreen
import com.qingyuan.lslife.feature.profile.EditProfileScreen
import com.qingyuan.lslife.feature.profile.MembershipScreen

import com.qingyuan.lslife.feature.chat.ChatSessionListScreen
import com.qingyuan.lslife.feature.chat.ChatScreen
import com.qingyuan.lslife.feature.profile.FavoritesScreen
import com.qingyuan.lslife.feature.profile.FootprintsScreen
import com.qingyuan.lslife.feature.profile.MyPostsScreen
import com.qingyuan.lslife.feature.profile.PersonalInfoScreen
import com.qingyuan.lslife.feature.profile.ProfileScreen
import com.qingyuan.lslife.feature.profile.RealNameScreen
import com.qingyuan.lslife.feature.publish.PublishScreen
import com.qingyuan.lslife.feature.wallet.WalletScreen
import com.qingyuan.lslife.feature.settings.AboutScreen
import com.qingyuan.lslife.feature.settings.PrivacyScreen
import com.qingyuan.lslife.feature.settings.SettingsScreen
import com.qingyuan.lslife.ui.navigation.Routes
import com.qingyuan.lslife.ui.components.PublishMenuBottomSheet

private data class Tab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val allTabs = listOf(
    Tab(Routes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    Tab(Routes.CATEGORY, R.string.nav_category, Icons.Filled.ManageSearch, Icons.Outlined.ManageSearch),
    Tab(Routes.PUBLISH, R.string.nav_publish, Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
    Tab(Routes.MESSAGE_LIST, R.string.nav_messages, Icons.Filled.Email, Icons.Outlined.Email),
    Tab(Routes.PROFILE, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person),
)

private val tabs: List<Tab>
    get() = allTabs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LsLifeApp(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val unreadCount by sessionViewModel.unreadCount.collectAsStateWithLifecycle()
    val inAppBanner by sessionViewModel.inAppBanner.collectAsStateWithLifecycle()
    var showPublishMenu by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn != null) {
            // Guarantee a minimum brief display time for the splash logo
            kotlinx.coroutines.delay(800)
            showSplash = false
        }
    }

    if (showSplash || isLoggedIn == null) {
        // Brief Splash Screen with app icon
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "Splash Logo",
                modifier = Modifier.size(100.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
            )
        }
        return
    }

    LaunchedEffect(Unit) {
        sessionViewModel.navigateToChatFlow.collect { (sessionId, targetUserId, targetName) ->
            navController.navigate(Routes.chat(sessionId, targetUserId, targetName))
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val mainRoutes = tabs.map { it.route } + Routes.PUBLISH
    val showBottomBar = currentRoute in mainRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
            if (showBottomBar) {
                Box {
                    NavigationBar(
                        // modifier = Modifier.height(60.dp), removed to let M3 pill show properly
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        tabs.forEach { tab ->
                            val selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                            val isPublish = tab.route == Routes.PUBLISH
                            val isMessages = tab.route == Routes.MESSAGE_LIST
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (isPublish) {
                                        showPublishMenu = true
                                    } else {
                                        val navigateRoute = tab.route.substringBefore("?")
                                        navController.navigate(navigateRoute) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    if (isPublish) {
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            shadowElevation = 0.dp
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Icon(
                                                    Icons.Filled.Add,
                                                    contentDescription = stringResource(tab.labelRes),
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                        }
                                    } else {
                                        if (isMessages && unreadCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(
                                                        containerColor = androidx.compose.ui.graphics.Color(0xFFE53935),
                                                        contentColor = androidx.compose.ui.graphics.Color.White
                                                    ) {
                                                        val text = if (unreadCount > 99) "99+" else unreadCount.toString()
                                                        Text(text)
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = stringResource(tab.labelRes),
                                                    modifier = Modifier.size(19.dp),
                                                )
                                            }
                                        } else {
                                            Icon(
                                                if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = stringResource(tab.labelRes),
                                                modifier = Modifier.size(19.dp),
                                            )
                                        }
                                    }
                                },
                                label = if (isPublish) null else {
                                    {
                                        Text(
                                            text = stringResource(tab.labelRes),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                        )
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.ui.graphics.Color(0xFF041E49),
                                    selectedTextColor = androidx.compose.ui.graphics.Color(0xFF1A73E8),
                                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFD3E3FD),
                                    unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF5F6368),
                                    unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF5F6368),
                                )
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn == true) Routes.HOME else Routes.LOGIN,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onForgotPasswordClick = {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    }
                )
            }
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenMerchant = { navController.navigate(Routes.merchant(it)) },
                    onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                    onMessageClick = { navController.navigate(Routes.MESSAGE_LIST) },
                    onNavigateToCategory = { id -> 
                        navController.navigate(Routes.categoryDetail(id))
                    },
                    onNavigateToCategoryTab = {
                        navController.navigate("category") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(
                route = Routes.CATEGORY_DETAIL,
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                com.qingyuan.lslife.feature.category.CategoryDetailScreen(
                    categoryId = categoryId,
                    onBack = { navController.popBackStack() },
                    onPostClick = { postId -> navController.navigate(Routes.postDetail(postId)) },
                    onChatClick = { targetId, targetName, postId ->
                        navController.navigate(Routes.chat("new", targetId, targetName, initPostId = postId))
                    }
                )
            }
            composable(
                route = Routes.CATEGORY,
                arguments = listOf(navArgument("primaryId") { nullable = true })
            ) {
                CategoryScreen(
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                    onCategoryClick = { categoryId -> navController.navigate(Routes.categoryDetail(categoryId)) },
                    onOpenPost = { postId -> navController.navigate(Routes.postDetail(postId)) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onPostClick = { navController.navigate(Routes.postDetail(it)) }
                )
            }
            composable(Routes.MESSAGE_LIST) {
                ChatSessionListScreen(
                    onNavigateToChat = { sessionId, targetUserId, targetName ->
                        navController.navigate(Routes.chat(sessionId, targetUserId, targetName))
                    }
                )
            }
            composable(
                route = Routes.PUBLISH,
                arguments = listOf(
                    navArgument("postId") { nullable = true },
                    navArgument("categoryId") { nullable = true }
                )
            ) { entry -> 
                val rawPostId = entry.arguments?.getString("postId")
                val validPostId = if (rawPostId == "{postId}" || rawPostId.isNullOrBlank()) null else rawPostId
                PublishScreen(
                    postId = validPostId,
                    onClose = { navController.popBackStack() },
                    onOpenPost = { id -> navController.navigate(Routes.postDetail(id)) },
                    onBackHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                ) 
            }

            composable(
                Routes.MY_POSTS,
                arguments = listOf(navArgument("status") { defaultValue = "ALL" })
            ) {
                MyPostsScreen(
                    onBack = { navController.popBackStack() },
                    onEditPost = { postId -> navController.navigate(Routes.publish(postId)) }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPostDetail = { postId -> navController.navigate(Routes.postDetail(postId)) },
                    onOpenHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.FOOTPRINTS) {
                FootprintsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPostDetail = { postId -> navController.navigate(Routes.postDetail(postId)) },
                    onOpenHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.FOLLOW_LIST) {
                com.qingyuan.lslife.feature.profile.FollowListScreen(
                    userId = "",
                    onBack = { navController.popBackStack() },
                    onOpenProfile = { uid -> navController.navigate(Routes.publicProfile(uid)) }
                )
            }
            composable(
                route = Routes.POST_DETAIL,
                arguments = listOf(
                    navArgument("postId") { type = NavType.StringType },
                    navArgument("mode") { nullable = true },
                    navArgument("reportId") { nullable = true }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                val mode = backStackEntry.arguments?.getString("mode")
                val reportId = backStackEntry.arguments?.getString("reportId")
                val context = androidx.compose.ui.platform.LocalContext.current
                com.qingyuan.lslife.feature.home.PostDetailScreen(
                    postId = postId,
                    isAdminMode = mode == "admin",
                    reportId = reportId,
                    onBack = { navController.popBackStack() },
                    onChatClick = { targetId, targetName ->
                        navController.navigate(Routes.chat("new", targetId, targetName, initPostId = postId))
                    },
                    onPhoneClick = { phone ->
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:$phone")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },

                    onPublisherClick = { publisherId, isMerchant ->
                        if (isMerchant) {
                            navController.navigate(Routes.merchant(publisherId))
                        } else {
                            navController.navigate(Routes.publicProfile(publisherId))
                        }
                    }
                )
            }
            
            composable(Routes.ADMIN_REVIEW_LIST) {
                com.qingyuan.lslife.feature.admin.ContentAuditScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenPostDetail = { postId -> navController.navigate(Routes.postDetail(postId, mode = "admin")) }
                )
            }
            
            composable(Routes.ADMIN_USER_LIST) {
                com.qingyuan.lslife.feature.admin.UserGovernanceScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Routes.ADMIN_REPORT_LIST) {
                com.qingyuan.lslife.feature.admin.AdminReportListScreen(
                    onBack = { navController.popBackStack() },
                    onReportClick = { reportId, targetId, targetType ->
                        if (targetType == "POST") {
                            navController.navigate(Routes.postDetail(targetId, mode = "admin", reportId = reportId))
                        }
                    }
                )
            }
            composable(Routes.ADMIN_APPROVAL_DASHBOARD) {
                com.qingyuan.lslife.feature.admin.AdminApprovalDashboardScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPostReview = { navController.navigate(Routes.ADMIN_REVIEW_LIST) },
                    onOpenProfileReview = { navController.navigate(Routes.ADMIN_PROFILE_REVIEW_LIST) },
                    onOpenKycReview = { navController.navigate(Routes.ADMIN_KYC_REVIEW_LIST) },
                    onOpenMerchantCertReview = { navController.navigate(Routes.ADMIN_MERCHANT_CERT_REVIEW_LIST) }
                )
            }
            composable(Routes.ADMIN_PROFILE_REVIEW_LIST) {
                com.qingyuan.lslife.feature.admin.AdminProfileReviewListScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ADMIN_KYC_REVIEW_LIST) {
                com.qingyuan.lslife.feature.admin.KycAuditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ADMIN_MERCHANT_CERT_REVIEW_LIST) {
                com.qingyuan.lslife.feature.admin.AdminMerchantCertReviewListScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ADMIN_DASHBOARD) {
                com.qingyuan.lslife.feature.admin.AdminDashboardScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ADMIN_GOVERNANCE_CENTER) {
                com.qingyuan.lslife.feature.admin.GovernanceCenterScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenPersonalInfo = { navController.navigate(Routes.PERSONAL_INFO) },
                    onOpenMembership = { navController.navigate(Routes.MEMBERSHIP) },
                    onOpenMessage = { navController.navigate(Routes.MESSAGE_LIST) },
                    onOpenRealName = { navController.navigate(Routes.REAL_NAME_AUTH) },
                    onOpenMyPosts = { status -> navController.navigate(Routes.myPosts(status)) },
                    onOpenFavorites = { navController.navigate(Routes.FAVORITES) },
                    onOpenFootprints = { navController.navigate(Routes.FOOTPRINTS) },
                    onOpenWallet = { navController.navigate(Routes.WALLET) },
                    onOpenFollowList = { navController.navigate(Routes.FOLLOW_LIST) },
                    onOpenMerchantCertify = { navController.navigate(Routes.MERCHANT_CERTIFY) },
                    onOpenAdminApprovals = { navController.navigate(Routes.ADMIN_APPROVAL_DASHBOARD) },
                    onOpenAdminUserList = { navController.navigate(Routes.ADMIN_USER_LIST) },
                    onOpenAdminReportList = { navController.navigate(Routes.ADMIN_REPORT_LIST) },
                    onOpenGovernanceCenter = { navController.navigate(Routes.ADMIN_DASHBOARD) },
                    onOpenPromotionCenter = { navController.navigate("promotion_center") },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) { popUpTo(0) }
                    },
                )
            }
            composable(Routes.WALLET) {
                WalletScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Routes.PERSONAL_INFO) {
                PersonalInfoScreen(
                    onBack = { navController.popBackStack() },
                    onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                    onOpenMembership = { navController.navigate(Routes.MEMBERSHIP) },
                )
            }
            composable(Routes.EDIT_PROFILE) { entry ->
                val croppedAvatar = entry.savedStateHandle.get<String>("cropped_avatar")
                EditProfileScreen(
                    croppedAvatar = croppedAvatar,
                    onBack = { navController.popBackStack() },
                    onNavigateToCrop = { navController.navigate(Routes.cropAvatar()) }
                )
            }
            composable(Routes.CROP_AVATAR) { entry ->
                CropScreen(
                    uriString = "avatar_temp.jpg",
                    onBack = { navController.popBackStack() },
                    onCropped = { uploadedUrl ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("cropped_avatar", uploadedUrl)
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.MEMBERSHIP) {
                MembershipScreen(onBack = { navController.popBackStack() })
            }
            composable("promotion_center") {
                com.qingyuan.lslife.feature.profile.PromotionCenterScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.MERCHANT_CERTIFY) {
                com.qingyuan.lslife.feature.profile.MerchantCertifyScreen(
                    navController = navController
                )
            }
            composable(
                route = Routes.PUBLIC_PROFILE,
                enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }) }
            ) { entry ->
                com.qingyuan.lslife.feature.profile.PublicProfileScreen(
                    userId = entry.arguments?.getString("userId") ?: "",
                    onBack = { navController.popBackStack() },
                    onOpenPost = { postId -> navController.navigate(Routes.postDetail(postId)) },
                    onChatClick = { targetId, targetName ->
                        navController.navigate(Routes.chat("new", targetId, targetName))
                    }
                )
            }
            
            composable(
                route = Routes.MERCHANT,
                enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }) }
            ) { entry ->
                com.qingyuan.lslife.feature.profile.MerchantStoreScreen(
                    merchantId = entry.arguments?.getString("merchantId") ?: "",
                    onBack = { navController.popBackStack() },
                    onChatClick = { navController.navigate(Routes.chat("draft", entry.arguments?.getString("merchantId") ?: "", "商家客服")) }
                )
            }

            composable(Routes.MESSAGE_LIST) { 
                ChatSessionListScreen(
                    onNavigateToChat = { sessionId, targetId, targetName ->
                        navController.navigate(Routes.chat(sessionId, targetId, targetName))
                    }
                )
            }
            composable(
                route = Routes.CHAT,
                arguments = listOf(
                    navArgument("initPostId") { nullable = true }
                )
            ) { entry ->
                ChatScreen(
                    sessionId = entry.arguments?.getString("sessionId").orEmpty(),
                    targetUserId = entry.arguments?.getString("targetUserId").orEmpty(),
                    targetName = java.net.URLDecoder.decode(entry.arguments?.getString("targetName").orEmpty(), "UTF-8"),
                    initPostId = entry.arguments?.getString("initPostId"),
                    onNavigateToProfile = { targetId -> navController.navigate(Routes.publicProfile(targetId)) },
                    onNavigateToPostDetail = { postId -> navController.navigate(Routes.postDetail(postId)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REAL_NAME_AUTH) {
                RealNameScreen(
                    onBack = { navController.popBackStack() }
                ) 
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) },
                    onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) { popUpTo(0) }
                    },
                )
            }
            composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PRIVACY) { PrivacyScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.MERCHANT) { entry ->
                MerchantDetailScreen(
                    merchantId = entry.arguments?.getString("merchantId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onChatClick = { targetId, targetName ->
                        navController.navigate(Routes.chat("new", targetId, targetName))
                    }
                )
            }
        }
        
        if (showPublishMenu) {
            PublishMenuBottomSheet(
                onDismiss = { showPublishMenu = false },
                onNavigateToPublish = { routeId -> 
                    navController.navigate(Routes.publish(null, routeId))
                }
            )
        }
        }

        // 应用内顶部悬浮消息胶囊 (In-App Top Dropdown Message Card)
        AnimatedVisibility(
            visible = inAppBanner != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .zIndex(999f)
        ) {
            inAppBanner?.let { banner ->
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            sessionViewModel.dismissInAppBanner()
                            navController.navigate(Routes.chat(banner.sessionId, banner.senderId, banner.senderName))
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        coil.compose.AsyncImage(
                            model = banner.avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(androidx.compose.ui.graphics.Color(0xFFEEEEEE)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = banner.senderName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "刚刚",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = banner.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
