package com.example.android16demo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.android16demo.data.entity.Task
import com.example.android16demo.ui.theme.Android16DemoTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Daily Calendar View - A proper calendar-style view for tasks
 * 
 * Features:
 * - Dynamic time range based on task times (with 1 hour buffer)
 * - Task height strictly based on duration (1 minute = 1dp at scale 1.0)
 * - Task position based on start time
 * - Overlapping tasks displayed side by side
 * - Pinch-to-zoom for time scale
 */
@Composable
fun DailyCalendarView(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    
    // Scale factor for pinch-to-zoom (1.0 = default)
    var scale by remember { mutableFloatStateOf(1f) }
    
    // Base height per hour: 60dp (so 1 minute = 1dp)
    val baseHourHeightDp = 60.dp
    val hourHeightDp = baseHourHeightDp * scale.coerceIn(0.5f, 3f)
    
    // Get start of today
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis
    val todayEnd = todayStart + 24 * 60 * 60 * 1000L
    val now = System.currentTimeMillis()
    
    // Filter tasks for today
    val todayTasks = tasks.filter { task ->
        val taskStart = task.startTime ?: task.createdAt
        val taskEnd = task.deadline ?: (taskStart + 60 * 60 * 1000L)
        // Include tasks that overlap with today
        taskStart < todayEnd && taskEnd > todayStart
    }
    
    // Calculate dynamic time range based on tasks
    val (displayStartHour, displayEndHour) = calculateDynamicRange(todayTasks, currentHour, todayStart)
    val displayHours = displayEndHour - displayStartHour
    val totalHeightDp = hourHeightDp * displayHours
    
    // Calculate initial scroll position to show current time
    val hourHeightPx = with(density) { hourHeightDp.toPx() }
    val currentTimeOffset = if (currentHour in displayStartHour until displayEndHour) {
        ((currentHour - displayStartHour) + currentMinute / 60f) * hourHeightPx
    } else {
        0f
    }
    
    // Scroll to show current time with some context
    LaunchedEffect(Unit) {
        val targetScroll = (currentTimeOffset - 100f).coerceAtLeast(0f).toInt()
        scrollState.scrollTo(targetScroll)
    }
    
    // Pinch-to-zoom
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
    }
    
    // Calculate overlapping groups for concurrent tasks
    val taskGroups = calculateOverlappingGroups(todayTasks, todayStart)
    
    Row(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformableState)
    ) {
        // Left: Time scale
        Column(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .verticalScroll(scrollState)
        ) {
            repeat(displayHours) { index ->
                val hour = displayStartHour + index
                TimeSlotLabel(
                    hour = hour,
                    height = hourHeightDp,
                    isCurrentHour = hour == currentHour
                )
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
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            val currentTimeColor = MaterialTheme.colorScheme.error
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeightDp)
            ) {
                val hourHeightPxCanvas = size.height / displayHours
                
                // Draw horizontal lines for each hour
                repeat(displayHours + 1) { index ->
                    val y = index * hourHeightPxCanvas
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }
                
                // Draw current time indicator
                if (currentHour in displayStartHour until displayEndHour) {
                    val currentY = ((currentHour - displayStartHour) + currentMinute / 60f) * hourHeightPxCanvas
                    
                    // Current time line (red)
                    drawLine(
                        color = currentTimeColor,
                        start = Offset(0f, currentY),
                        end = Offset(size.width, currentY),
                        strokeWidth = 2f
                    )
                    // Current time circle
                    drawCircle(
                        color = currentTimeColor,
                        radius = 6f,
                        center = Offset(6f, currentY)
                    )
                }
            }
            
            // Render tasks with proper positioning and overlap handling
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val containerWidth = maxWidth
                val displayStartMillis = todayStart + displayStartHour * 60 * 60 * 1000L
                
                taskGroups.forEach { group ->
                    val columnCount = group.tasks.size
                    group.tasks.forEachIndexed { index, taskWithPosition ->
                        val task = taskWithPosition.task
                        
                        // Calculate task position
                        val taskStart = (task.startTime ?: task.createdAt).coerceAtLeast(todayStart)
                        val taskEnd = (task.deadline ?: (taskStart + 60 * 60 * 1000L)).coerceAtMost(todayEnd)
                        
                        // Convert to minutes from display start
                        val startMinutesFromDisplayStart = ((taskStart - displayStartMillis) / (60 * 1000f))
                        val endMinutesFromDisplayStart = ((taskEnd - displayStartMillis) / (60 * 1000f))
                        val durationMinutes = (endMinutesFromDisplayStart - startMinutesFromDisplayStart)
                        
                        // Key fix: Height is strictly based on duration
                        // 60 minutes = hourHeightDp, so 1 minute = hourHeightDp / 60
                        val topOffsetDp = hourHeightDp * (startMinutesFromDisplayStart / 60f)
                        
                        // Minimum height of 2dp for visibility, but otherwise strictly based on duration
                        val minHeightDp = 2.dp
                        val calculatedHeight = hourHeightDp * (durationMinutes / 60f)
                        val heightDp = calculatedHeight.coerceAtLeast(minHeightDp)
                        
                        // Width and position based on how many overlapping tasks
                        val taskWidth = containerWidth / columnCount
                        val leftOffset = taskWidth * index
                        
                        // Only render if within visible range
                        if (startMinutesFromDisplayStart >= 0 && startMinutesFromDisplayStart < displayHours * 60) {
                            Box(
                                modifier = Modifier
                                    .width(taskWidth)
                                    .offset(
                                        x = leftOffset,
                                        y = topOffsetDp
                                    )
                                    .padding(horizontal = 2.dp, vertical = 1.dp)
                            ) {
                                CalendarTaskCard(
                                    task = task,
                                    height = heightDp,
                                    onClick = { onTaskClick(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calculate dynamic time range based on tasks
 * Returns (startHour, endHour) with 1 hour buffer on each side
 */
private fun calculateDynamicRange(tasks: List<Task>, currentHour: Int, todayStart: Long): Pair<Int, Int> {
    if (tasks.isEmpty()) {
        // No tasks: show 4 hours before and after current time
        val startHour = (currentHour - 4).coerceAtLeast(0)
        val endHour = (currentHour + 4).coerceAtMost(24)
        return Pair(startHour, endHour)
    }
    
    // Find earliest start time and latest end time
    var minHour = 24
    var maxHour = 0
    
    tasks.forEach { task ->
        val taskStart = task.startTime ?: task.createdAt
        val taskEnd = task.deadline ?: (taskStart + 60 * 60 * 1000L)
        
        val startHourOfTask = ((taskStart - todayStart) / (60 * 60 * 1000f)).toInt().coerceIn(0, 23)
        val endHourOfTask = ((taskEnd - todayStart) / (60 * 60 * 1000f)).toInt().coerceIn(0, 24)
        
        minHour = minOf(minHour, startHourOfTask)
        maxHour = maxOf(maxHour, endHourOfTask + 1) // +1 to include the end hour
    }
    
    // Add 1 hour buffer on each side
    val startHour = (minHour - 1).coerceAtLeast(0)
    val endHour = (maxHour + 1).coerceAtMost(24)
    
    // Also ensure current hour is visible if within reasonable range
    val adjustedStartHour = if (currentHour < startHour && currentHour >= startHour - 4) {
        currentHour
    } else {
        startHour
    }
    
    val adjustedEndHour = if (currentHour >= endHour && currentHour <= endHour + 4) {
        currentHour + 1
    } else {
        endHour
    }
    
    return Pair(adjustedStartHour.coerceAtLeast(0), adjustedEndHour.coerceAtMost(24))
}

/**
 * Time slot label on the left side
 */
@Composable
private fun TimeSlotLabel(
    hour: Int,
    height: Dp,
    isCurrentHour: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                if (isCurrentHour)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    Color.Transparent
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = String.format(Locale.getDefault(), "%02d:00", hour),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrentHour)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Task card for calendar view with ripple effect
 */
@Composable
private fun CalendarTaskCard(
    task: Task,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = task.isOverdue()
    
    val taskColor = when {
        isOverdue -> MaterialTheme.colorScheme.error
        task.priority == Task.PRIORITY_HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        task.priority == Task.PRIORITY_MEDIUM -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }
    
    val animatedColor by animateColorAsState(
        targetValue = taskColor,
        animationSpec = tween(300),
        label = "task_color"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = animatedColor.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = if (height > 50.dp) 2 else 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (height > 50.dp) {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val startTime = task.startTime ?: task.createdAt
                val endTime = task.deadline ?: (startTime + 60 * 60 * 1000L)
                
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${timeFormat.format(Date(startTime))} - ${timeFormat.format(Date(endTime))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Data class to hold task with its position info
 */
private data class TaskWithPosition(
    val task: Task,
    val column: Int
)

/**
 * Group of overlapping tasks
 */
private data class OverlapGroup(
    val tasks: List<TaskWithPosition>
)

/**
 * Calculate overlapping groups of tasks
 * Tasks that overlap in time should be displayed side by side
 */
private fun calculateOverlappingGroups(tasks: List<Task>, dayStart: Long): List<OverlapGroup> {
    if (tasks.isEmpty()) return emptyList()
    
    // Sort tasks by start time
    val sortedTasks = tasks.sortedBy { it.startTime ?: it.createdAt }
    
    // Find overlapping clusters
    val groups = mutableListOf<MutableList<Task>>()
    var currentGroup = mutableListOf<Task>()
    var groupEnd = 0L
    
    for (task in sortedTasks) {
        val taskStart = task.startTime ?: task.createdAt
        val taskEnd = task.deadline ?: (taskStart + 60 * 60 * 1000L)
        
        if (currentGroup.isEmpty()) {
            currentGroup.add(task)
            groupEnd = taskEnd
        } else if (taskStart < groupEnd) {
            // Overlaps with current group
            currentGroup.add(task)
            groupEnd = maxOf(groupEnd, taskEnd)
        } else {
            // Start new group
            groups.add(currentGroup)
            currentGroup = mutableListOf(task)
            groupEnd = taskEnd
        }
    }
    
    if (currentGroup.isNotEmpty()) {
        groups.add(currentGroup)
    }
    
    // Convert to OverlapGroups with column positions
    return groups.map { groupTasks ->
        val tasksWithPositions = groupTasks.mapIndexed { index, task ->
            TaskWithPosition(task, index)
        }
        OverlapGroup(tasksWithPositions)
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
fun DailyCalendarViewPreview() {
    val now = System.currentTimeMillis()
    val hourMs = 60 * 60 * 1000L
    
    val sampleTasks = listOf(
        Task(
            title = "Morning Meeting",
            startTime = now - 2 * hourMs,
            deadline = now - hourMs,
            priority = Task.PRIORITY_HIGH
        ),
        Task(
            title = "Overlapping Task 1",
            startTime = now,
            deadline = now + 2 * hourMs,
            priority = Task.PRIORITY_MEDIUM
        ),
        Task(
            title = "Overlapping Task 2",
            startTime = now + hourMs / 2,
            deadline = now + 2 * hourMs,
            priority = Task.PRIORITY_LOW
        ),
        Task(
            title = "Afternoon Work",
            startTime = now + 3 * hourMs,
            deadline = now + 5 * hourMs,
            description = "Long task"
        )
    )
    
    Android16DemoTheme {
        DailyCalendarView(
            tasks = sampleTasks,
            onTaskClick = {},
            onTaskComplete = {},
            onTaskDelete = {}
        )
    }
}
