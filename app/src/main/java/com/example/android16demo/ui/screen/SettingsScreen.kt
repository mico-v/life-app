package com.example.android16demo.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android16demo.ui.theme.Android16DemoTheme
import com.example.android16demo.viewmodel.SettingsUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Settings screen with sync configuration, server settings, and push templates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onSyncNow: () -> Unit,
    onAutoSyncToggle: (Boolean) -> Unit,
    onWifiOnlyToggle: (Boolean) -> Unit,
    onServerUrlChange: (String) -> Unit,
    onPushTemplateChange: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    onErrorDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Login form state
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginForm by remember { mutableStateOf(false) }
    
    // Server URL editing state
    var editingServerUrl by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(uiState.serverUrl) }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onErrorDismiss()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onErrorDismiss()
        }
    }
    
    // Update serverUrlInput when uiState changes
    LaunchedEffect(uiState.serverUrl) {
        serverUrlInput = uiState.serverUrl
    }
    
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Section
            item {
                SettingsSectionHeader(title = "Account")
            }
            
            item {
                AccountCard(
                    isLoggedIn = uiState.isLoggedIn,
                    username = uiState.username,
                    isLoggingIn = uiState.isLoggingIn,
                    showLoginForm = showLoginForm,
                    loginUsername = loginUsername,
                    loginPassword = loginPassword,
                    onUsernameChange = { loginUsername = it },
                    onPasswordChange = { loginPassword = it },
                    onShowLoginForm = { showLoginForm = true },
                    onHideLoginForm = { showLoginForm = false },
                    onLogin = {
                        onLogin(loginUsername, loginPassword)
                        loginPassword = ""
                    },
                    onLogout = onLogout
                )
            }
            
            // Server Section
            item {
                SettingsSectionHeader(title = "Server Configuration")
            }
            
            item {
                ServerConfigCard(
                    serverUrl = serverUrlInput,
                    isEditing = editingServerUrl,
                    onServerUrlChange = { serverUrlInput = it },
                    onStartEditing = { editingServerUrl = true },
                    onSaveServerUrl = {
                        onServerUrlChange(serverUrlInput)
                        editingServerUrl = false
                    },
                    onCancelEditing = {
                        serverUrlInput = uiState.serverUrl
                        editingServerUrl = false
                    }
                )
            }
            
            // Push Templates Section
            item {
                SettingsSectionHeader(title = "Push Notifications")
            }
            
            item {
                PushTemplateCard(
                    currentTemplateId = uiState.pushTemplateId,
                    onTemplateChange = onPushTemplateChange
                )
            }
            
            // Sync Section
            item {
                SettingsSectionHeader(title = "Sync")
            }
            
            item {
                SyncCard(
                    isLoggedIn = uiState.isLoggedIn,
                    lastSyncTime = uiState.lastSyncTime,
                    isSyncing = uiState.isSyncing,
                    autoSyncEnabled = uiState.autoSyncEnabled,
                    syncOnWifiOnly = uiState.syncOnWifiOnly,
                    onSyncNow = onSyncNow,
                    onAutoSyncToggle = onAutoSyncToggle,
                    onWifiOnlyToggle = onWifiOnlyToggle
                )
            }
            
            // About Section
            item {
                SettingsSectionHeader(title = "About")
            }
            
            item {
                AboutCard()
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ServerConfigCard(
    serverUrl: String,
    isEditing: Boolean,
    onServerUrlChange: (String) -> Unit,
    onStartEditing: () -> Unit,
    onSaveServerUrl: () -> Unit,
    onCancelEditing: () -> Unit
) {
    // Validate server URL format (domain:port or IP:port)
    val isValidUrl = serverUrl.isBlank() || isValidServerUrl(serverUrl)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Remote Server",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Configure the server for push notifications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isEditing) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = onServerUrlChange,
                    label = { Text("Server URL (domain:port or IP:port)") },
                    placeholder = { Text("example.com:8080 or 192.168.1.100:8080") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isValidUrl,
                    supportingText = if (!isValidUrl) {
                        { Text("Invalid format. Use domain:port or IP:port") }
                    } else null
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelEditing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onSaveServerUrl,
                        modifier = Modifier.weight(1f),
                        enabled = isValidUrl
                    ) {
                        Text("Save")
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartEditing() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (serverUrl.isNotBlank()) serverUrl else "Not configured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (serverUrl.isNotBlank()) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PushTemplateCard(
    currentTemplateId: String?,
    onTemplateChange: (String?) -> Unit
) {
    val templates = listOf(
        "default" to "Default Notification",
        "urgent" to "Urgent Alert",
        "silent" to "Silent Push",
        "summary" to "Daily Summary"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Push Template",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Select notification style for remote push",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            templates.forEach { (id, name) ->
                val isSelected = currentTemplateId == id || (currentTemplateId == null && id == "default")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTemplateChange(id) }
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = isSelected,
                        onClick = { onTemplateChange(id) }
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    isLoggedIn: Boolean,
    username: String?,
    isLoggingIn: Boolean,
    showLoginForm: Boolean,
    loginUsername: String,
    loginPassword: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onShowLoginForm: () -> Unit,
    onHideLoginForm: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isLoggedIn) {
                // Logged in state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = username ?: "User",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Logged in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Logout", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            } else if (showLoginForm) {
                // Login form
                OutlinedTextField(
                    value = loginUsername,
                    onValueChange = onUsernameChange,
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onHideLoginForm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onLogin,
                        modifier = Modifier.weight(1f),
                        enabled = loginUsername.isNotBlank() && loginPassword.isNotBlank() && !isLoggingIn
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Login")
                        }
                    }
                }
            } else {
                // Not logged in, show login button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowLoginForm() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Sign in to sync",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Backup your tasks and access them anywhere",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncCard(
    isLoggedIn: Boolean,
    lastSyncTime: Long,
    isSyncing: Boolean,
    autoSyncEnabled: Boolean,
    syncOnWifiOnly: Boolean,
    onSyncNow: () -> Unit,
    onAutoSyncToggle: (Boolean) -> Unit,
    onWifiOnlyToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Sync Now
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Sync Now",
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (lastSyncTime > 0) {
                        Text(
                            text = "Last sync: ${formatSyncTime(lastSyncTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = onSyncNow,
                    enabled = isLoggedIn && !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sync")
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Auto Sync Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Auto Sync",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Sync automatically in the background",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = onAutoSyncToggle,
                    enabled = isLoggedIn
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wi-Fi Only Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Wi-Fi Only",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Only sync when connected to Wi-Fi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = syncOnWifiOnly,
                    onCheckedChange = onWifiOnlyToggle,
                    enabled = isLoggedIn && autoSyncEnabled
                )
            }
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Life App",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Push to Start, Pop to Finish",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun formatSyncTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

/**
 * Validates server URL format (domain:port or IP:port)
 */
private fun isValidServerUrl(url: String): Boolean {
    if (url.isBlank()) return true
    
    // Pattern for domain:port or IP:port
    val domainPortPattern = Regex("""^[a-zA-Z0-9]([a-zA-Z0-9\-]*[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9\-]*[a-zA-Z0-9])?)*:\d{1,5}$""")
    val ipPortPattern = Regex("""^(\d{1,3}\.){3}\d{1,3}:\d{1,5}$""")
    
    if (!domainPortPattern.matches(url) && !ipPortPattern.matches(url)) {
        return false
    }
    
    // Validate port range (1-65535)
    val port = url.substringAfterLast(':').toIntOrNull() ?: return false
    return port in 1..65535
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    Android16DemoTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                isLoggedIn = true,
                username = "testuser",
                lastSyncTime = System.currentTimeMillis(),
                autoSyncEnabled = true,
                serverUrl = "api.example.com:8080",
                pushTemplateId = "default"
            ),
            onLogin = { _, _ -> },
            onLogout = {},
            onSyncNow = {},
            onAutoSyncToggle = {},
            onWifiOnlyToggle = {},
            onServerUrlChange = {},
            onPushTemplateChange = {},
            onNavigateBack = {},
            onErrorDismiss = {}
        )
    }
}
