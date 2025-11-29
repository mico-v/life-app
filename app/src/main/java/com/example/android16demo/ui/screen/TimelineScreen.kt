package com.example.android16demo.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android16demo.data.entity.Task
import com.example.android16demo.ui.components.TimelineView
import com.example.android16demo.ui.theme.Android16DemoTheme
import com.example.android16demo.viewmodel.HomeUiState

/**
 * Timeline Screen - Visual representation of tasks on a timeline
 * Shows tasks grouped by time section (Overdue, Today, Tomorrow, etc.)
 */
@Composable
fun TimelineScreen(
    uiState: HomeUiState,
    onTaskClick: (String) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onAddTask: () -> Unit,
    onErrorDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onErrorDismiss()
        }
    }
    
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Push new task"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.activeTasks.isEmpty() -> {
                    EmptyTimelineMessage(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    TimelineView(
                        tasks = uiState.activeTasks,
                        onTaskClick = onTaskClick,
                        onTaskComplete = onTaskComplete,
                        onTaskDelete = onTaskDelete
                    )
                }
            }
        }
    }
}

/**
 * Empty state message for timeline
 */
@Composable
private fun EmptyTimelineMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Timeline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Timeline is Empty",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Push your first task to see your timeline!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimelineScreenPreview() {
    val sampleTasks = listOf(
        Task(
            title = "Overdue Task",
            deadline = System.currentTimeMillis() - 3600000,
            priority = Task.PRIORITY_HIGH
        ),
        Task(
            title = "Today's Task",
            deadline = System.currentTimeMillis() + 3600000,
            description = "This is due today"
        ),
        Task(
            title = "Tomorrow's Task",
            deadline = System.currentTimeMillis() + 86400000
        )
    )
    
    Android16DemoTheme {
        TimelineScreen(
            uiState = HomeUiState(
                activeTasks = sampleTasks,
                isLoading = false
            ),
            onTaskClick = {},
            onTaskComplete = {},
            onTaskDelete = {},
            onAddTask = {},
            onErrorDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimelineScreenEmptyPreview() {
    Android16DemoTheme {
        TimelineScreen(
            uiState = HomeUiState(
                activeTasks = emptyList(),
                isLoading = false
            ),
            onTaskClick = {},
            onTaskComplete = {},
            onTaskDelete = {},
            onAddTask = {},
            onErrorDismiss = {}
        )
    }
}
