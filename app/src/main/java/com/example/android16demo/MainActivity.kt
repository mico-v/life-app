package com.example.android16demo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android16demo.ui.screen.ArchiveScreen
import com.example.android16demo.ui.screen.ProfileScreenContent
import com.example.android16demo.ui.screen.SettingsScreen
import com.example.android16demo.ui.screen.TaskDetailScreen
import com.example.android16demo.ui.screen.TaskQueueScreen
import com.example.android16demo.ui.screen.TimelineScreen
import com.example.android16demo.ui.theme.Android16DemoTheme
import com.example.android16demo.util.LocaleHelper
import com.example.android16demo.viewmodel.ArchiveViewModel
import com.example.android16demo.viewmodel.HomeViewModel
import com.example.android16demo.viewmodel.ProfileViewModel
import com.example.android16demo.viewmodel.SettingsViewModel
import com.example.android16demo.viewmodel.TaskDetailViewModel
import com.example.android16demo.viewmodel.ViewModelFactory
import com.example.android16demo.worker.DailySummaryWorker

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
        
        // Schedule daily summary notifications
        DailySummaryWorker.scheduleDailySummary(this)
        
        setContent {
            val app = applicationContext as LifeApp
            // Observe theme mode from preferences
            var themeMode by remember { mutableStateOf(app.syncPreferences.themeMode) }
            
            Android16DemoTheme(themeMode = themeMode) {
                LifeAppMain(
                    onThemeChanged = { newMode ->
                        themeMode = newMode
                    },
                    onLanguageChanged = {
                        // Recreate activity to apply new language
                        recreate()
                    }
                )
            }
        }
    }
}

/**
 * Screen routes for navigation
 */
sealed class Screen(val route: String, val titleResId: Int, val icon: ImageVector? = null) {
    data object Queue : Screen("queue", R.string.nav_queue, Icons.AutoMirrored.Filled.FormatListBulleted)
    data object Archive : Screen("archive", R.string.nav_archive, Icons.Filled.Archive)
    data object Profile : Screen("profile", R.string.nav_profile, Icons.Filled.Person)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)
    data object TaskDetail : Screen("task/{taskId}", R.string.title_push_new_task) {
        fun createRoute(taskId: String?) = if (taskId != null) "task/$taskId" else "task/new"
    }
}

val bottomNavScreens = listOf(Screen.Queue, Screen.Archive, Screen.Profile)

/**
 * View mode for task display
 */
enum class ViewMode {
    LIST,
    TIMELINE
}

// Animation duration constant
private const val ANIMATION_DURATION = 300

