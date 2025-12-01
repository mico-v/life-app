package com.example.android16demo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * - Time scale on left side
 * - Tasks on right side
 * - Scroll up/down to view past/future tasks
 * - Pinch to zoom the time scale
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
    
    // Scale factor for pinch-to-zoom
    var scale by remember { mutableFloatStateOf(1f) }
    
    // Base height per hour
    val baseHourHeightDp = 60.dp
    val hourHeightDp = baseHourHeightDp * scale.coerceIn(0.5f, 3f)
    
    // Time range: 3 days before to 4 days after
    val totalHours = 168
    val daysBeforeNow = 3
    val totalHeightDp = hourHeightDp * totalHours
    
    // Initial scroll position
    val hourHeightPx = with(density) { hourHeightDp.toPx() }
    val hoursFromStart = daysBeforeNow * 24
    
    LaunchedEffect(Unit) {
        scrollState.scrollTo((hoursFromStart * hourHeightPx - 200).toInt().coerceAtLeast(0))
    }
    
    // Pinch-to-zoom
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
    }
    
    // Calculate time boundaries
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = now
    calendar.add(Calendar.DAY_OF_YEAR, -daysBeforeNow)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val startTime = calendar.timeInMillis
    
    Row(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformableState)
    ) {
        // Left: Time scale
        Column(
            modifier = Modifier
                .width(70.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .verticalScroll(scrollState)
        ) {
            val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
            val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            repeat(totalHours) { hour ->
                val timeMillis = startTime + hour * 3600 * 1000L
                val isStartOfDay = hour % 24 == 0
                val isCurrentHour = now in timeMillis until (timeMillis + 3600 * 1000L)
                
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
        
        // Right: Tasks area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(scrollState)
        ) {
            // Background grid
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            val currentTimeColor = MaterialTheme.colorScheme.primary
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeightDp)
            ) {
                val hourHeightPxCanvas = size.height / totalHours
                
                // Draw horizontal grid lines
                repeat(totalHours + 1) { hour ->
                    val y = hour * hourHeightPxCanvas
                    val isDayStart = hour % 24 == 0
                    
                    drawLine(
                        color = if (isDayStart) gridColor.copy(alpha = 0.5f) else gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = if (isDayStart) 2f else 1f
                    )
                }
                
                // Draw current time indicator
                val currentHourOffset = ((now - startTime) / (3600 * 1000f)) * hourHeightPxCanvas
                if (currentHourOffset >= 0 && currentHourOffset <= size.height) {
                    drawLine(
                        color = currentTimeColor,
                        start = Offset(0f, currentHourOffset),
                        end = Offset(size.width, currentHourOffset),
                        strokeWidth = 3f
                    )
                    drawCircle(
                        color = currentTimeColor,
                        radius = 8f,
                        center = Offset(8f, currentHourOffset)
                    )
                }
            }
            
            // Task bars using absolute positioning
            tasks.sortedBy { it.startTime ?: it.deadline ?: it.createdAt }.forEach { task ->
                val taskStart = task.startTime ?: task.createdAt
                val taskEnd = task.deadline ?: (taskStart + 3600 * 1000L)
                
                val startOffsetHours = ((taskStart - startTime) / (3600 * 1000f)).coerceIn(0f, totalHours.toFloat())
                val durationHours = ((taskEnd - taskStart) / (3600 * 1000f)).coerceAtLeast(1f).coerceAtMost(24f)
                
                val topOffset = hourHeightDp * startOffsetHours
                val taskHeight = (hourHeightDp * durationHours).coerceAtLeast(48.dp)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .offset(y = topOffset)
                ) {
                    VerticalGanttTaskBar(
                        task = task,
                        heightDp = taskHeight,
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }
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
            .height(heightDp)
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
            
            // Task info
            Column(modifier = Modifier.weight(1f)) {
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
            
            // Progress
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
