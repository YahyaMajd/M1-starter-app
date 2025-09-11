# M1

## New Feature

**Name:** GitHub Repository Tracker

**Short description:** A mobile GitHub integration that allows users to connect their GitHub accounts via OAuth authentication and monitor their repositories, commits, and workflow status in a native Android interface with real-time data visualization.

**Location and code:** 

### Frontend Implementation (Android/Kotlin):
- **`GitHubScreen.kt`** - Main dashboard with tabbed interface for repositories, commits, and workflows
- **`GitHubComponents.kt`** - Reusable UI components for repository cards and commit displays  
- **`GitHubViewModel.kt`** - State management for GitHub data and authentication flow
- **`GitHubApiInterface.kt`** - Retrofit interface defining GitHub REST API endpoints
- **`GitHubRepository.kt` & `GitHubRepositoryImpl.kt`** - Data layer with OAuth token management
- **`TokenManager.kt`** - Secure storage for GitHub OAuth tokens
- **`NetworkModule.kt`** - Dependency injection for GitHub API services

```
