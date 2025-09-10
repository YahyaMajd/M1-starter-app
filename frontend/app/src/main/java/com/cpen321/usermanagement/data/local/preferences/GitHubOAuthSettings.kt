package com.cpen321.usermanagement.data.local.preferences

data class GitHubOAuthSettings(
    val clientId: String = "",
    val clientSecret: String = "",
    val isConfigured: Boolean = false
)

data class GitHubSetupState(
    val step: GitHubSetupStep = GitHubSetupStep.INSTRUCTIONS,
    val clientId: String = "",
    val clientSecret: String = "",
    val isValidating: Boolean = false,
    val errorMessage: String? = null,
    val isComplete: Boolean = false
)

enum class GitHubSetupStep {
    INSTRUCTIONS,    // Show how to create GitHub OAuth app
    CREDENTIALS,     // Input client ID/secret
    VALIDATION,      // Test credentials
    COMPLETE         // Setup successful
}
