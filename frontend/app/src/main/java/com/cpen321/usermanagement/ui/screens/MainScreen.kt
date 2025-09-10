package com.cpen321.usermanagement.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.ui.components.MessageSnackbar
import com.cpen321.usermanagement.ui.components.MessageSnackbarState
import com.cpen321.usermanagement.ui.components.MainTabRow
import com.cpen321.usermanagement.ui.components.EmbeddedGitHubDashboard
import com.cpen321.usermanagement.ui.components.EmbeddedProfileDashboard
import com.cpen321.usermanagement.ui.viewmodels.MainTab
import com.cpen321.usermanagement.ui.viewmodels.MainUiState
import com.cpen321.usermanagement.ui.viewmodels.MainViewModel
import com.cpen321.usermanagement.ui.viewmodels.ProfileViewModel
import com.cpen321.usermanagement.ui.viewmodels.NavigationViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    navigationViewModel: NavigationViewModel,
    profileViewModel: ProfileViewModel,
    onProfileClick: () -> Unit
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // Load profile data if not already loaded
    LaunchedEffect(Unit) {
        if (profileUiState.user == null) {
            profileViewModel.loadProfile()
        }
    }

    MainContent(
        uiState = uiState,
        userName = profileUiState.user?.name,
        user = profileUiState.user,
        snackBarHostState = snackBarHostState,
        onProfileClick = onProfileClick,
        onTabSelected = mainViewModel::selectTab,
        onGitHubSetupClick = { 
            Log.d("MainScreen", "GitHub setup clicked - navigating to setup")
            navigationViewModel.navigationStateManager.navigateToGitHub() 
        },
        onGitHubViewToggle = mainViewModel::toggleGitHubView,
        onManageProfileClick = { 
            navigationViewModel.navigationStateManager.navigateToManageProfile() 
        },
        onManageHobbiesClick = { 
            navigationViewModel.navigationStateManager.navigateToManageHobbies() 
        },
        onLogoutClick = { 
            navigationViewModel.navigationStateManager.handleLogout() 
        },
        onSuccessMessageShown = mainViewModel::clearSuccessMessage
    )
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    userName: String?,
    user: com.cpen321.usermanagement.data.remote.dto.User?,
    snackBarHostState: SnackbarHostState,
    onProfileClick: () -> Unit,
    onTabSelected: (MainTab) -> Unit,
    onGitHubSetupClick: () -> Unit,
    onGitHubViewToggle: () -> Unit,
    onManageProfileClick: () -> Unit,
    onManageHobbiesClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSuccessMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MainTopBar(userName = userName)
        },
        snackbarHost = {
            MainSnackbarHost(
                hostState = snackBarHostState,
                successMessage = uiState.successMessage,
                onSuccessMessageShown = onSuccessMessageShown
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main tab row
            MainTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = onTabSelected
            )
            
            // Tab content
            when (uiState.selectedTab) {
                MainTab.GITHUB -> {
                    EmbeddedGitHubDashboard(
                        onSetupClick = onGitHubSetupClick,
                        showSummaryView = uiState.showGitHubSummary,
                        onViewFullDashboard = onGitHubViewToggle,
                        modifier = Modifier.weight(1f)
                    )
                }
                MainTab.PROFILE -> {
                    EmbeddedProfileDashboard(
                        user = user,
                        onManageProfileClick = onManageProfileClick,
                        onManageHobbiesClick = onManageHobbiesClick,
                        onFullProfileClick = onProfileClick,
                        onLogoutClick = onLogoutClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    userName: String?,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            val title = if (userName != null) {
                "Welcome, $userName"
            } else {
                "CPEN 321 - GitHub Dashboard"
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    )
}

@Composable
private fun MainSnackbarHost(
    hostState: SnackbarHostState,
    successMessage: String?,
    onSuccessMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    MessageSnackbar(
        hostState = hostState,
        messageState = MessageSnackbarState(
            successMessage = successMessage,
            errorMessage = null,
            onSuccessMessageShown = onSuccessMessageShown,
            onErrorMessageShown = { }
        ),
        modifier = modifier
    )
}