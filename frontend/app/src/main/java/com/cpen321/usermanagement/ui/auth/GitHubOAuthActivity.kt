package com.cpen321.usermanagement.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.network.auth.GitHubAuthService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GitHubOAuthActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "GitHubOAuthActivity"
    }
    
    @Inject
    lateinit var authService: GitHubAuthService
    
    @Inject
    lateinit var tokenManager: TokenManager
    
    @Inject
    lateinit var profileRepository: ProfileRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "GitHubOAuthActivity started - onCreate")
        Log.d(TAG, "Intent: ${intent}")
        Log.d(TAG, "Intent action: ${intent.action}")
        Log.d(TAG, "Intent data: ${intent.data}")
        Log.d(TAG, "Intent categories: ${intent.categories}")
        Log.d(TAG, "Intent extras: ${intent.extras}")
        
        handleAuthorizationResponse(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "GitHubOAuthActivity onNewIntent")
        Log.d(TAG, "New intent: ${intent}")
        Log.d(TAG, "New intent action: ${intent.action}")
        Log.d(TAG, "New intent data: ${intent.data}")
        handleAuthorizationResponse(intent)
    }
    
    private fun handleAuthorizationResponse(intent: Intent) {
        Log.d(TAG, "Handling authorization response")
        Log.d(TAG, "Intent data: ${intent.data}")
        Log.d(TAG, "Intent action: ${intent.action}")
        Log.d(TAG, "Intent extras: ${intent.extras}")
        
        // Get the URI from the intent
        val uri = intent.data
        
        if (uri != null) {
            Log.d(TAG, "Processing URI: $uri")
            Log.d(TAG, "URI scheme: ${uri.scheme}")
            Log.d(TAG, "URI host: ${uri.host}")
            Log.d(TAG, "URI path: ${uri.path}")
            Log.d(TAG, "URI query: ${uri.query}")
            
            // Extract authorization code from the URI
            val authCode = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            val errorDescription = uri.getQueryParameter("error_description")
            
            Log.d(TAG, "Auth code: $authCode")
            Log.d(TAG, "Error: $error")
            Log.d(TAG, "Error description: $errorDescription")
            
            lifecycleScope.launch {
                try {
                    if (authCode != null) {
                        Log.d(TAG, "Exchanging code for token")
                        val token = authService.exchangeCodeForToken(authCode)
                        
                        Log.d(TAG, "Token received, getting user ID...")
                        // Get current user ID
                        val userResult = profileRepository.getProfile()
                        val userId = userResult.getOrNull()?._id
                        
                        if (userId != null) {
                            Log.d(TAG, "Storing token for user: $userId")
                            // Store the token for the specific user
                            tokenManager.saveGitHubTokenForUser(userId, token)
                            Log.d(TAG, "Token stored successfully")
                            setResult(RESULT_OK)
                        } else {
                            Log.e(TAG, "Unable to get current user ID")
                            setResult(RESULT_CANCELED, Intent().apply {
                                putExtra("error", "Unable to get user information")
                            })
                        }
                        finish()
                    } else if (error != null) {
                        Log.e(TAG, "OAuth error: $error")
                        // Handle authorization error
                        setResult(RESULT_CANCELED, Intent().apply {
                            putExtra("error", "OAuth error: $error")
                        })
                        finish()
                    } else {
                        Log.e(TAG, "No authorization code or error found in URI")
                        setResult(RESULT_CANCELED, Intent().apply {
                            putExtra("error", "No authorization code received")
                        })
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Token exchange failed", e)
                    // Handle token exchange error
                    setResult(RESULT_CANCELED, Intent().apply {
                        putExtra("error", e.message ?: "Token exchange failed")
                    })
                    finish()
                }
            }
        } else {
            Log.e(TAG, "No URI data in intent")
            setResult(RESULT_CANCELED, Intent().apply {
                putExtra("error", "No URI data received")
            })
            finish()
        }
    }
}
