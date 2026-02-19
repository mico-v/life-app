package com.example.android16demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android16demo.R
import com.example.android16demo.viewmodel.WebProfileUiState

@Composable
fun WebProfileScreen(
    uiState: WebProfileUiState,
    onDisplayNameChanged: (String) -> Unit,
    onMottoChanged: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: () -> Unit,
    onClearFlags: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.nav_profile), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Button(onClick = onRefresh) { Text(stringResource(R.string.action_refresh)) }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.displayName,
            onValueChange = onDisplayNameChanged,
            label = { Text(stringResource(R.string.label_display_name)) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.motto,
            onValueChange = onMottoChanged,
            label = { Text(stringResource(R.string.label_motto)) }
        )

        Button(onClick = onSave) { Text(stringResource(R.string.action_save_profile)) }

        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.profile_total_posts, uiState.totalPosts))
                Text(stringResource(R.string.profile_active_sources, uiState.activeSources))
                Text(stringResource(R.string.profile_primary_status, uiState.primaryStatus))
            }
        }

        uiState.error?.let {
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onClearFlags) { Text(stringResource(R.string.action_dismiss)) }
                }
            }
        }

        if (uiState.saved) {
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.profile_saved), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
