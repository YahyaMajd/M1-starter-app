package com.cpen321.usermanagement.domain.usecase.github

import android.util.Log

/**
 * Utility for consistent error handling across GitHub operations
 */
object GitHubErrorHandler {
    
    /**
     * Handles errors consistently and returns appropriate error messages
     */
    inline fun <T> handleError(
        operation: String,
        tag: String,
        block: () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Log.e(tag, "Error during $operation", e)
            Result.failure(e)
        }
    }
    
    /**
     * Formats error messages consistently
     */
    fun formatErrorMessage(operation: String, exception: Throwable?): String {
        return when (exception) {
            null -> "Failed to $operation"
            else -> "Failed to $operation: ${exception.message}"
        }
    }
}
