package com.cpen321.usermanagement.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.data.remote.dto.User

/**
 * Embedded profile dashboard component for the main screen.
 * Shows essential profile information and quick actions.
 */
@Composable
fun EmbeddedProfileDashboard(
    user: User?,
    onManageProfileClick: () -> Unit,
    onManageHobbiesClick: () -> Unit,
    onFullProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            ProfileSummaryCard(
                user = user,
                onFullProfileClick = onFullProfileClick
            )
        }
        
        item {
            QuickActionsCard(
                onManageProfileClick = onManageProfileClick,
                onManageHobbiesClick = onManageHobbiesClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun ProfileSummaryCard(
    user: User?,
    onFullProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Profile Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(
                    onClick = onFullProfileClick,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "View full profile",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            HorizontalDivider()
            
            if (user != null) {
                ProfileInfoRow(label = "Name", value = user.name)
                ProfileInfoRow(label = "Email", value = user.email)
                
                if (user.hobbies.isNotEmpty()) {
                    ProfileInfoRow(
                        label = "Hobbies",
                        value = user.hobbies.take(3).joinToString(", ") +
                                if (user.hobbies.size > 3) "..." else ""
                    )
                }
            } else {
                Text(
                    text = "Loading profile...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun QuickActionsCard(
    onManageProfileClick: () -> Unit,
    onManageHobbiesClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            HorizontalDivider()
            
            QuickActionButton(
                icon = Icons.Default.Edit,
                title = "Manage Profile",
                subtitle = "Update your personal information",
                onClick = onManageProfileClick
            )
            
            QuickActionButton(
                icon = Icons.Default.Settings,
                title = "Manage Hobbies",
                subtitle = "Add or remove your interests",
                onClick = onManageHobbiesClick
            )
            
            QuickActionButton(
                icon = Icons.Default.ExitToApp,
                title = "Logout",
                subtitle = "Sign out of your account",
                onClick = onLogoutClick,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        colors = if (isDestructive) {
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        } else {
            ButtonDefaults.outlinedButtonColors()
        },
        border = if (isDestructive) {
            ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
            )
        } else {
            ButtonDefaults.outlinedButtonBorder
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDestructive) MaterialTheme.colorScheme.error else LocalContentColor.current
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else LocalContentColor.current
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
