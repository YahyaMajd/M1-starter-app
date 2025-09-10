package com.cpen321.usermanagement.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.dto.GitHubCommit
import com.cpen321.usermanagement.data.remote.dto.GitHubRepository
import com.cpen321.usermanagement.data.remote.dto.GitHubUser
import com.cpen321.usermanagement.data.remote.dto.GitHubWorkflowRun
import com.cpen321.usermanagement.data.repository.GitHubRepository as GitHubRepositoryInterface
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.domain.usecase.github.CheckOAuthCredentialsUseCase
import com.cpen321.usermanagement.domain.usecase.github.GitHubConnectionResult
import com.cpen321.usermanagement.domain.usecase.github.GitHubConnectionState
import com.cpen321.usermanagement.domain.usecase.github.ManageGitHubConnectionUseCase
import com.cpen321.usermanagement.domain.usecase.github.OAuthCredentialsCheckResult
import com.cpen321.usermanagement.domain.usecase.github.ReactiveOAuthCredentialsUseCase
import com.cpen321.usermanagement.domain.usecase.github.ReactiveOAuthState
import com.cpen321.usermanagement.network.auth.GitHubAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GitHubUiState(
    // Loading states
    val isLoadingRepositories: Boolean = false,
    val isLoadingCommits: Boolean = false,
    val isLoadingWorkflows: Boolean = false,
    val isConnecting: Boolean = false,

    // Data states
    val repositories: List<GitHubRepository> = emptyList(),
    val selectedRepository: GitHubRepository? = null,
    val recentCommits: List<GitHubCommit> = emptyList(),
    val workflowRuns: List<GitHubWorkflowRun> = emptyList(),
    val githubUser: GitHubUser? = null,

    // Connection state
    val isGitHubConnected: Boolean = false,
    val hasOAuthCredentials: Boolean = false,

    // Message states
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // View states
    val selectedTab: GitHubTab = GitHubTab.REPOSITORIES
)

enum class GitHubTab {
    REPOSITORIES,
    COMMITS,
    ACTIONS
}

