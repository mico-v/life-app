package com.example.android16demo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android16demo.data.entity.Task
import com.example.android16demo.ui.theme.Android16DemoTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Timeline View Component
 * Displays tasks in a vertical timeline with time indicators
 */
@Composable
fun TimelineView(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedTasks = groupTasksByTimeSection(tasks)
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        groupedTasks.forEach { (section, sectionTasks) ->
            item(key = "header_$section") {
                TimelineSectionHeader(section = section)
            }
            
            itemsIndexed(
                items = sectionTasks,
                key = { _, task -> task.id }
            ) { index, task ->
                TimelineTaskItem(
                    task = task,
                    isLast = index == sectionTasks.lastIndex,
                    onClick = { onTaskClick(task.id) },
                    onComplete = { onTaskComplete(task.id) },
                    onDelete = { onTaskDelete(task.id) }
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

/**
 * Groups tasks into time sections: Overdue, Today, Tomorrow, This Week, Later
 */
private fun groupTasksByTimeSection(tasks: List<Task>): Map<TimeSection, List<Task>> {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    
    // Start of today
    calendar.timeInMillis = now
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis
    
    // End of today
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val endOfToday = calendar.timeInMillis
    
    // End of tomorrow
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val endOfTomorrow = calendar.timeInMillis
    
    // End of this week
    calendar.timeInMillis = now
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    calendar.add(Calendar.WEEK_OF_YEAR, 1)
    val endOfWeek = calendar.timeInMillis
    
    return tasks.groupBy { task ->
        val deadline = task.deadline ?: task.startTime ?: task.createdAt
        when {
            task.isOverdue() -> TimeSection.OVERDUE
            deadline < endOfToday -> TimeSection.TODAY
            deadline < endOfTomorrow -> TimeSection.TOMORROW
            deadline < endOfWeek -> TimeSection.THIS_WEEK
            else -> TimeSection.LATER
        }
    }.toSortedMap(compareBy { it.ordinal })
}

enum class TimeSection(val displayName: String) {
    OVERDUE("Overdue"),
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    THIS_WEEK("This Week"),
    LATER("Later")
}

/**
 * Section header for timeline
 */
@Composable
private fun TimelineSectionHeader(
    section: TimeSection,
    modifier: Modifier = Modifier
) {
    val color = when (section) {
        TimeSection.OVERDUE -> MaterialTheme.colorScheme.error
        TimeSection.TODAY -> MaterialTheme.colorScheme.primary
        TimeSection.TOMORROW -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = section.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Timeline task item with connector line
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineTaskItem(
    task: Task,
    isLast: Boolean,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = task.isOverdue()
    val lineColor = if (isOverdue) 
        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    else 
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // Timeline line and dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            // Dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isOverdue) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
            
            // Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(lineColor)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Task card
        TaskItem(
            task = task,
            onComplete = onComplete,
            onDelete = onDelete,
            onClick = onClick,
            modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp)
        )
    }
}

/**
 * Format time display
 */
private fun formatTime(timestamp: Long): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun TimelineViewPreview() {
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
        ),
        Task(
            title = "Future Task",
            deadline = System.currentTimeMillis() + 604800000,
            priority = Task.PRIORITY_LOW
        )
    )
    
    Android16DemoTheme {
        TimelineView(
            tasks = sampleTasks,
            onTaskClick = {},
            onTaskComplete = {},
            onTaskDelete = {}
        )
    }
}
