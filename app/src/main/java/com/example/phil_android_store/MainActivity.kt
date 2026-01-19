package com.example.phil_android_store

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.CartManager
import com.example.phil_android_store.data.PurchaseManager
import com.example.phil_android_store.data.UserProfileManager
import com.example.phil_android_store.ui.screens.CartScreen
import com.example.phil_android_store.ui.screens.ProfileScreen
import com.example.phil_android_store.ui.screens.PurchaseHistoryScreen
import com.example.phil_android_store.ui.screens.SettingsScreen
import com.example.phil_android_store.ui.screens.StoreScreen
import com.example.phil_android_store.ui.screens.content.ContentScreen
import com.example.phil_android_store.ui.theme.Phil_Android_StoreTheme

class MainActivity : ComponentActivity() {
    private var navigationCallback: ((AppDestinations) -> Unit)? = null
    private var vipTabCallback: ((Boolean) -> Unit)? = null
    private var purchaseHistoryCallback: ((Boolean) -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Braze SDK: Ensure in-app message manager is subscribed to events
        BrazeInAppMessageManager.getInstance().ensureSubscribedToInAppMessageEvents(this)
        
        // Handle deep link
        val initialDestination = handleDeepLink(intent)
        val initialVipCategory = getDeepLinkCategory(intent)
        
        setContent {
            Phil_Android_StoreApp(
                initialDestination = initialDestination,
                initialVipCategory = initialVipCategory,
                onNavigationCallback = { callback ->
                    navigationCallback = callback
                },
                onVipTabCallback = { callback ->
                    vipTabCallback = callback
                },
                onPurchaseHistoryCallback = { callback ->
                    purchaseHistoryCallback = callback
                }
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Braze SDK: Register in-app message manager to display messages
        BrazeInAppMessageManager.getInstance().registerInAppMessageManager(this)
        
        // Check for deep link in current intent (handles case when app resumes after deep link)
        handleDeepLinkNavigation(intent)
    }
    
    override fun onPause() {
        super.onPause()
        // Braze SDK: Unregister in-app message manager to prevent memory leaks
        BrazeInAppMessageManager.getInstance().unregisterInAppMessageManager(this)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle deep link when app is already running
        handleDeepLinkNavigation(intent)
    }
    
    private fun handleDeepLinkNavigation(intent: Intent?) {
        val destination = handleDeepLink(intent)
        val category = getDeepLinkCategory(intent)
        
        // Use post to ensure callback is set if called during activity creation
        window.decorView.post {
            if (category == "PURCHASE_HISTORY") {
                // For purchase history, navigate to profile first, then show purchase history
                navigationCallback?.invoke(AppDestinations.PROFILE)
                purchaseHistoryCallback?.invoke(true)
            } else if (category == "VIP") {
                if (destination != null) {
                    navigationCallback?.invoke(destination)
                }
                vipTabCallback?.invoke(true)
            } else if (destination != null) {
                navigationCallback?.invoke(destination)
            }
        }
    }
    
    fun handleDeepLink(intent: Intent?): AppDestinations? {
        val data: Uri? = intent?.data
        if (data != null && "philstore" == data.scheme) {
            val host = data.host
            android.util.Log.d("MainActivity", "Deep link detected - scheme: ${data.scheme}, host: $host")
            
            // Handle host with or without dash (in case of URL encoding issues)
            val normalizedHost = host?.lowercase()?.replace("%2d", "-")?.replace("%2D", "-")
            
            if (normalizedHost == "login" || normalizedHost == "profile") {
                return AppDestinations.PROFILE
            }
            if (normalizedHost == "vip") {
                return AppDestinations.HOME
            }
            if (normalizedHost == "cart") {
                return AppDestinations.CART
            }
            if (normalizedHost == "purchase-history" || normalizedHost == "history") {
                android.util.Log.d("MainActivity", "Purchase history deep link detected")
                return AppDestinations.PROFILE // Navigate to profile first, then show purchase history
            }
        }
        return null
    }
    
    fun getDeepLinkCategory(intent: Intent?): String? {
        val data: Uri? = intent?.data
        if (data != null && "philstore" == data.scheme) {
            val host = data.host
            // Handle host with or without dash (in case of URL encoding issues)
            val normalizedHost = host?.lowercase()?.replace("%2d", "-")?.replace("%2D", "-")
            
            if (normalizedHost == "vip") {
                return "VIP"
            }
            if (normalizedHost == "purchase-history" || normalizedHost == "history") {
                android.util.Log.d("MainActivity", "Purchase history category detected")
                return "PURCHASE_HISTORY"
            }
        }
        return null
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@PreviewScreenSizes
@Composable
fun Phil_Android_StoreApp(
    initialDestination: AppDestinations? = null,
    initialVipCategory: String? = null,
    onNavigationCallback: ((AppDestinations) -> Unit) -> Unit = {},
    onVipTabCallback: ((Boolean) -> Unit) -> Unit = {},
    onPurchaseHistoryCallback: ((Boolean) -> Unit) -> Unit = {}
) {
    val context = LocalContext.current
    val profileManager = remember { UserProfileManager(context) }
    
    // Load dark mode from current user profile on startup
    var isDarkMode by remember {
        mutableStateOf(
            profileManager.getCurrentProfile()?.isDarkModeEnabled ?: false
        )
    }
    
    // Determine initial destination - if purchase history, go to profile
    val computedInitialDestination = if (initialVipCategory == "PURCHASE_HISTORY") {
        AppDestinations.PROFILE
    } else {
        initialDestination ?: AppDestinations.HOME
    }
    
    var currentDestination by rememberSaveable { 
        mutableStateOf(computedInitialDestination) 
    }
    
    // Track if we should show VIP tab on home screen
    var showVipTab by remember { mutableStateOf(initialVipCategory == "VIP") }
    
    // Refresh key that changes when login/logout happens to trigger UI refresh
    var refreshKey by remember { mutableIntStateOf(0) }
    var showPurchaseHistory by remember { mutableStateOf(initialVipCategory == "PURCHASE_HISTORY") }
    var showSettings by remember { mutableStateOf(false) }
    
    // Register navigation callback with activity so it can trigger navigation updates
    DisposableEffect(Unit) {
        onNavigationCallback { destination ->
            currentDestination = destination
        }
        onVipTabCallback { shouldShow ->
            showVipTab = shouldShow
        }
        onDispose { }
    }
    
    // Observe intent changes to handle deep links (only if not already set from initial values)
    LaunchedEffect(Unit) {
        // Only process if we don't already have purchase history or VIP set from initial deep link
        if (!showPurchaseHistory && !showVipTab) {
            val activity = context as? MainActivity
            activity?.let {
                val destination = it.handleDeepLink(it.intent)
                // Check if deep link is for VIP tab or purchase history
                val category = it.getDeepLinkCategory(it.intent)
                if (category == "PURCHASE_HISTORY") {
                    // For purchase history, navigate to profile and show purchase history
                    currentDestination = AppDestinations.PROFILE
                    showPurchaseHistory = true
                } else if (category == "VIP") {
                    if (destination != null) {
                        currentDestination = destination
                    }
                    showVipTab = true
                } else if (destination != null) {
                    currentDestination = destination
                }
            }
        }
    }
    
    // Braze SDK: Initialize Braze session on app startup
    LaunchedEffect(Unit) {
        val currentProfile = profileManager.getCurrentProfile()
        if (currentProfile != null) {
            // Braze SDK: User is logged in - identify them and set active_member=true
            // Do NOT send profile attributes (those are only sent when Save Profile is clicked)
            BrazeUserSync.loginUserToBraze(context, currentProfile.userId)
            
            // Braze SDK: Sync VIP status on app startup
            val purchaseManager = PurchaseManager(context)
            val isVip = purchaseManager.isVip(currentProfile.userId)
            BrazeUserSync.syncVipStatusToBraze(context, currentProfile.userId, isVip)
        } else {
            // Braze SDK: No user logged in - initialize anonymous session
            BrazeUserSync.initializeAnonymousSession(context)
        }
        
        // Braze SDK: Request banner refresh for all banner placements on app startup
        BrazeUserSync.requestBannerRefresh(context, listOf("store_page_banner", "cart_banner"))
        
        // Braze SDK: Request Content Cards refresh on app startup
        BrazeUserSync.requestContentCardsRefresh(context)
    }
    var bannerContent by remember { mutableStateOf<String?>(null) } // Can be set to a string to show banner
    
    // Set up purchase history callback
    LaunchedEffect(Unit) {
        onPurchaseHistoryCallback { shouldShow ->
            showPurchaseHistory = shouldShow
            if (shouldShow) {
                currentDestination = AppDestinations.PROFILE // Navigate to profile first
            }
        }
    }
    
    // Create cart manager instance
    val cartManager = remember { CartManager(context) }
    
    // Observe cart item count for badge - update when destination changes
    var cartItemCount by remember { mutableIntStateOf(cartManager.cartItemCount) }
    
    // Update cart count when destination changes or when refreshKey changes (login/logout)
    LaunchedEffect(currentDestination, refreshKey) {
        cartItemCount = cartManager.cartItemCount
    }

    Phil_Android_StoreTheme(darkTheme = isDarkMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Persistent Top Banner (hide when Settings is shown, as it has its own banner)
            if (!showSettings) {
                TopBanner()
            }
            
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    AppDestinations.entries.forEach {
                        item(
                            icon = {
                                // Show badge on cart icon if there are items
                                if (it == AppDestinations.CART && cartItemCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge {
                                                Text(
                                                    text = cartItemCount.toString(),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            it.icon,
                                            contentDescription = it.label
                                        )
                                    }
                                } else {
                                Icon(
                                    it.icon,
                                    contentDescription = it.label
                                )
                                }
                            },
                            label = { Text(it.label) },
                            selected = it == currentDestination && !showPurchaseHistory && !showSettings,
                            onClick = { 
                                currentDestination = it
                                showPurchaseHistory = false // Close purchase history when navigating
                                showSettings = false // Close settings when navigating
                            }
                        )
                    }
                }
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    if (showSettings) {
                        // Show Settings screen (no bottom nav tabs)
                        SettingsScreen(
                            onClose = { showSettings = false }
                        )
                    } else if (showPurchaseHistory) {
                        // Show Purchase History screen within the main UI
                        PurchaseHistoryScreen(
                            onBack = { showPurchaseHistory = false }
                        )
                    } else {
                        when (currentDestination) {
                            AppDestinations.HOME -> {
                                StoreScreen(
                                    bannerContent = bannerContent,
                                    onShowBannerMessage = { }, // No longer needed, handled internally
                                    initialCategory = if (showVipTab) "VIP" else null,
                                    refreshKey = refreshKey,
                                    cartManager = cartManager,
                                    onCartUpdated = {
                                        // Update cart count immediately when item is added
                                        cartItemCount = cartManager.cartItemCount
                                    }
                                )
                            }
                            AppDestinations.CART -> {
                                CartScreen(
                                    cartManager = cartManager,
                                    onCartUpdated = {
                                        // Update cart count immediately when cart changes
                                        cartItemCount = cartManager.cartItemCount
                                    }
                                )
                            }
                            AppDestinations.CONTENT -> {
                                ContentScreen()
                            }
                        AppDestinations.PROFILE -> ProfileScreen(
                            onDarkModeChanged = { enabled ->
                                isDarkMode = enabled
                                },
                                onLoginStateChanged = {
                                    refreshKey++ // Trigger refresh in StoreScreen
                                },
                                onNavigateToPurchaseHistory = {
                                    showPurchaseHistory = true
                                },
                                onNavigateToSettings = {
                                    showSettings = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBanner() {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Status bar background to match banner color
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarPadding.calculateTopPadding()),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}
        
        // Banner content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Demo Store",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    HOME("Store", Icons.Default.Home),
    CART("Cart", Icons.Default.ShoppingCart),
    CONTENT("Content", Icons.Default.Article),
    PROFILE("Profile", Icons.Default.AccountBox),
}