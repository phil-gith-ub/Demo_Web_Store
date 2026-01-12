package com.example.phil_android_store

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.UserProfileManager
import com.example.phil_android_store.ui.screens.CartScreen
import com.example.phil_android_store.ui.screens.ProfileScreen
import com.example.phil_android_store.ui.screens.StoreScreen
import com.example.phil_android_store.ui.theme.Phil_Android_StoreTheme

class MainActivity : ComponentActivity() {
    private var navigationCallback: ((AppDestinations) -> Unit)? = null
    private var vipTabCallback: ((Boolean) -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Ensure in-app message manager is subscribed to events
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
                }
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Register in-app message manager to display messages
        BrazeInAppMessageManager.getInstance().registerInAppMessageManager(this)
        
        // Check for deep link in current intent (handles case when app resumes after deep link)
        handleDeepLinkNavigation(intent)
    }
    
    override fun onPause() {
        super.onPause()
        // Unregister in-app message manager to prevent memory leaks
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
        if (destination != null) {
            // Use post to ensure callback is set if called during activity creation
            window.decorView.post {
                navigationCallback?.invoke(destination)
                // Check if deep link is for VIP tab
                val category = getDeepLinkCategory(intent)
                if (category == "VIP") {
                    vipTabCallback?.invoke(true)
                }
            }
        }
    }
    
    fun getDeepLinkCategoryForComposable(intent: Intent?): String? {
        return getDeepLinkCategory(intent)
    }
    
    fun handleDeepLink(intent: Intent?): AppDestinations? {
        val data: Uri? = intent?.data
        if (data != null && "philstore" == data.scheme) {
            val host = data.host
            if (host == "login" || host == "profile") {
                return AppDestinations.PROFILE
            }
            if (host == "vip") {
                return AppDestinations.HOME
            }
        }
        return null
    }
    
    fun getDeepLinkCategory(intent: Intent?): String? {
        val data: Uri? = intent?.data
        if (data != null && "philstore" == data.scheme) {
            val host = data.host
            if (host == "vip") {
                return "VIP"
            }
        }
        return null
    }
}

@PreviewScreenSizes
@Composable
fun Phil_Android_StoreApp(
    initialDestination: AppDestinations? = null,
    initialVipCategory: String? = null,
    onNavigationCallback: ((AppDestinations) -> Unit) -> Unit = {},
    onVipTabCallback: ((Boolean) -> Unit) -> Unit = {}
) {
    val context = LocalContext.current
    val profileManager = remember { UserProfileManager(context) }
    
    // Load dark mode from current user profile on startup
    var isDarkMode by remember {
        mutableStateOf(
            profileManager.getCurrentProfile()?.isDarkModeEnabled ?: false
        )
    }
    
    var currentDestination by rememberSaveable { 
        mutableStateOf(initialDestination ?: AppDestinations.HOME) 
    }
    
    // Track if we should show VIP tab on home screen
    var showVipTab by remember { mutableStateOf(initialVipCategory == "VIP") }
    
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
    
    // Observe intent changes to handle deep links
    LaunchedEffect(Unit) {
        val activity = context as? MainActivity
        activity?.let {
            val destination = it.handleDeepLink(it.intent)
            if (destination != null) {
                currentDestination = destination
            }
            // Check if deep link is for VIP tab
            val category = it.getDeepLinkCategory(it.intent)
            if (category == "VIP") {
                showVipTab = true
            }
        }
    }
    
    // Initialize Braze session on app startup
    LaunchedEffect(Unit) {
        val currentProfile = profileManager.getCurrentProfile()
        if (currentProfile != null) {
            // User is logged in - identify them and set active_member=true
            // Do NOT send profile attributes (those are only sent when Save Profile is clicked)
            BrazeUserSync.loginUserToBraze(context, currentProfile.userId)
        } else {
            // No user logged in - initialize anonymous session
            BrazeUserSync.initializeAnonymousSession(context)
        }
    }
    var bannerContent by remember { mutableStateOf<String?>(null) } // Can be set to a string to show banner

    Phil_Android_StoreTheme(darkTheme = isDarkMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Persistent Top Banner
            TopBanner()
            
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    AppDestinations.entries.forEach {
                        item(
                            icon = {
                                Icon(
                                    it.icon,
                                    contentDescription = it.label
                                )
                            },
                            label = { Text(it.label) },
                            selected = it == currentDestination,
                            onClick = { currentDestination = it }
                        )
                    }
                }
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentDestination) {
                        AppDestinations.HOME -> StoreScreen(
                            bannerContent = bannerContent,
                            onShowBannerMessage = { }, // No longer needed, handled internally
                            initialCategory = if (showVipTab) "VIP" else null
                        )
                        AppDestinations.CART -> CartScreen()
                        AppDestinations.PROFILE -> ProfileScreen(
                            onDarkModeChanged = { enabled ->
                                isDarkMode = enabled
                            }
                        )
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
    PROFILE("Profile", Icons.Default.AccountBox),
}