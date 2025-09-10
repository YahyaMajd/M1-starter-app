package com.cpen321.usermanagement.domain.usecase.github

import android.util.Log
import com.cpen321.usermanagement.data.local.preferences.GitHubOAuthSettings
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reactive use case for OAuth credentials that provides a single source of truth.
 * Replaces the need for manual credential checking by providing reactive updates.
 */
@Singleton
class ReactiveOAuthCredentialsUseCase @Inject constructor(
    private val tokenManager: TokenManager,
    private val profileRepository: ProfileRepository
) {
    
    companion object {
        private const val TAG = "ReactiveOAuthCredentialsUseCase"
    }

    /**
     * Provides a reactive stream of OAuth credentials state for the current user.
     * This is the single source of truth for OAuth credentials status.
     */
    suspend fun getCredentialsFlow(): Flow<ReactiveOAuthState> = flow {
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                Log.d(TAG, "Starting reactive OAuth credentials for user: $userId")
                
                // Monitor OAuth settings changes reactively
                tokenManager.getGitHubOAuthSettingsFlowForUser(userId)
                    .collect { oauthSettings ->
                        Log.d(TAG, "OAuth settings updated for user $userId: configured=${oauthSettings.isConfigured}")
                        emit(ReactiveOAuthState.Success(oauthSettings))
                    }
            } else {
                Log.e(TAG, "Unable to get user ID for reactive OAuth monitoring")
                emit(ReactiveOAuthState.Error("Unable to get user information"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in reactive OAuth credentials monitoring", e)
            emit(ReactiveOAuthState.Error("Failed to monitor OAuth credentials: ${e.message}"))
        }
    }

    /**
     * Gets current OAuth credentials state synchronously (for one-time checks)
     */
    suspend fun getCurrentCredentials(): ReactiveOAuthState {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val credentials = tokenManager.getGitHubOAuthSettingsForUser(userId)
                ReactiveOAuthState.Success(credentials)
            } else {
                ReactiveOAuthState.Error("Unable to get user information")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current OAuth credentials", e)
            ReactiveOAuthState.Error("Failed to get OAuth credentials: ${e.message}")
        }
    }

    /**
     * Clears OAuth credentials for a specific user and notifies reactive listeners
     */
    suspend fun clearCredentialsForUser(userId: String): ReactiveOAuthState {
        return try {
            Log.d(TAG, "Clearing OAuth credentials for user: $userId")
            tokenManager.clearGitHubOAuthSettingsForUser(userId)
            Log.d(TAG, "OAuth credentials cleared successfully for user: $userId")
            ReactiveOAuthState.Success(GitHubOAuthSettings()) // Empty settings
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing OAuth credentials for user: $userId", e)
            ReactiveOAuthState.Error("Failed to clear OAuth credentials: ${e.message}")
        }
    }

    private suspend fun getCurrentUserId(): String? {
        return try {
            val profileResult = profileRepository.getProfile()
            profileResult.getOrNull()?._id
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID", e)
            null
        }
    }
}

/**
 * Represents the reactive state of OAuth credentials
 */
sealed class ReactiveOAuthState {
    data class Success(val oauthSettings: GitHubOAuthSettings) : ReactiveOAuthState()
    data class Error(val message: String) : ReactiveOAuthState()
}
