package com.cpen321.usermanagement.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.ui.viewmodels.MainTab

@Composable
fun MainTabRow(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        MainTab.values().forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = when (tab) {
                            MainTab.GITHUB -> "GitHub Dashboard"
                            MainTab.PROFILE -> "Profile"
                        },
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            MainTab.GITHUB -> Icons.Default.Home
                            MainTab.PROFILE -> Icons.Default.AccountCircle
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}
