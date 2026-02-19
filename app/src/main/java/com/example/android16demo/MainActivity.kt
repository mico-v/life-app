package com.example.android16demo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.android16demo.ui.screen.PublishScreen
import com.example.android16demo.ui.screen.StatusScreen
import com.example.android16demo.ui.screen.WebProfileScreen
import com.example.android16demo.ui.theme.Android16DemoTheme
import com.example.android16demo.util.LocaleHelper
import com.example.android16demo.viewmodel.PublishViewModel
import com.example.android16demo.viewmodel.StatusViewModel
import com.example.android16demo.viewmodel.WebProfileViewModel

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val app = newBase.applicationContext as? LifeApp
            val languageCode = app?.syncPreferences?.language ?: "system"
            val context = LocaleHelper.applyLanguage(newBase, languageCode)
            super.attachBaseContext(context)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = applicationContext as LifeApp
            val themeMode = app.syncPreferences.themeMode

            Android16DemoTheme(themeMode = themeMode) {
                LifeAppMain()
            }
        }
    }
}

private sealed class WebScreen(val route: String, val titleRes: Int, val icon: ImageVector) {
    data object Status : WebScreen("status", R.string.nav_status, Icons.Filled.Home)
    data object Profile : WebScreen("profile", R.string.nav_profile, Icons.Filled.Person)
    data object Publish : WebScreen("publish", R.string.nav_publish, Icons.Filled.Edit)
}

private val webBottomScreens = listOf(WebScreen.Status, WebScreen.Profile, WebScreen.Publish)

@Composable
fun LifeAppMain(
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val app = LocalContext.current.applicationContext as LifeApp

    val statusViewModel: StatusViewModel = viewModel(factory = webFactory { StatusViewModel(app.webRepository) })
    val profileViewModel: WebProfileViewModel = viewModel(factory = webFactory { WebProfileViewModel(app.webRepository, app.syncPreferences) })
    val publishViewModel: PublishViewModel = viewModel(factory = webFactory { PublishViewModel(app.webRepository, app.syncPreferences) })

    val statusState by statusViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val publishState by publishViewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                webBottomScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleRes)) },
                        label = { Text(stringResource(screen.titleRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WebScreen.Status.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(WebScreen.Status.route) {
                StatusScreen(
                    uiState = statusState,
                    onRefresh = statusViewModel::refresh,
                    onErrorDismiss = statusViewModel::clearError
                )
            }

            composable(WebScreen.Profile.route) {
                WebProfileScreen(
                    uiState = profileState,
                    onDisplayNameChanged = profileViewModel::onDisplayNameChanged,
                    onMottoChanged = profileViewModel::onMottoChanged,
                    onSave = profileViewModel::saveLocalProfile,
                    onRefresh = profileViewModel::refresh,
                    onClearFlags = profileViewModel::clearFlags
                )
            }

            composable(WebScreen.Publish.route) {
                PublishScreen(
                    uiState = publishState,
                    onServerUrlChanged = publishViewModel::onServerUrlChanged,
                    onClientTokenChanged = publishViewModel::onClientTokenChanged,
                    onServerPasswordChanged = publishViewModel::onServerPasswordChanged,
                    onStatusChanged = publishViewModel::onStatusChanged,
                    onTtlChanged = publishViewModel::onTtlChanged,
                    onPostContentChanged = publishViewModel::onPostContentChanged,
                    onPostTagsChanged = publishViewModel::onPostTagsChanged,
                    onPostLocationChanged = publishViewModel::onPostLocationChanged,
                    onPublishStatus = publishViewModel::publishStatus,
                    onPublishPost = publishViewModel::publishPost,
                    onRefreshPosts = publishViewModel::refreshPosts,
                    onDeletePost = publishViewModel::deletePost,
                    onClearMessages = publishViewModel::clearMessages
                )
            }
        }
    }

}

private fun <T : ViewModel> webFactory(builder: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            return builder() as VM
        }
    }
}
