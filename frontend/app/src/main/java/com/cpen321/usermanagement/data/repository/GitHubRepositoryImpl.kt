package com.cpen321.usermanagement.data.repository

import android.content.Context
import android.util.Log
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.api.GitHubApiInterface
import com.cpen321.usermanagement.data.remote.dto.GitHubCommit
import com.cpen321.usermanagement.data.remote.dto.GitHubRepository as GitHubRepositoryModel
import com.cpen321.usermanagement.data.remote.dto.GitHubUser
import com.cpen321.usermanagement.data.remote.dto.GitHubWorkflowRun
import com.cpen321.usermanagement.utils.JsonUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager
) : GitHubRepository {

    companion object {
        private const val TAG = "GitHubRepositoryImpl"
        private const val GITHUB_BASE_URL = "https://api.github.com/"
        private const val GITHUB_TOKEN_KEY = "github_access_token"
    }

    private val githubApi: GitHubApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApiInterface::class.java)
    }

    override suspend fun getUserRepositories(userId: String): Result<List<GitHubRepositoryModel>> {
        return try {
            val token = getGitHubToken(userId)
            if (token == null) {
                return Result.failure(Exception("GitHub not connected"))
            }

            val response = githubApi.getUserRepositories("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = JsonUtils.parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to fetch repositories"
                )
                Log.e(TAG, "Failed to fetch repositories: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching repositories", e)
            Result.failure(e)
        }
    }

    override suspend fun getRepository(userId: String, owner: String, repo: String): Result<GitHubRepositoryModel> {
        return try {
            val token = getGitHubToken(userId)
            if (token == null) {
                return Result.failure(Exception("GitHub not connected"))
            }

            val response = githubApi.getRepository("Bearer $token", owner, repo)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = JsonUtils.parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to fetch repository"
                )
                Log.e(TAG, "Failed to fetch repository: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching repository", e)
            Result.failure(e)
        }
    }

    override suspend fun getRepositoryCommits(userId: String, owner: String, repo: String): Result<List<GitHubCommit>> {
        return try {
            val token = getGitHubToken(userId)
            if (token == null) {
                return Result.failure(Exception("GitHub not connected"))
            }

            val response = githubApi.getRepositoryCommits("Bearer $token", owner, repo)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = JsonUtils.parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to fetch commits"
                )
                Log.e(TAG, "Failed to fetch commits: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching commits", e)
            Result.failure(e)
        }
    }

    override suspend fun getWorkflowRuns(userId: String, owner: String, repo: String): Result<List<GitHubWorkflowRun>> {
        return try {
            val token = getGitHubToken(userId)
            if (token == null) {
                return Result.failure(Exception("GitHub not connected"))
            }

            val response = githubApi.getWorkflowRuns("Bearer $token", owner, repo)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.workflowRuns)
            } else {
                val errorMessage = JsonUtils.parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to fetch workflow runs"
                )
                Log.e(TAG, "Failed to fetch workflow runs: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching workflow runs", e)
            Result.failure(e)
        }
    }

    override suspend fun getAuthenticatedUser(userId: String): Result<GitHubUser> {
        return try {
            val token = getGitHubToken(userId)
            if (token == null) {
                return Result.failure(Exception("GitHub not connected"))
            }

            val response = githubApi.getAuthenticatedUser("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = JsonUtils.parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to fetch user info"
                )
                Log.e(TAG, "Failed to fetch user info: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user info", e)
            Result.failure(e)
        }
    }

    override suspend fun connectGitHub(userId: String, accessToken: String): Result<Boolean> {
        return try {
            // Store the token for this user
            tokenManager.saveGitHubTokenForUser(userId, accessToken)
            
            // Verify the token works by getting user info
            val userResult = getAuthenticatedUser(userId)
            if (userResult.isSuccess) {
                Log.d(TAG, "GitHub connected successfully for user: $userId")
                Result.success(true)
            } else {
                // Token is invalid, remove it
                tokenManager.clearGitHubTokenForUser(userId)
                Result.failure(userResult.exceptionOrNull() ?: Exception("Invalid GitHub token"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting GitHub for user: $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun isGitHubConnected(userId: String): Boolean {
        return getGitHubToken(userId) != null
    }

    override suspend fun disconnectGitHub(userId: String) {
        tokenManager.clearGitHubTokenForUser(userId)
        Log.d(TAG, "GitHub disconnected for user: $userId")
    }

    private suspend fun getGitHubToken(userId: String): String? {
        return try {
            tokenManager.getGitHubTokenForUser(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting GitHub token for user: $userId", e)
            null
        }
    }
}
