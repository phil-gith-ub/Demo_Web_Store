package com.example.phil_android_store

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
import com.example.phil_android_store.data.UserProfileManager
import com.example.phil_android_store.ui.screens.CartScreen
import com.example.phil_android_store.ui.screens.ProfileScreen
import com.example.phil_android_store.ui.screens.StoreScreen
import com.example.phil_android_store.ui.theme.Phil_Android_StoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Phil_Android_StoreApp()
        }
    }
}

@PreviewScreenSizes
@Composable
fun Phil_Android_StoreApp() {
    val context = LocalContext.current
    val profileManager = remember { UserProfileManager(context) }
    
    // Load dark mode from current user profile on startup
    var isDarkMode by remember {
        mutableStateOf(
            profileManager.getCurrentProfile()?.isDarkModeEnabled ?: false
        )
    }
    
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
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
                            onShowBannerMessage = { } // No longer needed, handled internally
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
                    text = "Phil's Store",
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
    HOME("Home", Icons.Default.Home),
    CART("Cart", Icons.Default.ShoppingCart),
    PROFILE("Profile", Icons.Default.AccountBox),
}