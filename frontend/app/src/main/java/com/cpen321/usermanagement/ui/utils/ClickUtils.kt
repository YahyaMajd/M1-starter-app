package com.cpen321.usermanagement.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * A composable function that provides debounced click handling to prevent rapid successive clicks
 * and ghost clicks during navigation transitions.
 * 
 * @param debounceTime The minimum time in milliseconds between clicks (default: 500ms)
 * @param onClick The action to perform when a valid click occurs
 * @return A debounced click handler function
 */
@Composable
fun debouncedClickable(
    debounceTime: Long = 500L,
    onClick: () -> Unit
): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    
    return {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            onClick()
        }
    }
}
