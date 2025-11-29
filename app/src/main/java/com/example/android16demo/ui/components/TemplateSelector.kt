package com.example.android16demo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android16demo.data.entity.TaskTemplate
import com.example.android16demo.ui.theme.Android16DemoTheme

/**
 * Horizontal scrolling template selector for quick task creation
 */
@Composable
fun TemplateSelector(
    templates: List<TaskTemplate>,
    onTemplateSelected: (TaskTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Start",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(templates, key = { it.id }) { template ->
                TemplateChip(
                    template = template,
                    onClick = { onTemplateSelected(template) }
                )
            }
        }
    }
}

/**
 * Individual template chip
 */
@Composable
private fun TemplateChip(
    template: TaskTemplate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor = try {
        Color(android.graphics.Color.parseColor(template.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTemplateIcon(template.iconName),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.labelLarge
                )
                template.defaultDurationMinutes?.let { duration ->
                    Text(
                        text = "$duration min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Get icon for template based on icon name
 */
private fun getTemplateIcon(iconName: String): ImageVector {
    return when (iconName) {
        "work" -> Icons.Default.Work
        "school" -> Icons.Default.School
        "fitness" -> Icons.Default.FitnessCenter
        "meeting" -> Icons.Default.Groups
        "person" -> Icons.Default.Person
        else -> Icons.Default.Work
    }
}

@Preview(showBackground = true)
@Composable
fun TemplateSelectorPreview() {
    Android16DemoTheme {
        TemplateSelector(
            templates = TaskTemplate.getDefaultTemplates(),
            onTemplateSelected = {}
        )
    }
}
