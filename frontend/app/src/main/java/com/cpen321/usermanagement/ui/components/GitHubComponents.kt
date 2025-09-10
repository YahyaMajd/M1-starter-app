package com.cpen321.usermanagement.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cpen321.usermanagement.data.remote.dto.GitHubCommit
import com.cpen321.usermanagement.data.remote.dto.GitHubRepository
import com.cpen321.usermanagement.data.remote.dto.GitHubUser
import com.cpen321.usermanagement.data.remote.dto.GitHubWorkflowRun
import com.cpen321.usermanagement.ui.viewmodels.GitHubTab
import com.google.android.gms.maps.model.Circle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubConnectionCard(
    isConnected: Boolean,
    isConnecting: Boolean,
    githubUser: GitHubUser?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "GitHub",
                    modifier = Modifier.size(32.dp),
                    tint = if (isConnected) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GitHub Integration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isConnected && githubUser != null) {
                            "Connected as ${githubUser.login}"
                        } else {
                            "Connect your GitHub account"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isConnected && githubUser?.avatarUrl != null) {
                    AsyncImage(
                        model = githubUser.avatarUrl,
                        contentDescription = "GitHub Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            if (isConnecting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Text(
                        text = "Connecting...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Button(
                    onClick = if (isConnected) onDisconnect else onConnect,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) 
                            MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isConnected) "Disconnect" else "Connect GitHub")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubTabRow(
    selectedTab: GitHubTab,
    onTabSelected: (GitHubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        GitHubTab.values().forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = when (tab) {
                            GitHubTab.REPOSITORIES -> "Repositories"
                            GitHubTab.COMMITS -> "Recent Commits"
                            GitHubTab.ACTIONS -> "Actions"
                        },
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            GitHubTab.REPOSITORIES -> Icons.AutoMirrored.Filled.List
                            GitHubTab.COMMITS -> Icons.Default.DateRange
                            GitHubTab.ACTIONS -> Icons.Default.PlayArrow
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryCard(
    repository: GitHubRepository,
    onRepositoryClick: (GitHubRepository) -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRepositoryClick(repository) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            CardDefaults.cardColors()
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (repository.private) Icons.Default.Lock else Icons.Default.Star,
                    contentDescription = if (repository.private) "Private" else "Public",
                    modifier = Modifier.size(16.dp),
                    tint = if (repository.private) MaterialTheme.colorScheme.error 
                           else MaterialTheme.colorScheme.primary
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = repository.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (repository.description != null) {
                        Text(
                            text = repository.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (repository.language != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = getLanguageColor(repository.language),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = repository.language,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = repository.stargazersCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = formatDate(repository.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitCard(
    commit: GitHubCommit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (commit.author?.avatarUrl != null) {
                    AsyncImage(
                        model = commit.author.avatarUrl,
                        contentDescription = "Author Avatar",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Author",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = commit.commit.message,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = commit.commit.author.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = formatDate(commit.commit.author.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = commit.sha.take(7),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowRunCard(
    workflowRun: GitHubWorkflowRun,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = getWorkflowStatusIcon(workflowRun.status, workflowRun.conclusion),
                contentDescription = "Workflow Status",
                modifier = Modifier.size(20.dp),
                tint = getWorkflowStatusColor(workflowRun.status, workflowRun.conclusion)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workflowRun.name ?: "Unknown Workflow",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${workflowRun.event ?: workflowRun.headBranch ?: "main"} • ${formatDate(workflowRun.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = workflowRun.conclusion?.replaceFirstChar { it.uppercase() } 
                       ?: workflowRun.status.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = getWorkflowStatusColor(workflowRun.status, workflowRun.conclusion),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LoadingIndicator(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateMessage(
    message: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper functions
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun getLanguageColor(language: String): Color {
    return when (language.lowercase()) {
        "kotlin" -> Color(0xFF7F52FF)
        "java" -> Color(0xFFED8B00)
        "javascript" -> Color(0xFFF7DF1E)
        "typescript" -> Color(0xFF3178C6)
        "python" -> Color(0xFF3776AB)
        "swift" -> Color(0xFFFFA500)
        "go" -> Color(0xFF00ADD8)
        "rust" -> Color(0xFF000000)
        "c++" -> Color(0xFF00599C)
        else -> Color(0xFF888888)
    }
}

private fun getWorkflowStatusIcon(status: String, conclusion: String?): ImageVector {
    return when {
        conclusion == "success" -> Icons.Default.CheckCircle
        conclusion == "failure" -> Icons.Default.Close
        conclusion == "cancelled" -> Icons.Default.Clear
        status == "in_progress" -> Icons.Default.PlayArrow
        else -> Icons.Default.Info
    }
}

@Composable
private fun getWorkflowStatusColor(status: String, conclusion: String?): Color {
    return when {
        conclusion == "success" -> Color(0xFF28A745)
        conclusion == "failure" -> MaterialTheme.colorScheme.error
        conclusion == "cancelled" -> Color(0xFF6F42C1)
        status == "in_progress" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
