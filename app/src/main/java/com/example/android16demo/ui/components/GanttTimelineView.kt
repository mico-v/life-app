package com.example.android16demo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.android16demo.R
import com.example.android16demo.data.entity.Task
import com.example.android16demo.ui.theme.Android16DemoTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Vertical Gantt Chart style Timeline View
 * Displays tasks on a vertical timeline with time scale on left.
 * - Scroll up: view past/overdue tasks
 * - Scroll down: view future tasks
 * - Pinch to zoom: adjust time scale
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
    
    // Scale factor for pinch-to-zoom (1.0 = default, higher = more zoomed in)
    var scale by remember { mutableFloatStateOf(1f) }
    
    // Base height per hour (scaled)
    val baseHourHeightDp = 60.dp
    val hourHeightDp = baseHourHeightDp * scale.coerceIn(0.5f, 3f)
    
    // Time range: 3 days before to 4 days after = 7 days
    val totalHours = 168 // 7 days
    val daysBeforeNow = 3
    
    // Calculate initial scroll position to center on current time
    val hourHeightPx = with(density) { hourHeightDp.toPx() }
    val hoursFromStart = daysBeforeNow * 24
    
    // Set initial scroll position to current time
    LaunchedEffect(Unit) {
        scrollState.scrollTo((hoursFromStart * hourHeightPx - 200).toInt().coerceAtLeast(0))
    }
    
    // Pinch-to-zoom state
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
    }
    
    Row(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformableState)
    ) {
        // Left side: Time scale
        VerticalTimeScale(
            scrollState = scrollState,
            hourHeightDp = hourHeightDp,
            totalHours = totalHours,
            daysBeforeNow = daysBeforeNow,
            modifier = Modifier.width(70.dp)
        )
        
        // Right side: Tasks
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Background with time grid lines
            VerticalTimelineBackground(
                scrollState = scrollState,
                hourHeightDp = hourHeightDp,
                totalHours = totalHours,
                daysBeforeNow = daysBeforeNow,
                modifier = Modifier.fillMaxSize()
            )
            
            // Task bars
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 8.dp)
            ) {
                // Pre-calculate task positions
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = now
                calendar.add(Calendar.DAY_OF_YEAR, -daysBeforeNow)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startTime = calendar.timeInMillis
                
                // Sort tasks by start time
                val sortedTasks = tasks.sortedBy { it.startTime ?: it.deadline ?: it.createdAt }
                
                sortedTasks.forEach { task ->
                    val taskStart = task.startTime ?: task.createdAt
                    val taskEnd = task.deadline ?: (taskStart + 3600 * 1000L)
                    
                    val startOffsetHours = ((taskStart - startTime) / (3600 * 1000f)).coerceIn(0f, totalHours.toFloat())
                    val durationHours = ((taskEnd - taskStart) / (3600 * 1000f)).coerceAtLeast(1f).coerceAtMost(24f)
                    
                    Spacer(modifier = Modifier.height(hourHeightDp * startOffsetHours))
                    
                    VerticalGanttTaskBar(
                        task = task,
                        heightDp = hourHeightDp * durationHours,
                        onClick = { onTaskClick(task.id) }
                    )
                }
                
                // Bottom spacer
                Spacer(modifier = Modifier.height(hourHeightDp * 24))
            }
        }
    }
}

/**
 * Vertical time scale on the left side
 */
@Composable
private fun VerticalTimeScale(
    scrollState: androidx.compose.foundation.ScrollState,
    hourHeightDp: Dp,
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
    val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .verticalScroll(scrollState)
    ) {
        repeat(totalHours) { hour ->
            val timeMillis = startTime + hour * 3600 * 1000L
            val isStartOfDay = hour % 24 == 0
            val isCurrentHour = System.currentTimeMillis() in timeMillis until (timeMillis + 3600 * 1000L)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hourHeightDp)
                    .background(
                        if (isCurrentHour)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else
                            Color.Transparent
                    )
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
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
 * Background with horizontal grid lines and current time indicator
 */
@Composable
private fun VerticalTimelineBackground(
    scrollState: androidx.compose.foundation.ScrollState,
    hourHeightDp: Dp,
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
    val hourHeightPx = with(density) { hourHeightDp.toPx() }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(hourHeightDp * totalHours)
            .verticalScroll(scrollState)
    ) {
        // Draw horizontal grid lines
        repeat(totalHours + 1) { hour ->
            val y = hour * hourHeightPx
            val isDayStart = hour % 24 == 0
            
            drawLine(
                color = if (isDayStart) gridColor.copy(alpha = 0.5f) else gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = if (isDayStart) 2f else 1f
            )
        }
        
        // Draw current time indicator (horizontal line)
        val currentHourOffset = ((now - startTime) / (3600 * 1000f)) * hourHeightPx
        if (currentHourOffset >= 0 && currentHourOffset <= size.height) {
            // Current time line
            drawLine(
                color = currentTimeColor,
                start = Offset(0f, currentHourOffset),
                end = Offset(size.width, currentHourOffset),
                strokeWidth = 3f
            )
            
            // Current time dot at left
            drawCircle(
                color = currentTimeColor,
                radius = 8f,
                center = Offset(8f, currentHourOffset)
            )
        }
    }
}

/**
 * Task bar for vertical Gantt chart
 */
@Composable
private fun VerticalGanttTaskBar(
    task: Task,
    heightDp: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val taskEnd = task.deadline ?: (task.createdAt + 3600 * 1000L)
    val isOverdue = task.isOverdue()
    val isPast = taskEnd < now
    
    val taskColor = when {
        isOverdue -> MaterialTheme.colorScheme.error
        task.priority == Task.PRIORITY_HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        task.priority == Task.PRIORITY_MEDIUM -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }
    
    val animatedColor by animateColorAsState(
        targetValue = taskColor,
        animationSpec = tween(300),
        label = "task_color"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.coerceAtLeast(48.dp))
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (isPast) animatedColor else Color.White,
                        CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Task title and info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isPast)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Show deadline if exists
                task.deadline?.let { deadline ->
                    val timeFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                    Text(
                        text = timeFormat.format(Date(deadline)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPast)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else
                            Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Overdue warning
            if (isOverdue) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.task_overdue),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
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

@Preview(showBackground = true)
@Composable
fun VerticalGanttTimelineViewPreview() {
    val sampleTasks = listOf(
        Task(
            title = "逾期任务",
            deadline = System.currentTimeMillis() - 3600000,
            priority = Task.PRIORITY_HIGH
        ),
        Task(
            title = "今天的任务",
            startTime = System.currentTimeMillis(),
            deadline = System.currentTimeMillis() + 3600000,
            description = "This is due today"
        ),
        Task(
            title = "明天的长任务",
            startTime = System.currentTimeMillis() + 86400000,
            deadline = System.currentTimeMillis() + 86400000 + 7200000
        ),
        Task(
            title = "未来的任务",
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