@Composable
fun LifeAppMain(
    onThemeChanged: (String) -> Unit = {},
    onLanguageChanged: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // View mode toggle state
    var viewMode by rememberSaveable { mutableStateOf(ViewMode.LIST) }
    
    // Get app instance for repository access
    val app = LocalContext.current.applicationContext as LifeApp
    
    // Check if current screen should show bottom nav
    val showBottomNav = bottomNavScreens.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                screen.icon?.let { 
                                    Icon(it, contentDescription = stringResource(screen.titleResId)) 
                                }
                            },
                            label = { Text(stringResource(screen.titleResId)) },
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Queue.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            // Queue (Home) Screen - supports list and timeline views
            composable(
                route = Screen.Queue.route,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                }
            ) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = ViewModelFactory(app.taskRepository)
                )
                val uiState by homeViewModel.uiState.collectAsState()
                
                when (viewMode) {
                    ViewMode.LIST -> {
                        TaskQueueScreen(
                            uiState = uiState,
                            onTaskClick = { taskId ->
                                navController.navigate(Screen.TaskDetail.createRoute(taskId))
                            },
                            onTaskComplete = { taskId ->
                                homeViewModel.popTask(taskId)
                            },
                            onTaskDelete = { taskId ->
                                homeViewModel.deleteTask(taskId)
                            },
                            onAddTask = {
                                navController.navigate(Screen.TaskDetail.createRoute(null))
                            },
                            onToggleView = { viewMode = ViewMode.TIMELINE },
                            onErrorDismiss = { homeViewModel.clearError() }
                        )
                    }
                    ViewMode.TIMELINE -> {
                        TimelineScreen(
                            uiState = uiState,
                            onTaskClick = { taskId ->
                                navController.navigate(Screen.TaskDetail.createRoute(taskId))
                            },
                            onTaskComplete = { taskId ->
                                homeViewModel.popTask(taskId)
                            },
                            onTaskDelete = { taskId ->
                                homeViewModel.deleteTask(taskId)
                            },
                            onAddTask = {
                                navController.navigate(Screen.TaskDetail.createRoute(null))
                            },
                            onToggleView = { viewMode = ViewMode.LIST },
                            onErrorDismiss = { homeViewModel.clearError() }
                        )
                    }
                }
            }
            
            // Archive Screen
            composable(
                route = Screen.Archive.route,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                }
            ) {
                val archiveViewModel: ArchiveViewModel = viewModel(
                    factory = ViewModelFactory(app.taskRepository)
                )
                val uiState by archiveViewModel.uiState.collectAsState()
                
                ArchiveScreen(
                    uiState = uiState,
                    onDeleteTask = { taskId ->
                        archiveViewModel.deleteTask(taskId)
                    },
                    onClearAll = { archiveViewModel.clearAllArchived() },
                    onSearchQueryChange = { archiveViewModel.updateSearchQuery(it) },
                    onTagFilterChange = { archiveViewModel.updateSelectedTag(it) },
                    onErrorDismiss = { archiveViewModel.clearError() }
                )
            }
            
            // Profile Screen
            composable(
                route = Screen.Profile.route,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                }
            ) {
                val viewModel: ProfileViewModel = viewModel(
                    factory = ViewModelFactory(
                        repository = app.taskRepository,
                        syncPreferences = app.syncPreferences
                    )
                )
                val uiState by viewModel.uiState.collectAsState()
                
                ProfileScreenContent(
                    uiState = uiState,
                    onRefresh = { viewModel.refresh() },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onUpdateDisplayName = { viewModel.updateDisplayName(it) },
                    onUpdateMotto = { viewModel.updateMotto(it) },
                    onUpdateStatus = { viewModel.updateStatus(it) },
                    onErrorDismiss = { viewModel.clearError() }
                )
            }
            
            // Settings Screen
            composable(
                route = Screen.Settings.route,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                }
            ) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = ViewModelFactory(
                        repository = app.taskRepository,
                        syncPreferences = app.syncPreferences,
                        templateRepository = app.templateRepository
                    )
                )
                val uiState by settingsViewModel.uiState.collectAsState()
                
                SettingsScreen(
                    uiState = uiState,
                    onLogin = { username, password -> settingsViewModel.login(username, password) },
                    onLogout = { settingsViewModel.logout() },
                    onSyncNow = { settingsViewModel.syncNow() },
                    onAutoSyncToggle = { settingsViewModel.setAutoSync(it) },
                    onWifiOnlyToggle = { settingsViewModel.setWifiOnly(it) },
                    onServerUrlChange = { settingsViewModel.updateServerUrl(it) },
                    onServerPasswordChange = { settingsViewModel.updateServerPassword(it) },
                    onPushTemplateChange = { settingsViewModel.updatePushTemplate(it) },
                    onThemeModeChange = { mode ->
                        settingsViewModel.updateThemeMode(mode)
                        onThemeChanged(mode)
                    },
                    onLanguageChange = { language ->
                        settingsViewModel.updateLanguage(language)
                        onLanguageChanged()
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onErrorDismiss = { settingsViewModel.clearMessages() }
                )
            }
            
            // Task Detail Screen (Create/Edit)
            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(
                    navArgument("taskId") { 
                        type = NavType.StringType
                        nullable = true
                    }
                ),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(ANIMATION_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
                }
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.takeIf { it != "new" }
                val taskDetailViewModel: TaskDetailViewModel = viewModel(
                    factory = ViewModelFactory(app.taskRepository, taskId)
                )
                val uiState by taskDetailViewModel.uiState.collectAsState()
                
                TaskDetailScreen(
                    uiState = uiState,
                    isEditMode = taskDetailViewModel.isEditMode,
                    onTitleChange = { taskDetailViewModel.updateTitle(it) },
                    onDescriptionChange = { taskDetailViewModel.updateDescription(it) },
                    onStartTimeChange = { taskDetailViewModel.updateStartTime(it) },
                    onDeadlineChange = { taskDetailViewModel.updateDeadline(it) },
                    onPriorityChange = { taskDetailViewModel.updatePriority(it) },
                    onProgressChange = { taskDetailViewModel.updateProgress(it) },
                    onIsPublicChange = { taskDetailViewModel.updateIsPublic(it) },
                    onAddTag = { taskDetailViewModel.addTag(it) },
                    onRemoveTag = { taskDetailViewModel.removeTag(it) },
                    onSave = { taskDetailViewModel.saveTask() },
                    onNavigateBack = { navController.popBackStack() },
                    onErrorDismiss = { taskDetailViewModel.clearError() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Android16DemoTheme {
        // Preview with mock data
    }
}
