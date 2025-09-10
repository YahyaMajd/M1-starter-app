package com.cpen321.usermanagement.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale
import com.cpen321.usermanagement.data.remote.dto.GitHubCommit
import com.cpen321.usermanagement.data.remote.dto.GitHubRepository
import com.cpen321.usermanagement.data.remote.dto.GitHubWorkflowRun
import com.cpen321.usermanagement.ui.viewmodels.GitHubUiState

/**
 * GitHub summary widgets showing key metrics and recent activity
 * in a compact, at-a-glance format for the main dashboard.
 */
@Composable
fun GitHubSummaryWidgets(
    uiState: GitHubUiState,
    onRepositoryClick: (GitHubRepository) -> Unit,
    onRefresh: () -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top metrics row
        GitHubMetricsRow(
            uiState = uiState,
            onRefresh = onRefresh,
            onViewAllClick = onViewAllClick
        )
        
        // Recent repositories section
        if (uiState.repositories.isNotEmpty()) {
            RecentRepositoriesSection(
                repositories = uiState.repositories.take(3),
                onRepositoryClick = onRepositoryClick,
                isLoading = uiState.isLoadingRepositories
            )
        }
        
        // Recent commits section
        if (uiState.recentCommits.isNotEmpty()) {
            RecentCommitsSection(
                commits = uiState.recentCommits.take(3),
                isLoading = uiState.isLoadingCommits
            )
        }
        
        // Workflow status section
        if (uiState.workflowRuns.isNotEmpty()) {
            WorkflowStatusSection(
                workflowRuns = uiState.workflowRuns.take(3),
                isLoading = uiState.isLoadingWorkflows
            )
        }
    }
}

@Composable
private fun GitHubMetricsRow(
    uiState: GitHubUiState,
    onRefresh: () -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GitHub Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricItem(
                        icon = Icons.Default.AccountCircle,
                        label = "Repos",
                        value = uiState.repositories.size.toString()
                    )
                    
                    MetricItem(
                        icon = Icons.Default.Edit,
                        label = "Commits",
                        value = uiState.recentCommits.size.toString()
                    )
                    
                    MetricItem(
                        icon = Icons.Default.PlayArrow,
                        label = "Actions",
                        value = uiState.workflowRuns.size.toString()
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                TextButton(onClick = onViewAllClick) {
                    Text(
                        text = "View All",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "$value $label",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun RecentRepositoriesSection(
    repositories: List<GitHubRepository>,
    onRepositoryClick: (GitHubRepository) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            title = "Recent Repositories",
            icon = Icons.Default.AccountCircle
        )
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(repositories) { repository ->
                    RepositoryCard(
                        repository = repository,
                        onClick = { onRepositoryClick(repository) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RepositoryCard(
    repository: GitHubRepository,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = repository.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (repository.description?.isNotEmpty() == true) {
                Text(
                    text = repository.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (repository.language?.isNotEmpty() == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = repository.language,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = repository.stargazersCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentCommitsSection(
    commits: List<GitHubCommit>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            title = "Recent Commits",
            icon = Icons.Default.Edit
        )
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    commits.forEach { commit ->
                        CommitItem(commit = commit)
                        if (commit != commits.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommitItem(
    commit: GitHubCommit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = commit.commit.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "by ${commit.commit.author.name} • ${commit.sha.take(7)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkflowStatusSection(
    workflowRuns: List<GitHubWorkflowRun>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            title = "Recent Actions",
            icon = Icons.Default.PlayArrow
        )
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(workflowRuns) { workflow ->
                    WorkflowCard(workflow = workflow)
                }
            }
        }
    }
}

@Composable
private fun WorkflowCard(
    workflow: GitHubWorkflowRun,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (workflow.status) {
                "completed" -> if (workflow.conclusion == "success") {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = when (workflow.conclusion) {
                        "success" -> Icons.Default.CheckCircle
                        "failure" -> Icons.Default.Close
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when (workflow.conclusion) {
                        "success" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "failure" -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Text(
                    text = workflow.status.replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (workflow.conclusion) {
                        "success" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "failure" -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Text(
                text = workflow.name ?: "Workflow",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
