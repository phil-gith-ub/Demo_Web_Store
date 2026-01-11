package com.example.phil_android_store.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.phil_android_store.data.FeatureFlags
import com.example.phil_android_store.data.UserProfile
import com.example.phil_android_store.data.UserProfileManager

@Composable
fun ProfileScreen(
    onDarkModeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val profileManager = remember { UserProfileManager(context) }
    
    // Check if user is already logged in
    val currentUserId = profileManager.getCurrentUserId()
    val currentProfile = currentUserId?.let { profileManager.getProfile(it) }
    
    var userId by rememberSaveable { mutableStateOf(currentUserId ?: "") }
    var isLoggedIn by remember { mutableStateOf(profileManager.isLoggedIn()) }
    
    // Profile fields
    var firstName by rememberSaveable { mutableStateOf(currentProfile?.firstName ?: "") }
    var lastName by rememberSaveable { mutableStateOf(currentProfile?.lastName ?: "") }
    var email by rememberSaveable { mutableStateOf(currentProfile?.email ?: "") }
    var mobile by rememberSaveable { mutableStateOf(currentProfile?.mobile ?: "") }
    var favoriteCategory by rememberSaveable { mutableStateOf(currentProfile?.favoriteProductCategory ?: "") }
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
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
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
                    
                    OutlinedTextField(
                        value = favoriteCategory,
                        onValueChange = { favoriteCategory = it },
                        label = { Text("Favorite Product Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
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
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Profile")
                    }
                    
                    Button(
                        onClick = {
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
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout")
                    }
                }

                Text(
                    text = "Member Status: ${if (isLoggedIn) "Member" else "Guest"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
