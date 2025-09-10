package com.cpen321.usermanagement.domain.usecase.github

import android.util.Log
import com.cpen321.usermanagement.data.repository.GitHubRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case responsible for managing GitHub connection operations.
 * Encapsulates the business logic of connecting to and disconnecting from GitHub.
 */
@Singleton
class ManageGitHubConnectionUseCase @Inject constructor(
    private val githubRepository: GitHubRepository,
    private val profileRepository: ProfileRepository
) {
    
    companion object {
        private const val TAG = "ManageGitHubConnectionUseCase"
    }

    /**
     * Checks if the current user is connected to GitHub
     */
    suspend fun isConnected(): GitHubConnectionState {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                GitHubConnectionState.Error("Unable to get user information")
            } else {
                val isConnected = githubRepository.isGitHubConnected(userId)
                GitHubConnectionState.Success(isConnected)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking GitHub connection", e)
            GitHubConnectionState.Error("Failed to check GitHub connection: ${e.message}")
        }
    }

    /**
     * Connects the current user to GitHub using an access token
     */
    suspend fun connect(accessToken: String): GitHubConnectionResult {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                GitHubConnectionResult.Error("Unable to get user information")
            } else {
                Log.d(TAG, "Connecting to GitHub for user: $userId")
                val result = githubRepository.connectGitHub(userId, accessToken)
                
                if (result.isSuccess) {
                    Log.d(TAG, "GitHub connection successful for user: $userId")
                    GitHubConnectionResult.Success
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Failed to connect GitHub"
                    Log.e(TAG, "GitHub connection failed for user $userId: $errorMessage")
                    GitHubConnectionResult.Error(errorMessage)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to GitHub", e)
            GitHubConnectionResult.Error("Failed to connect GitHub: ${e.message}")
        }
    }

    /**
     * Disconnects the current user from GitHub
     */
    suspend fun disconnect(): GitHubConnectionResult {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                GitHubConnectionResult.Error("Unable to get user information")
            } else {
                Log.d(TAG, "Disconnecting from GitHub for user: $userId")
                githubRepository.disconnectGitHub(userId)
                Log.d(TAG, "GitHub disconnection successful for user: $userId")
                GitHubConnectionResult.Success
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from GitHub", e)
            GitHubConnectionResult.Error("Failed to disconnect GitHub: ${e.message}")
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
 * Represents the state of GitHub connection check
 */
sealed class GitHubConnectionState {
    data class Success(val isConnected: Boolean) : GitHubConnectionState()
    data class Error(val message: String) : GitHubConnectionState()
}

/**
 * Represents the result of GitHub connection operations
 */
sealed class GitHubConnectionResult {
    object Success : GitHubConnectionResult()
    data class Error(val message: String) : GitHubConnectionResult()
}
