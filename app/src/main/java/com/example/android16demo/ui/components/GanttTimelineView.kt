package com.example.android16demo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.android16demo.data.entity.Task
import com.example.android16demo.ui.theme.Android16DemoTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Gantt Chart style Timeline View
 * Displays tasks on a horizontal timeline with current time centered.
 * - Swipe left: view overdue/past tasks
 * - Swipe right: view future tasks
 */
@Composable
fun GanttTimelineView(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    
    // Calculate initial scroll position to center on current time
    val hourWidthDp = 80.dp
    val hourWidthPx = with(density) { hourWidthDp.toPx() }
    
    // Calculate total width (7 days = 168 hours)
    val totalHours = 168 // 7 days
    val totalWidthDp = hourWidthDp * totalHours
    
    // Get current hour offset from start (3 days back)
    val daysBeforeNow = 3
    val hoursFromStart = daysBeforeNow * 24
    
    // Set initial scroll position to current time
    LaunchedEffect(Unit) {
        scrollState.scrollTo((hoursFromStart * hourWidthPx - 200).toInt().coerceAtLeast(0))
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Time header
        TimelineHeader(
            scrollState = scrollState,
            hourWidthDp = hourWidthDp,
            totalHours = totalHours,
            daysBeforeNow = daysBeforeNow
        )
        
        // Task rows
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrollable content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
            ) {
                // Background grid with current time indicator
                TimelineBackground(
                    hourWidthDp = hourWidthDp,
                    totalHours = totalHours,
                    daysBeforeNow = daysBeforeNow,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Task items overlaid
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = tasks.sortedBy { it.deadline ?: it.startTime ?: it.createdAt },
                    key = { it.id }
                ) { task ->
                    GanttTaskBar(
                        task = task,
                        hourWidthDp = hourWidthDp,
                        daysBeforeNow = daysBeforeNow,
                        onClick = { onTaskClick(task.id) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

/**
 * Timeline header showing hours and dates
 */
@Composable
private fun TimelineHeader(
    scrollState: androidx.compose.foundation.ScrollState,
    hourWidthDp: Dp,
    totalHours: Int,
    daysBeforeNow: Int,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = now
    calendar.add(Calendar.DAY_OF_YEAR, -daysBeforeNow)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val startTime = calendar.timeInMillis
    
    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    val hourFormat = SimpleDateFormat("HH:00", Locale.getDefault())
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .horizontalScroll(scrollState)
    ) {
        repeat(totalHours) { hour ->
            val timeMillis = startTime + hour * 3600 * 1000L
            val isStartOfDay = hour % 24 == 0
            val isCurrentHour = System.currentTimeMillis() in timeMillis until (timeMillis + 3600 * 1000L)
            
            Box(
                modifier = Modifier
                    .width(hourWidthDp)
                    .fillMaxHeight()
                    .background(
                        if (isCurrentHour) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else 
                            Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isStartOfDay) {
                        Text(
                            text = dateFormat.format(Date(timeMillis)),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = hourFormat.format(Date(timeMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrentHour) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Background grid with current time indicator
 */
@Composable
private fun TimelineBackground(
    hourWidthDp: Dp,
    totalHours: Int,
    daysBeforeNow: Int,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = now
    calendar.add(Calendar.DAY_OF_YEAR, -daysBeforeNow)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val startTime = calendar.timeInMillis
    
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val currentTimeColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val hourWidthPx = with(density) { hourWidthDp.toPx() }
    
    Canvas(
        modifier = modifier
            .width(hourWidthDp * totalHours)
            .fillMaxHeight()
    ) {
        // Draw vertical grid lines
        repeat(totalHours + 1) { hour ->
            val x = hour * hourWidthPx
            val isDayStart = hour % 24 == 0
            
            drawLine(
                color = if (isDayStart) gridColor.copy(alpha = 0.5f) else gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = if (isDayStart) 2f else 1f
            )
        }
        
        // Draw current time indicator
        val currentHourOffset = ((now - startTime) / (3600 * 1000f)) * hourWidthPx
        if (currentHourOffset >= 0 && currentHourOffset <= size.width) {
            // Current time line
            drawLine(
                color = currentTimeColor,
                start = Offset(currentHourOffset, 0f),
                end = Offset(currentHourOffset, size.height),
                strokeWidth = 3f
            )
            
            // Current time dot at top
            drawCircle(
                color = currentTimeColor,
                radius = 8f,
                center = Offset(currentHourOffset, 8f)
            )
        }
    }
}

/**
 * Individual task bar in the Gantt chart
 */
@Composable
private fun GanttTaskBar(
    task: Task,
    hourWidthDp: Dp,
    daysBeforeNow: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = now
    calendar.add(Calendar.DAY_OF_YEAR, -daysBeforeNow)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val startTime = calendar.timeInMillis
    
    // Calculate task position and width
    val taskStart = task.startTime ?: task.createdAt
    val taskEnd = task.deadline ?: (taskStart + 3600 * 1000L) // Default 1 hour duration
    
    val startOffsetHours = ((taskStart - startTime) / (3600 * 1000f)).coerceAtLeast(0f)
    val durationHours = ((taskEnd - taskStart) / (3600 * 1000f)).coerceAtLeast(1f).coerceAtMost(48f)
    
    val isOverdue = task.isOverdue()
    val isPast = taskEnd < now
    
    val taskColor = when {
        isOverdue -> MaterialTheme.colorScheme.error
        task.priority == Task.PRIORITY_HIGH -> MaterialTheme.colorScheme.tertiary
        task.priority == Task.PRIORITY_MEDIUM -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }
    
    val animatedColor by animateColorAsState(
        targetValue = taskColor,
        animationSpec = tween(300),
        label = "task_color"
    )
    
    Box(
        modifier = modifier
            .padding(start = hourWidthDp * startOffsetHours)
            .width(hourWidthDp * durationHours)
            .height(48.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isPast) 
                    animatedColor.copy(alpha = 0.3f) 
                else 
                    animatedColor.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isPast) animatedColor else Color.White,
                            CircleShape
                        )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Task title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isPast) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else 
                        Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Overdue warning
                if (isOverdue) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Overdue",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Progress indicator
                if (task.progress > 0 && task.progress < 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(task.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Simple vertical timeline view as alternative
 */
@Composable
fun SimpleGanttView(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val hourWidthDp = 60.dp
    val totalHours = 168 // 7 days
    val daysBeforeNow = 3
    val hourWidthPx = with(density) { hourWidthDp.toPx() }
    
    // Center on current time initially
    LaunchedEffect(Unit) {
        val hoursFromStart = daysBeforeNow * 24
        scrollState.scrollTo((hoursFromStart * hourWidthPx - 200).toInt().coerceAtLeast(0))
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Compact time header
        CompactTimeHeader(
            scrollState = scrollState,
            hourWidthDp = hourWidthDp,
            totalHours = totalHours,
            daysBeforeNow = daysBeforeNow
        )
        
        // Task list with horizontal time axis
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = tasks.sortedBy { it.deadline ?: it.startTime ?: it.createdAt },
                key = { it.id }
            ) { task ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Task info column
                    TaskInfoColumn(
                        task = task,
                        onClick = { onTaskClick(task.id) },
                        modifier = Modifier.width(120.dp)
                    )
                    
                    // Timeline bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(scrollState)
                    ) {
                        GanttTaskBar(
                            task = task,
                            hourWidthDp = hourWidthDp,
                            daysBeforeNow = daysBeforeNow,
                            onClick = { onTaskClick(task.id) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CompactTimeHeader(
    scrollState: androidx.compose.foundation.ScrollState,
    hourWidthDp: Dp,
    totalHours: Int,
    daysBeforeNow: Int,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = now
    calendar.add(Calendar.DAY_OF_YEAR, -daysBeforeNow)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val startTime = calendar.timeInMillis
    
    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    
    Row(modifier = modifier.fillMaxWidth()) {
        // Empty space for task info column
        Spacer(modifier = Modifier.width(120.dp))
        
        // Scrollable time header
        Row(
            modifier = Modifier
                .weight(1f)
                .height(30.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .horizontalScroll(scrollState)
        ) {
            repeat(totalHours / 6) { index ->
                val hour = index * 6
                val timeMillis = startTime + hour * 3600 * 1000L
                val isToday = Calendar.getInstance().apply { timeInMillis = timeMillis }
                    .get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                
                Box(
                    modifier = Modifier
                        .width(hourWidthDp * 6)
                        .fillMaxHeight()
                        .background(
                            if (isToday)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (hour % 24 == 0) dateFormat.format(Date(timeMillis)) else "${hour % 24}:00",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskInfoColumn(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = task.isOverdue()
    val priorityColor = when (task.priority) {
        Task.PRIORITY_HIGH -> MaterialTheme.colorScheme.error
        Task.PRIORITY_MEDIUM -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Card(
        modifier = modifier
            .height(56.dp)
            .padding(4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(priorityColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GanttTimelineViewPreview() {
    val sampleTasks = listOf(
        Task(
            title = "Overdue Task",
            deadline = System.currentTimeMillis() - 3600000,
            priority = Task.PRIORITY_HIGH
        ),
        Task(
            title = "Today's Task",
            startTime = System.currentTimeMillis(),
            deadline = System.currentTimeMillis() + 3600000,
            description = "This is due today"
        ),
        Task(
            title = "Tomorrow's Long Task",
            startTime = System.currentTimeMillis() + 86400000,
            deadline = System.currentTimeMillis() + 86400000 + 7200000
        ),
        Task(
            title = "Future Task",
            deadline = System.currentTimeMillis() + 604800000 / 2,
            priority = Task.PRIORITY_LOW
        )
    )
    
    Android16DemoTheme {
        SimpleGanttView(
            tasks = sampleTasks,
            onTaskClick = {},
            onTaskComplete = {},
            onTaskDelete = {}
        )
    }
}
