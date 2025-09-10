package com.cpen321.usermanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cpen321.usermanagement.data.local.preferences.GitHubSetupStep
import com.cpen321.usermanagement.ui.viewmodels.GitHubSetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubSetupScreen(
    onBackClick: () -> Unit,
    onSetupComplete: () -> Unit,
    viewModel: GitHubSetupViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navigate to GitHub screen when setup is complete
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onSetupComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GitHub Setup") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState.step) {
                GitHubSetupStep.INSTRUCTIONS -> InstructionsStep(
                    onNext = { viewModel.nextStep() }
                )
                GitHubSetupStep.CREDENTIALS -> CredentialsStep(
                    clientId = uiState.clientId,
                    clientSecret = uiState.clientSecret,
                    onClientIdChange = viewModel::updateClientId,
                    onClientSecretChange = viewModel::updateClientSecret,
                    onValidate = viewModel::validateCredentials,
                    isValidating = uiState.isValidating,
                    errorMessage = uiState.errorMessage
                )
                GitHubSetupStep.VALIDATION -> ValidationStep()
                GitHubSetupStep.COMPLETE -> CompleteStep()
            }
        }
    }
}

@Composable
private fun InstructionsStep(
    onNext: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Set up GitHub Integration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "To connect to GitHub, you need to create a GitHub OAuth application. Follow these steps:",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Steps to create GitHub OAuth App:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                InstructionStep(
                    number = "1",
                    text = "Go to GitHub Settings → Developer settings → OAuth Apps"
                )
                
                InstructionStep(
                    number = "2", 
                    text = "Click 'New OAuth App'"
                )
                
                InstructionStep(
                    number = "3",
                    text = "Fill in the form:\n• Application name: Your app name\n• Homepage URL: Any URL (e.g., https://github.com)\n• Authorization callback URL: https://yahyamajd.github.io/m1-oauth-callback/"
                )
                
                InstructionStep(
                    number = "4",
                    text = "Click 'Register application'"
                )
                
                InstructionStep(
                    number = "5",
                    text = "Copy the Client ID and generate/copy the Client Secret"
                )
            }
        }
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I've created my GitHub OAuth App")
        }
    }
}

@Composable
private fun InstructionStep(
    number: String,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CredentialsStep(
    clientId: String,
    clientSecret: String,
    onClientIdChange: (String) -> Unit,
    onClientSecretChange: (String) -> Unit,
    onValidate: () -> Unit,
    isValidating: Boolean,
    errorMessage: String?
) {
    var showClientSecret by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter OAuth Credentials",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Enter the Client ID and Client Secret from your GitHub OAuth app:",
            style = MaterialTheme.typography.bodyLarge
        )
        
        OutlinedTextField(
            value = clientId,
            onValueChange = onClientIdChange,
            label = { Text("Client ID") },
            placeholder = { Text("Ov23li...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        
        OutlinedTextField(
            value = clientSecret,
            onValueChange = onClientSecretChange,
            label = { Text("Client Secret") },
            placeholder = { Text("66e182...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showClientSecret) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showClientSecret = !showClientSecret }) {
                    Text(if (showClientSecret) "Hide" else "Show")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )
        
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Button(
            onClick = onValidate,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isValidating && clientId.isNotEmpty() && clientSecret.isNotEmpty()
        ) {
            if (isValidating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isValidating) "Validating..." else "Validate & Save")
        }
    }
}

@Composable
private fun ValidationStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()
        
        Text(
            text = "Validating GitHub credentials...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "This may take a few seconds",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CompleteStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Complete",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        
        Text(
            text = "Setup Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your GitHub integration is now configured. You can connect to your GitHub account.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
