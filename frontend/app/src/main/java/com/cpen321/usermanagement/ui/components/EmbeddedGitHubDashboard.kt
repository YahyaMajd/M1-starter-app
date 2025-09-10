package com.cpen321.usermanagement.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cpen321.usermanagement.ui.screens.GitHubMainContent
import com.cpen321.usermanagement.ui.viewmodels.GitHubViewModel

/**
 * Embedded GitHub dashboard component for the main screen.
 * This version intelligently shows summary widgets when appropriate
 * and full content when the user wants detailed views.
 */
@Composable
fun EmbeddedGitHubDashboard(
    onSetupClick: () -> Unit,
    showSummaryView: Boolean = true,
    onViewFullDashboard: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GitHubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load GitHub data when component is first displayed
    LaunchedEffect(Unit) {
        viewModel.checkConnectionOnly()
        // If connected, also load the GitHub data for summaries
        if (uiState.isGitHubConnected) {
            viewModel.refreshData()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection status
        if (!uiState.isGitHubConnected) {
            GitHubConnectionPrompt(
                onSetupClick = onSetupClick,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Show summary or full content based on preference
            if (showSummaryView) {
                GitHubSummaryWidgets(
                    uiState = uiState,
                    onRepositoryClick = viewModel::selectRepository,
                    onRefresh = viewModel::refreshData,
                    onViewAllClick = onViewFullDashboard,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Add header with exit button for full view
                Column(modifier = Modifier.weight(1f)) {
                    // Exit full view header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onViewFullDashboard) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Exit full view"
                            )
                        }
                        Text(
                            text = "GitHub Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    // Show full GitHub content when connected
                    GitHubMainContent(
                        uiState = uiState,
                        onTabSelected = viewModel::selectTab,
                        onRepositoryClick = viewModel::selectRepository,
                        onRefresh = viewModel::refreshData,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GitHubConnectionPrompt(
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🔗 Connect your GitHub account",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "Access your repositories, commits, and workflow runs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Button(
                onClick = onSetupClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Connect GitHub")
            }
        }
    }
}
