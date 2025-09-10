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
 * Use case responsible for monitoring GitHub OAuth credentials for the current user.
 * Encapsulates the business logic of reactive OAuth monitoring.
 */
@Singleton
class MonitorOAuthCredentialsUseCase @Inject constructor(
    private val tokenManager: TokenManager,
    private val profileRepository: ProfileRepository
) {
    
    companion object {
        private const val TAG = "MonitorOAuthCredentialsUseCase"
    }

    /**
     * Starts monitoring OAuth credentials for the current user.
     * @return Flow of OAuth settings that emits whenever credentials change
     */
    suspend fun execute(): Flow<OAuthCredentialsState> = flow {
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                Log.d(TAG, "Starting OAuth credentials monitoring for user: $userId")
                
                // Monitor OAuth settings changes reactively
                tokenManager.getGitHubOAuthSettingsFlowForUser(userId)
                    .collect { oauthSettings ->
                        Log.d(TAG, "OAuth settings changed for user $userId: configured=${oauthSettings.isConfigured}")
                        emit(OAuthCredentialsState.Success(oauthSettings))
                    }
            } else {
                Log.e(TAG, "Unable to start OAuth monitoring: no user ID")
                emit(OAuthCredentialsState.Error("Unable to get user information"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting OAuth credentials monitoring", e)
            emit(OAuthCredentialsState.Error(e.message ?: "Unknown error"))
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
 * Represents the state of OAuth credentials monitoring
 */
sealed class OAuthCredentialsState {
    data class Success(val oauthSettings: GitHubOAuthSettings) : OAuthCredentialsState()
    data class Error(val message: String) : OAuthCredentialsState()
}
