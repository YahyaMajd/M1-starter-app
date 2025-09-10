package com.cpen321.usermanagement.domain.usecase.github

import android.util.Log
import com.cpen321.usermanagement.data.local.preferences.GitHubOAuthSettings
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case responsible for checking GitHub OAuth credentials configuration.
 * Encapsulates the business logic of validating OAuth setup for the current user.
 */
@Singleton
class CheckOAuthCredentialsUseCase @Inject constructor(
    private val tokenManager: TokenManager,
    private val profileRepository: ProfileRepository
) {
    
    companion object {
        private const val TAG = "CheckOAuthCredentialsUseCase"
    }

    /**
     * Checks if OAuth credentials are configured for the current user
     */
    suspend fun execute(): OAuthCredentialsCheckResult {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                // Only check user-specific settings - no global fallback
                val credentials = tokenManager.getGitHubOAuthSettingsForUser(userId)
                
                if (!credentials.isConfigured) {
                    Log.d(TAG, "No GitHub OAuth configured for user: $userId")
                    OAuthCredentialsCheckResult.NotConfigured("GitHub OAuth not configured for this user. Please set up your GitHub credentials first.")
                } else {
                    Log.d(TAG, "GitHub OAuth configured for user: $userId")
                    OAuthCredentialsCheckResult.Configured(credentials)
                }
            } else {
                Log.e(TAG, "Unable to get current user ID")
                OAuthCredentialsCheckResult.Error("Unable to get user information")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking OAuth credentials", e)
            OAuthCredentialsCheckResult.Error("Failed to check OAuth credentials: ${e.message}")
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
 * Represents the result of OAuth credentials check
 */
sealed class OAuthCredentialsCheckResult {
    data class Configured(val settings: GitHubOAuthSettings) : OAuthCredentialsCheckResult()
    data class NotConfigured(val message: String) : OAuthCredentialsCheckResult()
    data class Error(val message: String) : OAuthCredentialsCheckResult()
}
