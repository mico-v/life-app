package com.example.android16demo.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android16demo.R
import com.example.android16demo.ui.theme.Android16DemoTheme
import com.example.android16demo.viewmodel.DayStats
import com.example.android16demo.viewmodel.ProfileUiState
import com.example.android16demo.viewmodel.TaskStatistics
import com.example.android16demo.viewmodel.UserProfile

/**
 * Profile Screen with statistics, user information, and customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onUpdateMotto: (String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onErrorDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true
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
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(stringResource(R.string.title_profile)) },
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.btn_refresh)
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.nav_settings)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Info Card with customization
                item {
                    UserInfoCard(
                        userProfile = uiState.userProfile,
                        onUpdateDisplayName = onUpdateDisplayName,
                        onUpdateMotto = onUpdateMotto,
                        onUpdateStatus = onUpdateStatus
                    )
                }
                
                // Status Card
                item {
                    StatusCard(status = uiState.userProfile.status)
                }
                
                // Stats Overview
                item {
                    StatsOverviewCard(statistics = uiState.statistics)
                }
                
                // Achievement Progress
                item {
                    AchievementProgressCard(statistics = uiState.statistics)
                }
                
                // Weekly Chart
                item {
                    WeeklyChartCard(weeklyData = uiState.statistics.weeklyData)
                }
                
                // Completion Rate
                item {
                    CompletionRateCard(completionRate = uiState.statistics.completionRate)
                }
                
                // All-time stats
                item {
                    AllTimeStatsCard(totalCompleted = uiState.statistics.totalCompletedAllTime)
                }
            }
        }
    }
}

/**
 * User info card with editable fields
 */
@Composable
private fun UserInfoCard(
    userProfile: UserProfile,
    onUpdateDisplayName: (String) -> Unit,
    onUpdateMotto: (String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userProfile.displayName) }
    var editMotto by remember { mutableStateOf(userProfile.motto) }
    
    // Sync edit state with userProfile updates when not editing
    LaunchedEffect(userProfile.displayName, userProfile.motto) {
        if (!isEditing) {
            editName = userProfile.displayName
            editMotto = userProfile.motto
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text(stringResource(R.string.label_display_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = userProfile.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (!isEditing) {
                    IconButton(onClick = { 
                        isEditing = true 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.btn_edit),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Motto
            if (isEditing) {
                OutlinedTextField(
                    value = editMotto,
                    onValueChange = { editMotto = it },
                    label = { Text(stringResource(R.string.label_motto)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${userProfile.motto}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            if (isEditing) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        editName = userProfile.displayName
                        editMotto = userProfile.motto
                        isEditing = false
                    }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onUpdateDisplayName(editName)
                        onUpdateMotto(editMotto)
                        isEditing = false
                    }) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }
    }
}

/**
 * Status card showing current user status
 */
@Composable
private fun StatusCard(
    status: String,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status.lowercase()) {
        "busy" -> MaterialTheme.colorScheme.error
        "available" -> MaterialTheme.colorScheme.primary
        "away" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.label_status) + ": ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

/**
 * Achievement progress card
 */
@Composable
private fun AchievementProgressCard(
    statistics: TaskStatistics,
    modifier: Modifier = Modifier
) {
    @Composable
    fun achievementsList(): List<Triple<String, Int, Boolean>> {
        return listOf(
            Triple(stringResource(R.string.achievement_first_steps), 1, statistics.totalCompletedAllTime >= 1),
            Triple(stringResource(R.string.achievement_getting_started), 10, statistics.totalCompletedAllTime >= 10),
            Triple(stringResource(R.string.achievement_productive), 50, statistics.totalCompletedAllTime >= 50),
            Triple(stringResource(R.string.achievement_master), 100, statistics.totalCompletedAllTime >= 100),
            Triple(stringResource(R.string.achievement_legend), 500, statistics.totalCompletedAllTime >= 500)
        )
    }
    
    val achievements = achievementsList()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.title_achievements),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            achievements.forEach { (name, target, achieved) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (achieved) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (achieved) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = stringResource(R.string.achievement_complete_tasks, target),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (achieved) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.completed),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Stats overview with key metrics
 */
@Composable
private fun StatsOverviewCard(
    statistics: TaskStatistics,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.title_statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    value = statistics.completedToday.toString(),
                    label = stringResource(R.string.stats_today),
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.Default.TrendingUp,
                    value = statistics.completedThisWeek.toString(),
                    label = stringResource(R.string.stats_this_week),
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    icon = Icons.Default.Pending,
                    value = statistics.activeTasks.toString(),
                    label = stringResource(R.string.stats_active),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/**
 * Individual stat item
 */
@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Weekly chart showing daily completions
 */
@Composable
private fun WeeklyChartCard(
    weeklyData: List<DayStats>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.title_weekly_activity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val maxValue = (weeklyData.maxOfOrNull { it.completedCount } ?: 1).coerceAtLeast(1)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { dayData ->
                    DayBar(
                        dayName = dayData.dayOfWeek,
                        value = dayData.completedCount,
                        maxValue = maxValue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Individual day bar in the chart
 */
@Composable
private fun DayBar(
    dayName: String,
    value: Int,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    val barHeight = if (maxValue > 0) (value.toFloat() / maxValue) * 80f else 0f
    val animatedHeight by animateFloatAsState(
        targetValue = barHeight,
        animationSpec = tween(durationMillis = 500),
        label = "bar_height"
    )
    
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(animatedHeight.dp.coerceAtLeast(4.dp))
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Completion rate circular progress
 */
@Composable
private fun CompletionRateCard(
    completionRate: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = completionRate / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "completion_rate"
    )
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.title_completion_rate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressCanvas(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${completionRate.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.label_on_time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * All-time stats card
 */
@Composable
private fun AllTimeStatsCard(
    totalCompleted: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.title_all_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = totalCompleted.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "tasks popped",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Circular progress indicator using Canvas
 */
@Composable
private fun CircularProgressCanvas(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // Background circle
        drawCircle(
            color = backgroundColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Progress arc
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    Android16DemoTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                statistics = TaskStatistics(
                    completedToday = 5,
                    completedThisWeek = 23,
                    activeTasks = 8,
                    totalCompletedAllTime = 156,
                    completionRate = 78f,
                    weeklyData = listOf(
                        DayStats("Mon", 3),
                        DayStats("Tue", 5),
                        DayStats("Wed", 2),
                        DayStats("Thu", 4),
                        DayStats("Fri", 6),
                        DayStats("Sat", 1),
                        DayStats("Sun", 2)
                    )
                ),
                userProfile = UserProfile(
                    displayName = "John Doe",
                    motto = "Stay productive, stay happy!",
                    status = "Available"
                ),
                isLoading = false
            ),
            onRefresh = {},
            onSettingsClick = {},
            onUpdateDisplayName = {},
            onUpdateMotto = {},
            onUpdateStatus = {},
            onErrorDismiss = {}
        )
    }
}
