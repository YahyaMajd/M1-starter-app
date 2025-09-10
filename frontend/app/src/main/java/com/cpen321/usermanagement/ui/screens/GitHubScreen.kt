package com.cpen321.usermanagement.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.network.auth.GitHubAuthService
import com.cpen321.usermanagement.ui.components.*
import com.cpen321.usermanagement.ui.viewmodels.GitHubTab
import com.cpen321.usermanagement.ui.viewmodels.GitHubViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubScreen(
    onSetupClick: () -> Unit = {},
    viewModel: GitHubViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Get navigation for back button
    val navigationViewModel: com.cpen321.usermanagement.ui.viewmodels.NavigationViewModel = hiltViewModel()

    // ========== SCREEN RESUME DETECTION ==========
    // Check connection when screen is entered (OAuth monitoring is reactive)
    LaunchedEffect(Unit) {
        Log.d("GitHubScreen", "Screen entered - checking connection only (OAuth is reactive)")
        viewModel.checkConnectionOnly()
    }

    // OAuth launcher for GitHub authentication
    val oauthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                // OAuth successful, refresh state and load data
                Log.d("GitHubScreen", "OAuth successful, refreshing state")
                coroutineScope.launch {
                    viewModel.onOAuthSuccess()
                }
            }
            android.app.Activity.RESULT_CANCELED -> {
                // OAuth failed or cancelled - but check if token actually exists (for already-authorized case)
                Log.d("GitHubScreen", "OAuth result cancelled, checking if token exists")
                coroutineScope.launch {
                    // Give a small delay to ensure token storage completes if it was successful
                    delay(500)
                    viewModel.forceFullRefresh()
                    
                    // If still not connected after refresh, show error
                    if (!viewModel.uiState.value.isGitHubConnected) {
                        val error = result.data?.getStringExtra("error") ?: "OAuth authentication cancelled"
                        viewModel.setErrorMessage(error)
                    }
                }
            }
        }
    }

    // Handle error and success messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            // Error will be shown in the UI
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            // Success message will be shown in the UI
        }
    }

    Scaffold(
        topBar = {
            GitHubTopBar(onBackClick = { navigationViewModel.navigationStateManager.navigateBack() })
        }
    ) { paddingValues ->
        GitHubContent(
            uiState = uiState,
            viewModel = viewModel,
            paddingValues = paddingValues,
            modifier = modifier,
            onOAuthLaunch = { authIntent ->
                oauthLauncher.launch(authIntent)
            },
            onSetupClick = onSetupClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GitHubTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text("GitHub Dashboard") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun GitHubContent(
    uiState: com.cpen321.usermanagement.ui.viewmodels.GitHubUiState,
    viewModel: GitHubViewModel,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    onOAuthLaunch: (android.content.Intent) -> Unit,
    onSetupClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // OAuth Setup Check
        if (!uiState.hasOAuthCredentials) {
            GitHubSetupCard(onSetupClick = onSetupClick)
        } else {
            // GitHub Connection Card
            GitHubConnectionCard(
                isConnected = uiState.isGitHubConnected,
                isConnecting = uiState.isConnecting,
                githubUser = uiState.githubUser,
                onConnect = {
                    // Launch GitHub OAuth flow
                    viewModel.launchGitHubOAuth { authIntent ->
                        onOAuthLaunch(authIntent)
                    }
                },
                onDisconnect = viewModel::disconnectGitHub
            )
        }

        // Error Message
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::clearError) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Success Message
        uiState.successMessage?.let { success ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = success,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::clearSuccessMessage) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // GitHub Content (only show if connected)
        if (uiState.isGitHubConnected) {
            GitHubMainContent(
                uiState = uiState,
                onTabSelected = viewModel::selectTab,
                onRepositoryClick = viewModel::selectRepository,
                onRefresh = viewModel::refreshData,
                modifier = Modifier.weight(1f)
            )
        } else {
            // Not connected state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateMessage(
                    message = "Connect your GitHub account to view repositories, commits, and workflow runs"
                )
            }
        }
    }
}

@Composable
fun GitHubMainContent(
    uiState: com.cpen321.usermanagement.ui.viewmodels.GitHubUiState,
    onTabSelected: (GitHubTab) -> Unit,
    onRepositoryClick: (com.cpen321.usermanagement.data.remote.dto.GitHubRepository) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Row
        GitHubTabRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabSelected
        )

        // Content based on selected tab
        when (uiState.selectedTab) {
            GitHubTab.REPOSITORIES -> {
                RepositoriesContent(
                    repositories = uiState.repositories,
                    isLoading = uiState.isLoadingRepositories,
                    selectedRepository = uiState.selectedRepository,
                    onRepositoryClick = onRepositoryClick,
                    onRefresh = onRefresh,
                    modifier = Modifier.weight(1f)
                )
            }
            GitHubTab.COMMITS -> {
                CommitsContent(
                    commits = uiState.recentCommits,
                    isLoading = uiState.isLoadingCommits,
                    selectedRepository = uiState.selectedRepository,
                    modifier = Modifier.weight(1f)
                )
            }
            GitHubTab.ACTIONS -> {
                ActionsContent(
                    workflowRuns = uiState.workflowRuns,
                    isLoading = uiState.isLoadingWorkflows,
                    selectedRepository = uiState.selectedRepository,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RepositoriesContent(
    repositories: List<com.cpen321.usermanagement.data.remote.dto.GitHubRepository>,
    isLoading: Boolean,
    selectedRepository: com.cpen321.usermanagement.data.remote.dto.GitHubRepository?,
    onRepositoryClick: (com.cpen321.usermanagement.data.remote.dto.GitHubRepository) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator("Loading repositories...")
                }
            }
            repositories.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EmptyStateMessage("No repositories found")
                        Button(onClick = onRefresh) {
                            Text("Refresh")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(repositories) { repository ->
                        RepositoryCard(
                            repository = repository,
                            onRepositoryClick = onRepositoryClick,
                            isSelected = selectedRepository?.id == repository.id
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommitsContent(
    commits: List<com.cpen321.usermanagement.data.remote.dto.GitHubCommit>,
    isLoading: Boolean,
    selectedRepository: com.cpen321.usermanagement.data.remote.dto.GitHubRepository?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            selectedRepository == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateMessage("Select a repository to view commits")
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator("Loading commits...")
                }
            }
            commits.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateMessage("No commits found for ${selectedRepository.name}")
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(commits) { commit ->
                        CommitCard(commit = commit)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsContent(
    workflowRuns: List<com.cpen321.usermanagement.data.remote.dto.GitHubWorkflowRun>,
    isLoading: Boolean,
    selectedRepository: com.cpen321.usermanagement.data.remote.dto.GitHubRepository?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            selectedRepository == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateMessage("Select a repository to view workflow runs")
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator("Loading workflow runs...")
                }
            }
            workflowRuns.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateMessage("No workflow runs found for ${selectedRepository.name}")
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(workflowRuns) { workflowRun ->
                        WorkflowRunCard(workflowRun = workflowRun)
                    }
                }
            }
        }
    }
}

@Composable
private fun GitHubSetupCard(
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GitHub Setup Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = "To use GitHub features, you need to configure your GitHub OAuth credentials first.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            
            Button(
                onClick = onSetupClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set Up GitHub Integration")
            }
        }
    }
}
