package com.cpen321.usermanagement.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.api.AuthInterface
import com.cpen321.usermanagement.data.remote.api.HobbyInterface
import com.cpen321.usermanagement.data.remote.api.ImageInterface
import com.cpen321.usermanagement.data.remote.api.RetrofitClient
import com.cpen321.usermanagement.data.remote.api.UserInterface
import com.cpen321.usermanagement.data.remote.dto.UpdateProfileRequest
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.repository.GitHubRepository
import com.cpen321.usermanagement.utils.JsonUtils.parseErrorMessage
import com.cpen321.usermanagement.utils.MediaUtils.uriToFile
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject
import javax.inject.Singleton


import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userInterface: UserInterface,
    private val hobbyInterface: HobbyInterface,
    private val authInterface: AuthInterface,
    private val tokenManager: TokenManager,
    private val gitHubRepository: GitHubRepository
) : ProfileRepository {

    private val credentialManager = CredentialManager.create(context)

    companion object {
        private const val TAG = "ProfileRepositoryImpl"
    }

    override suspend fun getProfile(): Result<User> {
        return try {
            val response = userInterface.getProfile("") // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage =
                    parseErrorMessage(errorBodyString, "Failed to fetch user information.")
                Log.e(TAG, "Failed to get profile: $errorMessage")
                // For authentication failures, we need to clear tokens but can't identify specific user
                // Use deprecated global clear as fallback since profile fetch failed
                @Suppress("DEPRECATION")
                tokenManager.clearToken()
                RetrofitClient.setAuthToken(null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while getting profile", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while getting profile", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while getting profile", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while getting profile: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(name: String, bio: String): Result<User> {
        return try {
            val updateRequest = UpdateProfileRequest(name = name, bio = bio)
            val response = userInterface.updateProfile("", updateRequest) // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to update profile.")
                Log.e(TAG, "Failed to update profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while updating profile", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while updating profile", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while updating profile", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while updating profile: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUserHobbies(hobbies: List<String>): Result<User> {
        return try {
            val updateRequest = UpdateProfileRequest(hobbies = hobbies)
            val response = userInterface.updateProfile("", updateRequest) // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to update hobbies.")

                Log.e(TAG, "Failed to update hobbies: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while updating hobbies", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while updating hobbies", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while updating hobbies", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while updating hobbies: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun getAvailableHobbies(): Result<List<String>> {
        return try {
            val response = hobbyInterface.getAvailableHobbies("") // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.hobbies)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to fetch hobbies.")
                Log.e(TAG, "Failed to get available hobbies: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while getting available hobbies", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while getting available hobbies", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while getting available hobbies", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while getting available hobbies: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            // Get user ID before logout for proper cleanup
            val userResult = getProfile()
            val userId = userResult.getOrNull()?._id
            
            // First, try to notify the backend about the logout
            val logoutResult = runCatching {
                val response = authInterface.logout()
                if (!response.isSuccessful) {
                    val errorBodyString = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBodyString, "Failed to log out from server.")
                    Log.w(TAG, "Server logout failed: $errorMessage")
                }
            }

            // Always clear local session regardless of server response
            if (userId != null) {
                tokenManager.clearTokenForUser(userId)
            } else {
                // Fallback to global clear if we can't identify user
                @Suppress("DEPRECATION")
                tokenManager.clearToken()
            }
            RetrofitClient.setAuthToken(null)

            // Clear federated credential state so next time the account chooser shows
            credentialManager.clearCredentialState(ClearCredentialStateRequest())

            // Log any server logout error but still return success since local logout worked
            logoutResult.exceptionOrNull()?.let { error ->
                Log.w(TAG, "Server logout encountered an error, but local logout completed", error)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
            // We still cleared local state above; surface error if you want UI to show a message.
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            // Get user ID before deletion for GitHub cleanup
            val userResult = getProfile()
            val userId = userResult.getOrNull()?._id
            
            // First, delete account from backend
            val response = userInterface.deleteProfile("") // Auth header is handled by interceptor
            if (response.isSuccessful) {
                // Account successfully deleted from backend, now clear local state
                if (userId != null) {
                    tokenManager.clearTokenForUser(userId)
                } else {
                    // Fallback to global clear if we couldn't get user ID
                    @Suppress("DEPRECATION")
                    tokenManager.clearToken()
                }
                RetrofitClient.setAuthToken(null)

                // Clear GitHub connection for this specific user
                if (userId != null) {
                    gitHubRepository.disconnectGitHub(userId)
                    // Also clear OAuth settings for this user
                    tokenManager.clearGitHubOAuthSettingsForUser(userId)
                }

                // Clear federated credential state
                credentialManager.clearCredentialState(ClearCredentialStateRequest())

                Log.i(TAG, "Account deleted successfully")
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to delete account.")
                Log.e(TAG, "Failed to delete account: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Account deletion failed", e)
            Result.failure(e)
        }
    }

}
