package com.cpen321.usermanagement.data.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private const val TAG = "TokenManager"
    }

    // User-specific key generators
    private fun userTokenKey(userId: String) = stringPreferencesKey("auth_token_$userId")
    private fun userGitHubTokenKey(userId: String) = stringPreferencesKey("github_token_$userId")
    private fun userGitHubClientIdKey(userId: String) = stringPreferencesKey("github_client_id_$userId")
    private fun userGitHubClientSecretKey(userId: String) = stringPreferencesKey("github_client_secret_$userId")

    // ========== USER-SPECIFIC AUTH TOKEN METHODS ==========
    
    suspend fun saveTokenForUser(userId: String, token: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[userTokenKey(userId)] = token
            }
            Log.d(TAG, "Auth token saved successfully for user: $userId")
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while saving token for user", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to save token for user", e)
            throw e
        }
    }

    suspend fun getTokenForUser(userId: String): String? {
        return try {
            val token = context.dataStore.data.first()[userTokenKey(userId)]
            token
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while getting token for user", e)
            null
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to get token for user", e)
            null
        }
    }

    fun getTokenFlowForUser(userId: String): Flow<String?> {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[userTokenKey(userId)]
            }
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while getting token flow for user", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to get token flow for user", e)
            throw e
        }
    }

    suspend fun clearTokenForUser(userId: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(userTokenKey(userId))
            }
            Log.d(TAG, "Auth token cleared successfully for user: $userId")
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while clearing token for user", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to clear token for user", e)
            throw e
        }
    }

    // ========== USER-SPECIFIC GITHUB TOKEN METHODS ==========

    // ========== USER-SPECIFIC GITHUB TOKEN METHODS ==========

    suspend fun saveGitHubTokenForUser(userId: String, token: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[userGitHubTokenKey(userId)] = token
            }
            Log.d(TAG, "GitHub token saved successfully for user: $userId")
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while saving GitHub token for user", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to save GitHub token for user", e)
            throw e
        }
    }

    suspend fun getGitHubTokenForUser(userId: String): String? {
        return try {
            val token = context.dataStore.data.first()[userGitHubTokenKey(userId)]
            token
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while getting GitHub token for user", e)
            null
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to get GitHub token for user", e)
            null
        }
    }

    suspend fun clearGitHubTokenForUser(userId: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(userGitHubTokenKey(userId))
            }
            Log.d(TAG, "GitHub token cleared successfully for user: $userId")
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while clearing GitHub token for user", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to clear GitHub token for user", e)
            throw e
        }
    }

    // ========== USER-SPECIFIC GITHUB OAUTH SETTINGS METHODS ==========

    suspend fun saveGitHubOAuthSettingsForUser(userId: String, clientId: String, clientSecret: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[userGitHubClientIdKey(userId)] = clientId
                preferences[userGitHubClientSecretKey(userId)] = clientSecret
            }
            Log.d(TAG, "GitHub OAuth settings saved successfully for user: $userId")
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while saving GitHub OAuth settings for user", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to save GitHub OAuth settings for user", e)
            throw e
        }
    }

    suspend fun getGitHubOAuthSettingsForUser(userId: String): GitHubOAuthSettings {
        return try {
            val preferences = context.dataStore.data.first()
            val clientIdKey = userGitHubClientIdKey(userId)
            val clientSecretKey = userGitHubClientSecretKey(userId)
            val clientId = preferences[clientIdKey] ?: ""
            val clientSecret = preferences[clientSecretKey] ?: ""
            
            Log.d(TAG, "=== TOKEN MANAGER DEBUG ===")
            Log.d(TAG, "Getting OAuth settings for user: $userId")
            Log.d(TAG, "Client ID key: ${clientIdKey.name}")
            Log.d(TAG, "Client Secret key: ${clientSecretKey.name}")
            Log.d(TAG, "Client ID value: ${if (clientId.isNotEmpty()) "[PRESENT: ${clientId.take(10)}...]" else "[EMPTY]"}")
            Log.d(TAG, "Client Secret value: ${if (clientSecret.isNotEmpty()) "[PRESENT: ${clientSecret.take(10)}...]" else "[EMPTY]"}")
            Log.d(TAG, "All preferences keys: ${preferences.asMap().keys.map { it.name }}")
            Log.d(TAG, "=== END TOKEN MANAGER DEBUG ===")
            
            GitHubOAuthSettings(
                clientId = clientId,
                clientSecret = clientSecret,
                isConfigured = clientId.isNotEmpty() && clientSecret.isNotEmpty()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error while getting GitHub OAuth settings for user: $userId", e)
            GitHubOAuthSettings()
        }
    }

    // ========== REACTIVE OAUTH SETTINGS FLOW ==========
    
    fun getGitHubOAuthSettingsFlowForUser(userId: String): Flow<GitHubOAuthSettings> {
        return try {
            context.dataStore.data.map { preferences ->
                val clientIdKey = userGitHubClientIdKey(userId)
                val clientSecretKey = userGitHubClientSecretKey(userId)
                val clientId = preferences[clientIdKey] ?: ""
                val clientSecret = preferences[clientSecretKey] ?: ""
                
                GitHubOAuthSettings(
                    clientId = clientId,
                    clientSecret = clientSecret,
                    isConfigured = clientId.isNotEmpty() && clientSecret.isNotEmpty()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating OAuth settings flow for user: $userId", e)
            kotlinx.coroutines.flow.flowOf(GitHubOAuthSettings())
        }
    }

    suspend fun clearGitHubOAuthSettingsForUser(userId: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(userGitHubClientIdKey(userId))
                preferences.remove(userGitHubClientSecretKey(userId))
            }
            Log.d(TAG, "GitHub OAuth settings cleared successfully for user: $userId")
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while clearing GitHub OAuth settings for user", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to clear GitHub OAuth settings for user", e)
            throw e
        }
    }

    // ========== CLEANUP METHOD FOR ALL USER DATA ==========

    suspend fun clearAllDataForUser(userId: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(userTokenKey(userId))
                preferences.remove(userGitHubTokenKey(userId))
                preferences.remove(userGitHubClientIdKey(userId))
                preferences.remove(userGitHubClientSecretKey(userId))
            }
            Log.d(TAG, "All data cleared successfully for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all data for user: $userId", e)
            throw e
        }
    }

    // ========== LEGACY GLOBAL METHODS (DEPRECATED - FOR BACKWARD COMPATIBILITY) ==========
    
    private val tokenKey = stringPreferencesKey("auth_token")
    private val githubTokenKey = stringPreferencesKey("github_token")

    @Deprecated("Use saveTokenForUser instead")
    suspend fun saveToken(token: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[tokenKey] = token
            }
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while saving token", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to save token", e)
            throw e
        }
    }

    @Deprecated("Use getTokenForUser instead")
    fun getToken(): Flow<String?> {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[tokenKey]
            }
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while getting token flow", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to get token flow", e)
            throw e
        }
    }

    @Deprecated("Use getTokenForUser instead")
    suspend fun getTokenSync(): String? {
        return try {
            val token = context.dataStore.data.first()[tokenKey]
            token
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while getting token synchronously", e)
            null
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to get token synchronously", e)
            null
        }
    }

    @Deprecated("Use clearTokenForUser instead")
    suspend fun clearToken() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(tokenKey)
            }
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while clearing token", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to clear token", e)
            throw e
        }
    }
}
