package com.example.phil_android_store.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.FeatureFlags
import com.example.phil_android_store.data.PurchaseManager
import com.example.phil_android_store.data.UserProfile
import com.example.phil_android_store.data.UserProfileManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onDarkModeChanged: (Boolean) -> Unit,
    onLoginStateChanged: () -> Unit = {},
    onNavigateToPurchaseHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val profileManager = remember { UserProfileManager(context) }
    val purchaseManager = remember { PurchaseManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Check if user is already logged in
    val currentUserId = profileManager.getCurrentUserId()
    val currentProfile = currentUserId?.let { profileManager.getProfile(it) }
    
    var userId by rememberSaveable { mutableStateOf(currentUserId ?: "") }
    var isLoggedIn by remember { mutableStateOf(profileManager.isLoggedIn()) }
    
    // Check if user is VIP based on total purchases > $1000
    var isVip by remember { mutableStateOf(false) }
    
    // Refresh VIP status when login state changes or when user ID changes
    LaunchedEffect(isLoggedIn, currentUserId) {
        if (isLoggedIn && currentUserId != null) {
            isVip = purchaseManager.isVip(currentUserId)
            // Braze SDK: Sync VIP status to Braze whenever we check it
            BrazeUserSync.syncVipStatusToBraze(context, currentUserId, isVip)
        } else {
            isVip = false
        }
    }
    
    // Refresh VIP status when screen becomes visible (in case purchases were made elsewhere)
    LaunchedEffect(Unit) {
        if (isLoggedIn && currentUserId != null) {
            val newVipStatus = purchaseManager.isVip(currentUserId)
            if (newVipStatus != isVip) {
                isVip = newVipStatus
                // Braze SDK: Sync VIP status to Braze when it changes
                BrazeUserSync.syncVipStatusToBraze(context, currentUserId, newVipStatus)
            }
        }
    }
    
    // Product categories for dropdown
    val productCategories = listOf(
        "Electronics",
        "Clothing",
        "Appliances",
        "Accessories",
        "Fitness"
    )
    
    // Profile fields
    var firstName by rememberSaveable { mutableStateOf(currentProfile?.firstName ?: "") }
    var lastName by rememberSaveable { mutableStateOf(currentProfile?.lastName ?: "") }
    var email by rememberSaveable { mutableStateOf(currentProfile?.email ?: "") }
    var mobile by rememberSaveable { mutableStateOf(currentProfile?.mobile ?: "") }
    var favoriteCategory by rememberSaveable { mutableStateOf(currentProfile?.favoriteProductCategory ?: "") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(currentProfile?.isDarkModeEnabled ?: false) }
    
    // Load profile when component initializes or user logs in
    LaunchedEffect(isLoggedIn, currentUserId) {
        if (isLoggedIn && currentUserId != null) {
            val profile = profileManager.getProfile(currentUserId)
            firstName = profile.firstName
            lastName = profile.lastName
            email = profile.email
            mobile = profile.mobile
            favoriteCategory = profile.favoriteProductCategory
            isDarkMode = profile.isDarkModeEnabled
            onDarkModeChanged(isDarkMode)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile title
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("User ID") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoggedIn
                )

                if (!isLoggedIn) {
                    Button(
                        onClick = {
                            if (userId.isNotBlank()) {
                                val profile = profileManager.getProfile(userId)
                                profileManager.setCurrentUserId(userId)
                                isLoggedIn = true
                                
                                // Load existing profile data
                                firstName = profile.firstName
                                lastName = profile.lastName
                                email = profile.email
                                mobile = profile.mobile
                                favoriteCategory = profile.favoriteProductCategory
                                isDarkMode = profile.isDarkModeEnabled
                                onDarkModeChanged(isDarkMode)
                                
                                // Braze SDK: Login user to Braze (calls changeUser, sets active_member=true)
                                BrazeUserSync.loginUserToBraze(context, userId)
                                
                                // Braze SDK: Log login event immediately after changeUser
                                BrazeUserSync.logLoggedIn(context, userId)
                                
                                // Braze SDK: Refresh VIP status and sync to Braze
                                isVip = purchaseManager.isVip(userId)
                                BrazeUserSync.syncVipStatusToBraze(context, userId, isVip)
                                
                                onLoginStateChanged() // Notify MainActivity to refresh StoreScreen
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = userId.isNotBlank()
                    ) {
                        Text("Login")
                    }
                } else {
                    // Show additional fields when logged in
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Favorite Product Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = favoriteCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Favorite Product Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false }
                        ) {
                            productCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        favoriteCategory = category
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Save button to update profile
                    Button(
                        onClick = {
                            val profile = UserProfile(
                                userId = userId,
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                mobile = mobile,
                                favoriteProductCategory = favoriteCategory,
                                isDarkModeEnabled = isDarkMode
                            )
                            profileManager.saveProfile(profile)
                            
                            // Braze SDK: Sync updated profile to Braze
                            BrazeUserSync.syncUserToBraze(context, profile)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Profile")
                    }
                    
                    Button(
                        onClick = {
                            // Braze SDK: Set active_member=false and log logged_out event (no changeUser, no wipeData)
                            // Note: VIP status is NOT changed on logout - it persists based on purchase history
                            BrazeUserSync.onUserLogout(context, userId)
                            
                            profileManager.clearCurrentUser()
                            isLoggedIn = false
                            userId = ""
                            firstName = ""
                            lastName = ""
                            email = ""
                            mobile = ""
                            favoriteCategory = ""
                            isDarkMode = false
                            onDarkModeChanged(false)
                            
                            // Refresh VIP status and notify MainActivity
                            isVip = false
                            onLoginStateChanged() // Notify MainActivity to refresh StoreScreen
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Text(
                    text = "Member Status: ${if (isLoggedIn) "Member" else "Guest"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Show VIP tag if user has spent more than $1000
                    if (isVip) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "VIP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Purchase History button (only shown if logged in)
        if (isLoggedIn) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onNavigateToPurchaseHistory,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Purchase History")
                    }
                }
            }
        }
        
        // Dark Mode Toggle (only shown if feature flag is enabled)
        if (FeatureFlags.isDarkModeToggleEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDarkMode) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { enabled ->
                                isDarkMode = enabled
                                onDarkModeChanged(enabled)
                                
                                // Save to current user profile if logged in
                                if (isLoggedIn && userId.isNotBlank()) {
                                    val profile = profileManager.getProfile(userId)
                                    val updatedProfile = profile.copy(isDarkModeEnabled = enabled)
                                    profileManager.saveProfile(updatedProfile)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