@HiltViewModel
class GitHubViewModel @Inject constructor(
    private val githubRepository: GitHubRepositoryInterface,
    private val githubAuthService: GitHubAuthService,
    private val tokenManager: TokenManager,
    private val profileRepository: ProfileRepository,
    private val manageGitHubConnectionUseCase: ManageGitHubConnectionUseCase,
    private val checkOAuthCredentialsUseCase: CheckOAuthCredentialsUseCase,
    private val reactiveOAuthCredentialsUseCase: ReactiveOAuthCredentialsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "GitHubViewModel"
    }

    private val _uiState = MutableStateFlow(GitHubUiState())
    val uiState: StateFlow<GitHubUiState> = _uiState.asStateFlow()

    init {
        checkGitHubConnection()
        startOAuthCredentialsMonitoring()
    }

    // ========== REACTIVE OAUTH MONITORING ==========
    
    private fun startOAuthCredentialsMonitoring() {
        viewModelScope.launch {
            try {
                reactiveOAuthCredentialsUseCase.getCredentialsFlow()
                    .collectLatest { state ->
                        when (state) {
                            is ReactiveOAuthState.Success -> {
                                Log.d(TAG, "OAuth credentials state updated: configured=${state.oauthSettings.isConfigured}")
                                
                                _uiState.value = _uiState.value.copy(
                                    hasOAuthCredentials = state.oauthSettings.isConfigured
                                )
                                
                                // If OAuth credentials are now available and we weren't connected before,
                                // check connection status (user might have just completed setup)
                                if (state.oauthSettings.isConfigured && !_uiState.value.isGitHubConnected) {
                                    Log.d(TAG, "OAuth credentials detected, checking GitHub connection")
                                    checkGitHubConnection()
                                }
                            }
                            is ReactiveOAuthState.Error -> {
                                Log.e(TAG, "OAuth monitoring error: ${state.message}")
                                _uiState.value = _uiState.value.copy(
                                    hasOAuthCredentials = false,
                                    errorMessage = state.message
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in OAuth credentials monitoring", e)
                _uiState.value = _uiState.value.copy(
                    hasOAuthCredentials = false,
                    errorMessage = "Failed to monitor OAuth credentials"
                )
            }
        }
    }

    fun checkCredentialsConfigured() {
        viewModelScope.launch {
            when (val result = checkOAuthCredentialsUseCase.execute()) {
                is OAuthCredentialsCheckResult.NotConfigured -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is OAuthCredentialsCheckResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is OAuthCredentialsCheckResult.Configured -> {
                    // Credentials are configured, no error message needed
                    Log.d(TAG, "OAuth credentials are properly configured")
                }
            }
        }
    }

    // Helper method to get current user ID
    private suspend fun getCurrentUserId(): String? {
        return try {
            val profileResult = profileRepository.getProfile()
            profileResult.getOrNull()?._id
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID", e)
            null
        }
    }

    private fun checkGitHubConnection() {
        viewModelScope.launch {
            when (val result = manageGitHubConnectionUseCase.isConnected()) {
                is GitHubConnectionState.Success -> {
                    _uiState.value = _uiState.value.copy(isGitHubConnected = result.isConnected)
                    
                    if (result.isConnected) {
                        loadGitHubUser()
                        loadRepositories()
                    }
                }
                is GitHubConnectionState.Error -> {
                    Log.e(TAG, "Error checking GitHub connection: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isGitHubConnected = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun launchGitHubOAuth(onIntent: (Intent) -> Unit) {
        viewModelScope.launch {
            try {
                val intent = githubAuthService.createAuthorizationIntent()
                onIntent(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating OAuth intent", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to start OAuth: ${e.message}"
                )
            }
        }
    }

    fun checkConnectionOnly() {
        Log.d(TAG, "checkConnectionOnly() called - OAuth monitoring is reactive")
        checkGitHubConnection()
    }

    fun forceFullRefresh() {
        Log.d(TAG, "forceFullRefresh() called - for edge cases only")
        viewModelScope.launch {
            // Use reactive OAuth credentials for consistency
            when (val result = reactiveOAuthCredentialsUseCase.getCurrentCredentials()) {
                is ReactiveOAuthState.Success -> {
                    _uiState.value = _uiState.value.copy(hasOAuthCredentials = result.oauthSettings.isConfigured)
                }
                is ReactiveOAuthState.Error -> {
                    _uiState.value = _uiState.value.copy(
                        hasOAuthCredentials = false,
                        errorMessage = result.message
                    )
                }
            }
            checkGitHubConnection()
        }
    }

    fun onOAuthSuccess() {
        Log.d(TAG, "onOAuthSuccess() called - checking connection (OAuth is reactive)")
        viewModelScope.launch {
            try {
                // Only check connection since OAuth credentials are monitored reactively
                checkGitHubConnection()
                
                // Then load GitHub data
                loadGitHubData()
                
                Log.d(TAG, "OAuth success handling completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling OAuth success", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load GitHub data after connection: ${e.message}"
                )
            }
        }
    }

    fun loadGitHubData() {
        Log.d(TAG, "loadGitHubData() called")
        viewModelScope.launch {
            try {
                // Get current user ID first
                val userId = getCurrentUserId()
                if (userId != null) {
                    // Only use user-specific token - no global fallback
                    val token = tokenManager.getGitHubTokenForUser(userId)
                    Log.d(TAG, "Retrieved user-specific token for user $userId: ${if (token != null) "exists" else "null"}")
                    if (token != null) {
                        Log.d(TAG, "Connecting to GitHub with user-specific token")
                        connectGitHub(token)
                    } else {
                        Log.e(TAG, "No GitHub token found for user: $userId")
                        _uiState.value = _uiState.value.copy(
                            isGitHubConnected = false,
                            errorMessage = "No GitHub token found"
                        )
                    }
                } else {
                    Log.e(TAG, "Unable to get current user ID")
                    _uiState.value = _uiState.value.copy(
                        isGitHubConnected = false,
                        errorMessage = "Unable to get user information"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading GitHub data", e)
                _uiState.value = _uiState.value.copy(
                    isGitHubConnected = false,
                    errorMessage = "Failed to load GitHub data: ${e.message}"
                )
            }
        }
    }

    fun connectGitHub(accessToken: String) {
        Log.d(TAG, "connectGitHub() called")
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isConnecting = true, errorMessage = null)
                Log.d(TAG, "Set connecting state to true")

                when (val result = manageGitHubConnectionUseCase.connect(accessToken)) {
                    is GitHubConnectionResult.Success -> {
                        Log.d(TAG, "GitHub connection successful")
                        _uiState.value = _uiState.value.copy(
                            isConnecting = false,
                            isGitHubConnected = true,
                            successMessage = "GitHub connected successfully!"
                        )
                        loadGitHubUser()
                        loadRepositories()
                    }
                    is GitHubConnectionResult.Error -> {
                        Log.e(TAG, "GitHub connection failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isConnecting = false,
                            isGitHubConnected = false,
                            errorMessage = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting GitHub", e)
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    isGitHubConnected = false,
                    errorMessage = "Failed to connect GitHub: ${e.message}"
                )
            }
        }
    }

    fun disconnectGitHub() {
        viewModelScope.launch {
            when (val result = manageGitHubConnectionUseCase.disconnect()) {
                is GitHubConnectionResult.Success -> {
                    // Preserve OAuth credentials state, only clear connection-related data
                    _uiState.value = _uiState.value.copy(
                        isGitHubConnected = false,
                        githubUser = null,
                        repositories = emptyList(),
                        isLoadingRepositories = false,
                        isConnecting = false,
                        errorMessage = null,
                        successMessage = "GitHub disconnected successfully!"
                    )
                    
                    Log.d(TAG, "GitHub disconnected, OAuth credentials preserved")
                }
                is GitHubConnectionResult.Error -> {
                    Log.e(TAG, "Error disconnecting GitHub: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun loadGitHubUser() {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    Log.e(TAG, "Unable to get user ID for loading GitHub user")
                    return@launch
                }

                val result = githubRepository.getAuthenticatedUser(userId)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(githubUser = result.getOrNull())
                } else {
                    Log.e(TAG, "Failed to load GitHub user: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading GitHub user", e)
            }
        }
    }

    fun loadRepositories() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingRepositories = true, errorMessage = null)

                val userId = getCurrentUserId()
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingRepositories = false,
                        errorMessage = "Unable to get user information"
                    )
                    return@launch
                }

                val result = githubRepository.getUserRepositories(userId)
                if (result.isSuccess) {
                    val repositories = result.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoadingRepositories = false,
                        repositories = repositories
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingRepositories = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to load repositories"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading repositories", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingRepositories = false,
                    errorMessage = "Failed to load repositories: ${e.message}"
                )
            }
        }
    }

    fun selectRepository(repository: GitHubRepository) {
        _uiState.value = _uiState.value.copy(selectedRepository = repository)
        loadRepositoryCommits(repository.owner.login, repository.name)
        loadWorkflowRuns(repository.owner.login, repository.name)
    }

    private fun loadRepositoryCommits(owner: String, repo: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingCommits = true)

                val userId = getCurrentUserId()
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingCommits = false,
                        errorMessage = "Unable to get user information"
                    )
                    return@launch
                }

                val result = githubRepository.getRepositoryCommits(userId, owner, repo)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingCommits = false,
                        recentCommits = result.getOrNull() ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingCommits = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to load commits"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading commits", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingCommits = false,
                    errorMessage = "Failed to load commits: ${e.message}"
                )
            }
        }
    }

    private fun loadWorkflowRuns(owner: String, repo: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingWorkflows = true)

                val userId = getCurrentUserId()
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingWorkflows = false,
                        errorMessage = "Unable to get user information"
                    )
                    return@launch
                }

                val result = githubRepository.getWorkflowRuns(userId, owner, repo)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingWorkflows = false,
                        workflowRuns = result.getOrNull() ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingWorkflows = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to load workflow runs"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading workflow runs", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingWorkflows = false,
                    errorMessage = "Failed to load workflow runs: ${e.message}"
                )
            }
        }
    }

    fun selectTab(tab: GitHubTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun refreshData() {
        if (_uiState.value.isGitHubConnected) {
            loadRepositories()
            _uiState.value.selectedRepository?.let { repo ->
                loadRepositoryCommits(repo.owner.login, repo.name)
                loadWorkflowRuns(repo.owner.login, repo.name)
            }
        }
    }

    fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    // DEBUG METHOD: Clear OAuth credentials for a specific user (cleanup contaminated data)
    fun clearOAuthCredentialsForUser(userId: String) {
        viewModelScope.launch {
            when (val result = reactiveOAuthCredentialsUseCase.clearCredentialsForUser(userId)) {
                is ReactiveOAuthState.Success -> {
                    Log.d(TAG, "OAuth credentials cleared successfully for user: $userId")
                    
                    // If clearing current user's credentials, the reactive monitoring will handle UI updates
                    val currentUserId = getCurrentUserId()
                    if (currentUserId == userId) {
                        // The reactive monitoring will automatically update the UI state
                        Log.d(TAG, "Current user credentials cleared, reactive monitoring will update UI")
                    }
                }
                is ReactiveOAuthState.Error -> {
                    Log.e(TAG, "Error clearing OAuth credentials for user: $userId - ${result.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
            }
        }
    }

    // DEBUG METHOD: Clear Account B's contaminated OAuth credentials
    fun clearAccountBCredentials() {
        clearOAuthCredentialsForUser("68c0576d42a4ac1b9f169224")
    }
}
