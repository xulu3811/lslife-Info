package com.lianshan.lslife.ui

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
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.lianshan.lslife.feature.chat.ChatSessionListScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lianshan.lslife.R
import com.lianshan.lslife.feature.auth.ForgotPasswordScreen
import com.lianshan.lslife.feature.auth.LoginScreen
import com.lianshan.lslife.feature.cart.CartScreen
import com.lianshan.lslife.feature.cart.CheckoutScreen
import com.lianshan.lslife.feature.home.HomeScreen
import com.lianshan.lslife.feature.category.CategoryScreen
import com.lianshan.lslife.feature.search.SearchScreen
import com.lianshan.lslife.feature.merchant.MerchantDetailScreen
import com.lianshan.lslife.feature.orders.OrderListScreen
import com.lianshan.lslife.feature.orders.OrderTrackScreen
import com.lianshan.lslife.feature.profile.AddressScreen
import com.lianshan.lslife.feature.profile.CropScreen
import com.lianshan.lslife.feature.profile.EditProfileScreen
import com.lianshan.lslife.feature.profile.MembershipScreen
import com.lianshan.lslife.feature.profile.RealNameAgreementScreen
import com.lianshan.lslife.feature.chat.ChatSessionListScreen
import com.lianshan.lslife.feature.chat.ChatScreen
import com.lianshan.lslife.feature.profile.MyPostsScreen
import com.lianshan.lslife.feature.profile.PersonalInfoScreen
import com.lianshan.lslife.feature.profile.ProfileScreen
import com.lianshan.lslife.feature.profile.RealNameScreen
import com.lianshan.lslife.feature.publish.PublishScreen
import com.lianshan.lslife.feature.wallet.WalletScreen
import com.lianshan.lslife.feature.settings.AboutScreen
import com.lianshan.lslife.feature.settings.PrivacyScreen
import com.lianshan.lslife.feature.settings.SettingsScreen
import com.lianshan.lslife.ui.navigation.Routes
import com.lianshan.lslife.ui.components.PublishMenuBottomSheet

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
    var showPublishMenu by remember { mutableStateOf(false) }

    val cartItemCount by sessionViewModel.cartItemCount.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        sessionViewModel.navigateToChatFlow.collect { (sessionId, targetUserId, targetName) ->
            navController.navigate(Routes.chat(sessionId, targetUserId, targetName))
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val mainRoutes = tabs.map { it.route } + Routes.PUBLISH
    val showBottomBar = currentRoute in mainRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                Box {
                    NavigationBar(
                        modifier = Modifier.height(60.dp),
                        containerColor = androidx.compose.ui.graphics.Color.White,
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
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    if (isPublish) {
                                        Box(
                                            modifier = Modifier
                                                .size(37.dp)
                                                .border(
                                                    width = 1.2.dp,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    shape = androidx.compose.foundation.shape.CircleShape
                                                )
                                                .background(androidx.compose.ui.graphics.Color.Transparent)
                                                .padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = stringResource(tab.labelRes),
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(24.dp),
                                            )
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
                                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray,
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
                    onNavigateToCategory = { id, name -> navController.navigate(Routes.categoryDetail(id, name)) }
                )
            }
            composable(Routes.CATEGORY) {
                CategoryScreen(
                    onNavigateToCategory = { id, name -> navController.navigate(Routes.categoryDetail(id, name)) },
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                    onOpenPost = { postId -> navController.navigate(Routes.postDetail(postId)) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onPostClick = { navController.navigate(Routes.postDetail(it)) }
                )
            }
            composable(
                route = Routes.CATEGORY_DETAIL,
                arguments = listOf(
                    navArgument("categoryId") { nullable = false },
                    navArgument("categoryName") { nullable = false }
                )
            ) {
                com.lianshan.lslife.feature.category.CategoryDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPost = { navController.navigate(Routes.postDetail(it)) }
                )
            }
            composable(Routes.ORDERS) {
                OrderListScreen(onTrack = { navController.navigate(Routes.orderTrack(it)) })
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
            composable(Routes.MY_POSTS) {
                MyPostsScreen(
                    onBack = { navController.popBackStack() },
                    onEditPost = { postId -> navController.navigate(Routes.publish(postId)) }
                )
            }
            composable(Routes.POST_DETAIL) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                val context = androidx.compose.ui.platform.LocalContext.current
                com.lianshan.lslife.feature.home.PostDetailScreen(
                    postId = postId,
                    onBack = { navController.popBackStack() },
                    onChatClick = { targetId, targetName ->
                        navController.navigate(Routes.chat("new", targetId, targetName, initPostId = postId))
                    },
                    onBuyClick = { id ->
                        navController.navigate(Routes.CART)
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
            composable(Routes.CART) {
                CartScreen(
                    onOpenMerchant = { navController.navigate(Routes.merchant(it)) },
                    onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                    onCheckout = { mId, sId, eIds, dMethod -> navController.navigate(Routes.checkout(mId, sId, eIds, dMethod)) }
                )
            }
            composable(
                route = Routes.CHECKOUT,
                arguments = listOf(
                    navArgument("merchantId") { nullable = true },
                    navArgument("sellerId") { nullable = true },
                    navArgument("entryIds") { nullable = true },
                    navArgument("deliveryMethod") { nullable = true }
                )
            ) { entry ->
                CheckoutScreen(
                    merchantId = entry.arguments?.getString("merchantId"),
                    sellerId = entry.arguments?.getString("sellerId"),
                    onBack = { navController.popBackStack() },
                    onOrderCreated = { orderId -> navController.navigate(Routes.orderTrack(orderId)) { popUpTo(Routes.CART) } },
                    onAddressClick = { navController.navigate(Routes.ADDRESS_LIST) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenPersonalInfo = { navController.navigate(Routes.PERSONAL_INFO) },
                    onOpenMembership = { navController.navigate(Routes.MEMBERSHIP) },
                    onOpenAddress = { navController.navigate(Routes.ADDRESS_LIST) },
                    onOpenMessage = { navController.navigate(Routes.MESSAGE_LIST) },
                    onOpenRealName = { navController.navigate(Routes.REAL_NAME_AGREEMENT) },
                    onOpenMyPosts = { navController.navigate(Routes.MY_POSTS) },
                    onOpenWallet = { navController.navigate(Routes.WALLET) },
                    onOpenMerchantCertify = { navController.navigate(Routes.MERCHANT_CERTIFY) },
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
            composable(Routes.MERCHANT_CERTIFY) {
                com.lianshan.lslife.feature.profile.MerchantCertifyScreen(
                    navController = navController
                )
            }
            composable(Routes.PUBLIC_PROFILE) { entry ->
                com.lianshan.lslife.feature.profile.PublicProfileScreen(
                    userId = entry.arguments?.getString("userId") ?: "",
                    onBack = { navController.popBackStack() },
                    onOpenPost = { postId -> navController.navigate(Routes.postDetail(postId)) },
                    onChatClick = { targetId, targetName -> navController.navigate(Routes.chat("draft", targetId, targetName)) }
                )
            }
            composable(Routes.ADDRESS_LIST) { AddressScreen(onBack = { navController.popBackStack() }) }
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
                    targetName = entry.arguments?.getString("targetName").orEmpty(),
                    initPostId = entry.arguments?.getString("initPostId"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REAL_NAME_AGREEMENT) { 
                RealNameAgreementScreen(
                    onAgree = { signature -> navController.navigate(Routes.realNameAuth(signature)) {
                        popUpTo(Routes.REAL_NAME_AGREEMENT) { inclusive = true }
                    } },
                    onBack = { navController.popBackStack() }
                ) 
            }
            composable(Routes.REAL_NAME_AUTH) { entry ->
                RealNameScreen(
                    signature = entry.arguments?.getString("signature").orEmpty(),
                    onBack = { navController.popBackStack() }
                ) 
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) },
                    onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                )
            }
            composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PRIVACY) { PrivacyScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.MERCHANT) { entry ->
                MerchantDetailScreen(
                    merchantId = entry.arguments?.getString("merchantId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onCheckedOut = { orderId -> navController.navigate(Routes.orderTrack(orderId)) },
                    onChatClick = { targetId, targetName ->
                        navController.navigate(Routes.chat("new", targetId, targetName))
                    }
                )
            }
            composable(Routes.ORDER_TRACK) { entry ->
                OrderTrackScreen(
                    orderId = entry.arguments?.getString("orderId").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
        }
        
        if (showPublishMenu) {
            PublishMenuBottomSheet(
                onDismiss = { showPublishMenu = false },
                onNavigateToPublish = { categoryId -> navController.navigate(Routes.publish(null, categoryId)) }
            )
        }
    }
}
