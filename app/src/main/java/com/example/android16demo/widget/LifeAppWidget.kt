package com.example.android16demo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.android16demo.LifeApp
import com.example.android16demo.data.entity.Task
import kotlinx.coroutines.flow.first

/**
 * Life App Widget displaying current task and today's summary
 */
class LifeAppWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? LifeApp
        val repository = app?.taskRepository
        
        val activeTasks = repository?.getActiveTasks()?.first() ?: emptyList()
        val currentTask = activeTasks.firstOrNull()
        val todayTaskCount = activeTasks.size
        
        provideContent {
            GlanceTheme {
                WidgetContent(
                    currentTask = currentTask,
                    todayTaskCount = todayTaskCount
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    currentTask: Task?,
    todayTaskCount: Int
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Header
        Text(
            text = "Life App",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GlanceTheme.colors.primary
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Current task
        if (currentTask != null) {
            Text(
                text = "Current Task",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = currentTask.title,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onBackground
                ),
                maxLines = 2
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // Progress indicator (text-based)
            val progressPercent = (currentTask.progress * 100).toInt()
            Text(
                text = "Progress: $progressPercent%",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.primary
                )
            )
        } else {
            Text(
                text = "No active tasks",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = "Tap to add a task",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.primary
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.defaultWeight())
        
        // Footer with task count
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$todayTaskCount tasks remaining",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Widget receiver for Life App Widget
 */
class LifeAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LifeAppWidget()
}
