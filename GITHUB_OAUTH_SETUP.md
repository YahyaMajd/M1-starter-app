# GitHub OAuth Setup Guide

## Phase 1 - Developer Productivity Dashboard: GitHub OAuth Integration

### Overview
This guide walks through setting up GitHub OAuth authentication for the Developer Productivity Dashboard Phase 1 implementation.

### Prerequisites
- GitHub account
- Android Studio with the project open
- Developer mode enabled on Android device/emulator

### Step 1: Register GitHub OAuth App

1. Go to GitHub Settings: https://github.com/settings/applications/new
2. Fill out the form:
   - **Application name**: `M1 Starter App`
   - **Homepage URL**: `https://your-domain.com` (or use a placeholder like `https://github.com/yourusername/M1-starter-app`)
   - **Application description**: `Developer Productivity Dashboard for M1 Starter App`
   - **Authorization callback URL**: `m1starterapp://github/callback`

3. Click "Register application"
4. Copy the **Client ID** and **Client Secret**

### Step 2: Configure OAuth Credentials

1. Open `GitHubAuthService.kt` located at:
   ```
   frontend/app/src/main/java/com/cpen321/usermanagement/network/auth/GitHubAuthService.kt
   ```

2. Replace the placeholder values:
   ```kotlin
   private val CLIENT_ID = "your_actual_github_client_id"
   private val CLIENT_SECRET = "your_actual_github_client_secret"
   ```

### Step 3: Test OAuth Flow

1. Build and run the app
2. Navigate to the Developer Dashboard
3. Tap "Connect GitHub"
4. Browser should open with GitHub authorization page
5. Authorize the app
6. App should return with GitHub connection successful

### OAuth Flow Explanation

1. **Authorization Request**: App launches browser with GitHub auth URL
2. **User Authorization**: User logs in and authorizes the app
3. **Callback**: GitHub redirects to `m1starterapp://github/callback` with auth code
4. **Token Exchange**: App exchanges auth code for access token
5. **API Access**: App uses token to make GitHub API calls

### Security Notes

- **Never commit real CLIENT_SECRET to version control**
- Consider using build variants or environment variables for production
- The current implementation stores the CLIENT_SECRET in the app - for production, use a backend service

### Granted Permissions

The OAuth app requests these scopes:
- `repo`: Access to public and private repositories
- `user`: Access to user profile information  
- `workflow`: Access to GitHub Actions workflows

### Troubleshooting

**OAuth callback not working:**
- Verify the callback URL matches exactly: `m1starterapp://github/callback`
- Check AndroidManifest.xml has the correct intent filter

**Authorization fails:**
- Verify CLIENT_ID and CLIENT_SECRET are correct
- Check GitHub app is not suspended
- Ensure device has internet connection

**API calls fail after OAuth:**
- Check token is being stored properly
- Verify API endpoints are correct
- Check network connectivity

### Files Modified for OAuth Integration

1. **`build.gradle.kts`**: Added OAuth dependencies
2. **`GitHubAuthService.kt`**: OAuth service implementation
3. **`GitHubOAuthActivity.kt`**: Handles OAuth callback
4. **`AndroidManifest.xml`**: OAuth callback intent filter
5. **`NetworkModule.kt`**: Dependency injection for auth service
6. **`GitHubViewModel.kt`**: OAuth integration methods
7. **`GitHubScreen.kt`**: OAuth flow UI integration

### Next Steps for Phase 1 Completion

After OAuth setup:
1. ✅ GitHub API integration - **Complete**
2. ✅ OAuth authentication - **Complete** 
3. 🔄 Enhanced UX features (error handling, loading states)
4. 🔄 Data persistence (offline capability)
5. 🔄 Performance optimizations

### Production Considerations

For production deployment:
- Move CLIENT_SECRET to backend service
- Implement proper token refresh mechanism
- Add rate limiting and error retry logic
- Implement proper logging and analytics
- Add user consent and privacy controls
