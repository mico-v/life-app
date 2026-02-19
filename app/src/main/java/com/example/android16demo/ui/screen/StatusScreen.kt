package com.example.android16demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android16demo.R
import com.example.android16demo.network.model.PostDtoV2
import com.example.android16demo.network.model.StatusSourceDto
import com.example.android16demo.viewmodel.StatusUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusScreen(
    uiState: StatusUiState,
    onRefresh: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.nav_status), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Button(onClick = onRefresh) {
                    Text(stringResource(R.string.action_refresh))
                }
            }
        }

        uiState.primary?.let { primary ->
            item {
                PrimaryStatusCard(primary, uiState.updatedAt)
            }
        }

        item {
            Text(stringResource(R.string.status_sources), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        if (uiState.sources.isEmpty()) {
            item {
                Text(stringResource(R.string.status_no_active_sources))
            }
        } else {
            items(uiState.sources) { source ->
                SourceStatusCard(source)
            }
        }

        item {
            Text(stringResource(R.string.status_latest_posts), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        if (uiState.posts.isEmpty()) {
            item {
                Text(stringResource(R.string.status_no_posts_yet))
            }
        } else {
            items(uiState.posts) { post ->
                PostCard(post)
            }
        }

        if (uiState.loading) {
            item {
                CircularProgressIndicator()
            }
        }

        uiState.error?.let { error ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                        Button(onClick = onErrorDismiss) {
                            Text(stringResource(R.string.action_dismiss))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryStatusCard(primary: StatusSourceDto, updatedAt: Long?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(primary.status, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.status_source_prefix, primary.source), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.status_updated_prefix, formatTs(updatedAt ?: primary.observedAt)), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SourceStatusCard(source: StatusSourceDto) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${source.source} · ${source.status}", fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.status_observed_prefix, formatTs(source.observedAt)), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.status_expires_prefix, formatTs(source.expiresAt)), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PostCard(post: PostDtoV2) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(post.content)
            val meta = buildString {
                append(formatTs(post.createdAt))
                if (!post.tags.isNullOrBlank()) append("  #${post.tags}")
                if (!post.location.isNullOrBlank()) append("  @${post.location}")
            }
            Text(meta, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatTs(ts: Long?): String {
    if (ts == null || ts <= 0L) return ""
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(Date(ts))
}
