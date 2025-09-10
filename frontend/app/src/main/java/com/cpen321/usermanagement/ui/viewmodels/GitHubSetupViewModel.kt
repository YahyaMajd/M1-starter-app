package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.local.preferences.GitHubOAuthSettings
import com.cpen321.usermanagement.data.local.preferences.GitHubSetupState
import com.cpen321.usermanagement.data.local.preferences.GitHubSetupStep
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.network.auth.GitHubAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GitHubSetupViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val gitHubAuthService: GitHubAuthService,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GitHubSetupState())
    val uiState: StateFlow<GitHubSetupState> = _uiState.asStateFlow()

    init {
        checkExistingCredentials()
    }

    // Helper method to get current user ID
    private suspend fun getCurrentUserId(): String? {
        return try {
            val profileResult = profileRepository.getProfile()
            profileResult.getOrNull()?._id
        } catch (e: Exception) {
            null
        }
    }

    private fun checkExistingCredentials() {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId != null) {
                    val existingSettings = tokenManager.getGitHubOAuthSettingsForUser(userId)
                    if (existingSettings.isConfigured) {
                        // Skip to complete if credentials already exist for this user
                        _uiState.value = _uiState.value.copy(
                            step = GitHubSetupStep.COMPLETE,
                            clientId = existingSettings.clientId,
                            clientSecret = existingSettings.clientSecret,
                            isComplete = true
                        )
                    }
                }
            } catch (e: Exception) {
                // No existing credentials, start from beginning
            }
        }
    }

    fun nextStep() {
        val currentStep = _uiState.value.step
        val nextStep = when (currentStep) {
            GitHubSetupStep.INSTRUCTIONS -> GitHubSetupStep.CREDENTIALS
            GitHubSetupStep.CREDENTIALS -> GitHubSetupStep.VALIDATION
            GitHubSetupStep.VALIDATION -> GitHubSetupStep.COMPLETE
            GitHubSetupStep.COMPLETE -> GitHubSetupStep.COMPLETE
        }
        
        _uiState.value = _uiState.value.copy(step = nextStep)
    }

    fun updateClientId(clientId: String) {
        _uiState.value = _uiState.value.copy(
            clientId = clientId,
            errorMessage = null
        )
    }

    fun updateClientSecret(clientSecret: String) {
        _uiState.value = _uiState.value.copy(
            clientSecret = clientSecret,
            errorMessage = null
        )
    }

    fun validateCredentials() {
        val currentState = _uiState.value
        if (currentState.clientId.isEmpty() || currentState.clientSecret.isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter both Client ID and Client Secret"
            )
            return
        }

        _uiState.value = currentState.copy(
            isValidating = true,
            errorMessage = null,
            step = GitHubSetupStep.VALIDATION
        )

        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    _uiState.value = currentState.copy(
                        isValidating = false,
                        step = GitHubSetupStep.CREDENTIALS,
                        errorMessage = "Unable to get user information"
                    )
                    return@launch
                }

                // Save credentials temporarily for validation
                tokenManager.saveGitHubOAuthSettingsForUser(
                    userId = userId,
                    clientId = currentState.clientId,
                    clientSecret = currentState.clientSecret
                )

                // Validate credentials
                val isValid = gitHubAuthService.validateCredentials(
                    clientId = currentState.clientId,
                    clientSecret = currentState.clientSecret
                )

                if (isValid) {
                    // Credentials are valid, setup complete
                    _uiState.value = currentState.copy(
                        isValidating = false,
                        step = GitHubSetupStep.COMPLETE,
                        isComplete = true,
                        errorMessage = null
                    )
                } else {
                    // Invalid credentials, go back to credentials step
                    tokenManager.clearGitHubOAuthSettingsForUser(userId)
                    _uiState.value = currentState.copy(
                        isValidating = false,
                        step = GitHubSetupStep.CREDENTIALS,
                        errorMessage = "Invalid credentials. Please check your Client ID and Client Secret."
                    )
                }
            } catch (e: Exception) {
                // Validation failed, go back to credentials step
                val userId = getCurrentUserId()
                if (userId != null) {
                    tokenManager.clearGitHubOAuthSettingsForUser(userId)
                }
                _uiState.value = currentState.copy(
                    isValidating = false,
                    step = GitHubSetupStep.CREDENTIALS,
                    errorMessage = "Failed to validate credentials: ${e.message}"
                )
            }
        }
    }

    fun resetSetup() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId != null) {
                tokenManager.clearGitHubOAuthSettingsForUser(userId)
            }
            _uiState.value = GitHubSetupState()
        }
    }
}
