package com.cpen321.usermanagement.network.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class GitHubTokenResponse(
    val access_token: String,
    val token_type: String,
    val scope: String
)

interface GitHubOAuthApi {
    @FormUrlEncoded
    @POST("login/oauth/access_token")
    @Headers("Accept: application/json")
    suspend fun exchangeCodeForToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String
    ): GitHubTokenResponse
}

@Singleton
class GitHubAuthService @Inject constructor(
    private val tokenManager: TokenManager,
    private val profileRepository: ProfileRepository
) {
    companion object {
        private const val TAG = "GitHubAuthService"
        private const val REDIRECT_URI = "https://yahyamajd.github.io/m1-oauth-callback/"
        private const val GITHUB_BASE_URL = "https://github.com/"
    }

    private val githubApi: GitHubOAuthApi = Retrofit.Builder()
        .baseUrl(GITHUB_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GitHubOAuthApi::class.java)

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse("https://github.com/login/oauth/authorize"),
        Uri.parse("https://github.com/login/oauth/access_token")
    )

    private fun createAuthorizationService(context: Context): AuthorizationService {
        return AuthorizationService(context)
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

    suspend fun createAuthorizationIntent(): Intent {
        val userId = getCurrentUserId()
            ?: throw GitHubAuthException("Unable to get user information")
            
        // Only use user-specific settings - no global fallback
        val oauthSettings = tokenManager.getGitHubOAuthSettingsForUser(userId)
        Log.d(TAG, "User-specific OAuth settings configured for user $userId: ${oauthSettings.isConfigured}")
        
        if (!oauthSettings.isConfigured) {
            throw GitHubAuthException("GitHub OAuth not configured for this user. Please set up your credentials first.")
        }
        
        val clientId = oauthSettings.clientId
        Log.d(TAG, "CLIENT_ID: $clientId")
        Log.d(TAG, "REDIRECT_URI: $REDIRECT_URI")
        
        val authUrl = "https://github.com/login/oauth/authorize" +
                "?client_id=$clientId" +
                "&redirect_uri=${Uri.encode(REDIRECT_URI)}" +
                "&scope=${Uri.encode("repo user workflow")}" +
                "&response_type=code"
        
        Log.d(TAG, "Authorization URL: $authUrl")
        Log.d(TAG, "Encoded redirect URI: ${Uri.encode(REDIRECT_URI)}")
        
        
        return Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
    }
    
    fun launchAuthFlow(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val intent = createAuthorizationIntent()
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch auth flow", e)
            }
        }
    }

    suspend fun exchangeCodeForToken(authCode: String): String {
        return try {
            Log.d(TAG, "Exchanging code for token")
            Log.d(TAG, "Auth code: $authCode")
            
            val userId = getCurrentUserId()
                ?: throw GitHubAuthException("Unable to get user information")
                
            // Only use user-specific settings - no global fallback
            val oauthSettings = tokenManager.getGitHubOAuthSettingsForUser(userId)
            
            if (!oauthSettings.isConfigured) {
                throw GitHubAuthException("GitHub OAuth not configured for this user")
            }
            
            Log.d(TAG, "Client ID: ${oauthSettings.clientId}")
            Log.d(TAG, "Making token exchange request to GitHub...")
            
            val response = githubApi.exchangeCodeForToken(
                clientId = oauthSettings.clientId,
                clientSecret = oauthSettings.clientSecret,
                code = authCode
            )
            
            Log.d(TAG, "Token exchange successful, access token received")
            response.access_token
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP ${e.code()}: $errorBody")
            Log.e(TAG, "Response headers: ${e.response()?.headers()}")
            throw GitHubAuthException("Token exchange failed: HTTP ${e.code()}")
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.e(TAG, "JSON parsing error - GitHub may have returned non-JSON response", e)
            throw GitHubAuthException("Token exchange failed: Invalid response format from GitHub")
        } catch (e: Exception) {
            Log.e(TAG, "Token exchange error", e)
            throw GitHubAuthException("Token exchange failed: ${e.message}")
        }
    }

    suspend fun handleAuthCallback(intent: Intent): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val uri = intent.data
                if (uri != null) {
                    val authCode = uri.getQueryParameter("code")
                    val error = uri.getQueryParameter("error")
                    
                    when {
                        error != null -> {
                            val errorDescription = uri.getQueryParameter("error_description") ?: "Unknown error"
                            Log.e(TAG, "OAuth error: $error - $errorDescription")
                            continuation.resumeWithException(GitHubAuthException("OAuth failed: $errorDescription"))
                        }
                        authCode != null -> {
                            Log.d(TAG, "Auth code received: $authCode")
                            continuation.resume(authCode)
                        }
                        else -> {
                            Log.e(TAG, "No auth code or error in callback")
                            continuation.resumeWithException(GitHubAuthException("Invalid callback: no code or error"))
                        }
                    }
                } else {
                    Log.e(TAG, "No URI in callback intent")
                    continuation.resumeWithException(GitHubAuthException("Invalid callback: no URI"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling auth callback", e)
                continuation.resumeWithException(GitHubAuthException("Callback handling failed: ${e.message}"))
            }
        }
    }

    suspend fun validateCredentials(clientId: String, clientSecret: String): Boolean {
        return try {
            // Basic validation - check if credentials are not empty and have reasonable format
            if (clientId.isBlank() || clientSecret.isBlank()) {
                Log.w(TAG, "Invalid credentials: empty clientId or clientSecret")
                return false
            }
            
            // GitHub Client ID should be at least 15 characters (typical length is 20)
            if (clientId.length < 15) {
                Log.w(TAG, "Client ID too short (should be at least 15 characters)")
                return false
            }
            
            // GitHub Client Secret should be at least 30 characters (typical length is 40)
            if (clientSecret.length < 30) {
                Log.w(TAG, "Client Secret too short (should be at least 30 characters)")
                return false
            }
            
            // Check for reasonable character set (alphanumeric)
            if (!clientId.matches(Regex("^[a-zA-Z0-9]+$"))) {
                Log.w(TAG, "Client ID contains invalid characters")
                return false
            }
            
            if (!clientSecret.matches(Regex("^[a-zA-Z0-9]+$"))) {
                Log.w(TAG, "Client Secret contains invalid characters")
                return false
            }
            
            Log.d(TAG, "Credentials format validation passed")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Credential validation failed", e)
            false
        }
    }
}

class GitHubAuthException(message: String) : Exception(message)
