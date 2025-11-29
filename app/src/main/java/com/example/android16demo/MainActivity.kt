package com.example.android16demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.example.android16demo.ui.screen.TaskDetailScreen
import com.example.android16demo.ui.screen.TaskQueueScreen
import com.example.android16demo.ui.screen.TimelineScreen
import com.example.android16demo.ui.theme.Android16DemoTheme
import com.example.android16demo.viewmodel.ArchiveViewModel
import com.example.android16demo.viewmodel.HomeViewModel
import com.example.android16demo.viewmodel.TaskDetailViewModel
import com.example.android16demo.viewmodel.ViewModelFactory
import com.example.android16demo.worker.DailySummaryWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Schedule daily summary notifications
        DailySummaryWorker.scheduleDailySummary(this)
        
        setContent {
            Android16DemoTheme {
                LifeAppMain()
            }
        }
    }
}

/**
 * Screen routes for navigation
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Queue : Screen("queue", "Queue", Icons.Filled.FormatListBulleted)
    data object Archive : Screen("archive", "Archive", Icons.Filled.Archive)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    data object TaskDetail : Screen("task/{taskId}", "Task") {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeAppMain() {
    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // View mode toggle state
    var viewMode by rememberSaveable { mutableStateOf(ViewMode.LIST) }
    
    // Check if current screen should show bottom nav
    val showBottomNav = bottomNavScreens.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }
    
    // Check if on Queue screen (to show view toggle)
    val isQueueScreen = currentDestination?.hierarchy?.any { it.route == Screen.Queue.route } == true
    
    // Get app instance for repository access
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as LifeApp
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (showBottomNav) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Life App",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        if (isQueueScreen) {
                            IconButton(
                                onClick = {
                                    viewMode = if (viewMode == ViewMode.LIST) ViewMode.TIMELINE else ViewMode.LIST
                                }
                            ) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.LIST) Icons.Filled.Timeline else Icons.Filled.FormatListBulleted,
                                    contentDescription = if (viewMode == ViewMode.LIST) "Timeline view" else "List view",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                screen.icon?.let { Icon(it, contentDescription = screen.title) }
                            },
                            label = { Text(screen.title) },
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
            modifier = Modifier.padding(innerPadding)
        ) {
            // Queue (Home) Screen - supports list and timeline views
            composable(Screen.Queue.route) {
                val viewModel: HomeViewModel = viewModel(
                    factory = ViewModelFactory(app.taskRepository)
                )
                val uiState by viewModel.uiState.collectAsState()
                
                when (viewMode) {
                    ViewMode.LIST -> {
                        TaskQueueScreen(
                            uiState = uiState,
                            onTaskClick = { taskId ->
                                navController.navigate(Screen.TaskDetail.createRoute(taskId))
                            },
                            onTaskComplete = { taskId ->
                                viewModel.popTask(taskId)
                            },
                            onTaskDelete = { taskId ->
                                viewModel.deleteTask(taskId)
                            },
                            onAddTask = {
                                navController.navigate(Screen.TaskDetail.createRoute(null))
                            },
                            onErrorDismiss = { viewModel.clearError() }
                        )
                    }
                    ViewMode.TIMELINE -> {
                        TimelineScreen(
                            uiState = uiState,
                            onTaskClick = { taskId ->
                                navController.navigate(Screen.TaskDetail.createRoute(taskId))
                            },
                            onTaskComplete = { taskId ->
                                viewModel.popTask(taskId)
                            },
                            onTaskDelete = { taskId ->
                                viewModel.deleteTask(taskId)
                            },
                            onAddTask = {
                                navController.navigate(Screen.TaskDetail.createRoute(null))
                            },
                            onErrorDismiss = { viewModel.clearError() }
                        )
                    }
                }
            }
            
            // Archive Screen
            composable(Screen.Archive.route) {
                val viewModel: ArchiveViewModel = viewModel(
                    factory = ViewModelFactory(app.taskRepository)
                )
                val uiState by viewModel.uiState.collectAsState()
                
                ArchiveScreen(
                    uiState = uiState,
                    onDeleteTask = { taskId ->
                        viewModel.deleteTask(taskId)
                    },
                    onClearAll = { viewModel.clearAllArchived() },
                    onErrorDismiss = { viewModel.clearError() }
                )
            }
            
            // Profile Screen (placeholder for Phase 4)
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            
            // Task Detail Screen (Create/Edit)
            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(
                    navArgument("taskId") { 
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.takeIf { it != "new" }
                val viewModel: TaskDetailViewModel = viewModel(
                    factory = ViewModelFactory(app.taskRepository, taskId)
                )
                val uiState by viewModel.uiState.collectAsState()
                
                TaskDetailScreen(
                    uiState = uiState,
                    isEditMode = viewModel.isEditMode,
                    onTitleChange = { viewModel.updateTitle(it) },
                    onDescriptionChange = { viewModel.updateDescription(it) },
                    onStartTimeChange = { viewModel.updateStartTime(it) },
                    onDeadlineChange = { viewModel.updateDeadline(it) },
                    onPriorityChange = { viewModel.updatePriority(it) },
                    onProgressChange = { viewModel.updateProgress(it) },
                    onIsPublicChange = { viewModel.updateIsPublic(it) },
                    onSave = { viewModel.saveTask() },
                    onNavigateBack = { navController.popBackStack() },
                    onErrorDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

/**
 * Profile Screen placeholder
 */
@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Coming in Phase 4",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
