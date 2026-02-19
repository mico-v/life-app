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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android16demo.R
import com.example.android16demo.network.model.PostDtoV2
import com.example.android16demo.viewmodel.PublishUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PublishScreen(
    uiState: PublishUiState,
    onServerUrlChanged: (String) -> Unit,
    onClientTokenChanged: (String) -> Unit,
    onServerPasswordChanged: (String) -> Unit,
    onStatusChanged: (String) -> Unit,
    onTtlChanged: (String) -> Unit,
    onPostContentChanged: (String) -> Unit,
    onPostTagsChanged: (String) -> Unit,
    onPostLocationChanged: (String) -> Unit,
    onPublishStatus: () -> Unit,
    onPublishPost: () -> Unit,
    onRefreshPosts: () -> Unit,
    onDeletePost: (String) -> Unit,
    onClearMessages: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.nav_publish), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        item {
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.serverUrl,
                        onValueChange = onServerUrlChanged,
                        label = { Text(stringResource(R.string.label_server_url)) }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.clientToken,
                        onValueChange = onClientTokenChanged,
                        label = { Text(stringResource(R.string.label_client_token)) }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.serverPassword,
                        onValueChange = onServerPasswordChanged,
                        label = { Text(stringResource(R.string.label_server_password)) }
                    )
                }
            }
        }

        item {
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.publish_status_section), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.statusText,
                        onValueChange = onStatusChanged,
                        label = { Text(stringResource(R.string.label_status)) }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.statusTtlMinutes,
                        onValueChange = onTtlChanged,
                        label = { Text(stringResource(R.string.label_ttl_minutes)) }
                    )
                    Button(onClick = onPublishStatus) { Text(stringResource(R.string.action_publish_status)) }
                }
            }
        }

        item {
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.publish_post_section), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.postContent,
                        onValueChange = onPostContentChanged,
                        label = { Text(stringResource(R.string.label_content)) }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.postTags,
                        onValueChange = onPostTagsChanged,
                        label = { Text(stringResource(R.string.label_tags)) }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.postLocation,
                        onValueChange = onPostLocationChanged,
                        label = { Text(stringResource(R.string.label_location)) }
                    )
                    Button(onClick = onPublishPost) { Text(stringResource(R.string.action_publish_post)) }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.publish_my_posts), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onRefreshPosts) { Text(stringResource(R.string.action_refresh)) }
            }
        }

        if (uiState.posts.isEmpty()) {
            item { Text(stringResource(R.string.publish_no_posts)) }
        } else {
            items(uiState.posts) { post ->
                PublishPostItem(post = post, onDelete = { onDeletePost(post.id) })
            }
        }

        uiState.message?.let {
            item {
                Card {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(it, color = MaterialTheme.colorScheme.primary)
                        TextButton(onClick = onClearMessages) { Text(stringResource(R.string.action_dismiss)) }
                    }
                }
            }
        }

        uiState.error?.let {
            item {
                Card {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onClearMessages) { Text(stringResource(R.string.action_dismiss)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublishPostItem(post: PostDtoV2, onDelete: () -> Unit) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(post.content)
            Text(
                buildString {
                    append(formatTs(post.createdAt))
                    if (!post.tags.isNullOrBlank()) append("  #${post.tags}")
                    if (!post.location.isNullOrBlank()) append("  @${post.location}")
                },
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatTs(ts: Long?): String {
    if (ts == null || ts <= 0L) return ""
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(Date(ts))
}
