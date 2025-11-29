package com.example.android16demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android16demo.data.entity.Task
import com.example.android16demo.ui.theme.Android16DemoTheme
import com.example.android16demo.viewmodel.TaskDetailUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Task Detail Screen for creating and editing tasks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    isEditMode: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartTimeChange: (Long?) -> Unit,
    onDeadlineChange: (Long?) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onProgressChange: (Float) -> Unit,
    onIsPublicChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
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
    
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isEditMode) "Edit Task" else "Push New Task") 
                },
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
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    label = { Text("Title *") },
                    placeholder = { Text("What needs to be done?") },
                    isError = uiState.titleError != null,
                    supportingText = uiState.titleError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Description
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    placeholder = { Text("Add details...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                // Deadline picker
                DateTimePickerField(
                    label = "Deadline",
                    selectedTimestamp = uiState.deadline,
                    onTimestampSelected = onDeadlineChange,
                    icon = Icons.Default.CalendarMonth
                )
                
                // Start time picker
                DateTimePickerField(
                    label = "Start Time",
                    selectedTimestamp = uiState.startTime,
                    onTimestampSelected = onStartTimeChange,
                    icon = Icons.Default.Schedule
                )
                
                // Priority
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelLarge
                )
                PrioritySelector(
                    selectedPriority = uiState.priority,
                    onPrioritySelected = onPriorityChange
                )
                
                // Progress (only for edit mode)
                if (isEditMode) {
                    Text(
                        text = "Progress: ${(uiState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = uiState.progress,
                        onValueChange = onProgressChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Public toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Public Task",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "Show on your public status page",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isPublic,
                        onCheckedChange = onIsPublicChange
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save button
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text(if (isEditMode) "Update Task" else "Push Task")
                }
            }
        }
    }
}

/**
 * Priority selector with radio buttons
 */
@Composable
private fun PrioritySelector(
    selectedPriority: Int,
    onPrioritySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val priorities = listOf(
        Task.PRIORITY_LOW to "Low",
        Task.PRIORITY_MEDIUM to "Medium",
        Task.PRIORITY_HIGH to "High"
    )
    
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        priorities.forEach { (priority, label) ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = selectedPriority == priority,
                        onClick = { onPrioritySelected(priority) },
                        role = Role.RadioButton
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedPriority == priority,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * Date/Time picker field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerField(
    label: String,
    selectedTimestamp: Long?,
    onTimestampSelected: (Long?) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = selectedTimestamp?.let { dateFormat.format(Date(it)) } ?: label
            )
        }
        
        if (selectedTimestamp != null) {
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { onTimestampSelected(null) }) {
                Text("Clear")
            }
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedTimestamp ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Add current time to the selected date
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis
                            calendar.set(Calendar.HOUR_OF_DAY, 23)
                            calendar.set(Calendar.MINUTE, 59)
                            onTimestampSelected(calendar.timeInMillis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDetailScreenPreview() {
    Android16DemoTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(),
            isEditMode = false,
            onTitleChange = {},
            onDescriptionChange = {},
            onStartTimeChange = {},
            onDeadlineChange = {},
            onPriorityChange = {},
            onProgressChange = {},
            onIsPublicChange = {},
            onSave = {},
            onNavigateBack = {},
            onErrorDismiss = {}
        )
    }
}
