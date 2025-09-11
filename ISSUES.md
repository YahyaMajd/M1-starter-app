# M1

## List of issues

### Issue 1: Missing Logout Functionality

**Description**: The application lacked a proper logout mechanism. Users could sign in with Google OAuth but had no way to sign out from the application. When users attempted to switch accounts, the app would retain the previous user's session data, causing confusion and potential security concerns.

**How it was fixed?**: Implemented a complete logout flow including:
* Added `POST /auth/logout` endpoint in the backend with proper authentication middleware
* Created logout method in `AuthInterface.kt` for frontend API calls
* Enhanced `ProfileRepositoryImpl.logout()` to call backend endpoint and clear local tokens
* Added `handleLogout()` method in `AuthViewModel` to reset authentication state
* Updated `ProfileViewModel.handleLogoutAction()` to clear user profile data
* Coordinated logout flow in `Navigation.kt` to properly clear all ViewModels and trigger navigation

### Issue 2: Deleted Account can still sign in - High (Functional + Security)

**Description**: Clicking the 'Delete Account' button logs out the user but the account is still present. Users can sign back in with the same credentials after "deleting" their account, creating a serious security vulnerability where user data is not actually removed.

**How it was fixed?**: 
1. **Added backend API call to frontend**: Implemented `deleteProfile()` method in `UserInterface.kt` to call the existing `DELETE /user/profile` backend endpoint
2. **Enhanced ProfileRepository**: Added `deleteAccount()` method to `ProfileRepository` interface and implemented it in `ProfileRepositoryImpl.kt` to properly call the backend deletion API
3. **Updated AuthRepository**: Added `deleteAccount()` method to `AuthRepository` interface and implemented it in `AuthRepositoryImpl.kt` to handle account deletion through proper API calls
4. **Fixed AuthViewModel**: Modified `handleAccountDeletion()` in `AuthViewModel.kt` to call `authRepository.deleteAccount()` instead of only clearing local tokens
5. **Comprehensive cleanup**: The fix ensures that account deletion now:
   - Calls the backend `DELETE /user/profile` endpoint to remove user data from database
   - Deletes all associated user images via `MediaService.deleteAllUserImages()`
   - Clears local authentication tokens and state
   - Clears federated credential state for Google OAuth
   - Provides proper error handling and logging

The root cause was that the frontend `handleAccountDeletion()` method only cleared local authentication state without calling the backend API, leaving user data intact in the database.

### Issue 3: Profile Photo Upload Not Working

**Description**: Users could successfully take photos using the camera or select images from gallery through the image picker, but the photos were not actually being saved to the backend server. The photo would appear temporarily in the UI, but would disappear after app restart since it was never uploaded to the server. Users would see a "Profile picture updated successfully!" message, but this was misleading as no actual upload occurred.

**Root Cause Analysis**: The `ProfileViewModel.uploadProfilePicture()` method was only updating the local UI state with the image URI instead of performing an actual upload to the backend server. The implementation was:

```kotlin
fun uploadProfilePicture(pictureUri: Uri) {
    viewModelScope.launch {
        val currentUser = _uiState.value.user ?: return@launch
        val updatedUser = currentUser.copy(profilePicture = pictureUri.toString())
        _uiState.value = _uiState.value.copy(isLoadingPhoto = false, user= updatedUser, successMessage = "Profile picture updated successfully!")
    }
}
```

This was essentially a "fake" implementation that only showed the image locally using the temporary URI.

**How it was fixed?**: 

**Backend Verification**: The backend already had complete upload functionality:
- ✅ `MediaController.uploadImage()` - Handles multipart file upload
- ✅ `MediaService.saveImage()` - Saves images to filesystem with proper naming
- ✅ `storage.ts` - Multer configuration for image processing  
- ✅ `/api/media/upload` endpoint - Ready to receive images
- ✅ `ImageInterface.kt` - Frontend interface already defined

**Frontend Implementation**:

1. **Added Repository Method**: Added `uploadProfilePicture(pictureUri: Uri): Result<String>` to `ProfileRepository` interface

2. **Implemented Upload Logic**: Created complete implementation in `ProfileRepositoryImpl.kt`:
   - Convert URI to File using existing `MediaUtils.uriToFile()`
   - Create multipart request body with proper content type
   - Call `imageInterface.uploadPicture()` to upload to backend
   - Handle response and return image URL from server

3. **Fixed Dependency Injection**: 
   - Added `ImageInterface` parameter to `ProfileRepositoryImpl` constructor
   - Updated `NetworkModule.kt` to provide `ImageInterface` via `provideImageInterface()`
   - Resolved duplicate binding conflicts in Hilt configuration

4. **Enhanced ViewModel**: Completely rewrote `uploadProfilePicture()` method to:
   - Set proper loading state during upload
   - Call repository upload method 
   - Update user profile with returned image URL from server
   - Handle upload errors with appropriate user feedback
   - Only show success message when upload actually completes

5. **Updated Profile Update**: Enhanced repository `updateProfile()` method to accept optional `profilePicture` parameter for updating user profile with new image URL

**Technical Changes**:
- **ProfileRepository.kt**: Added `uploadProfilePicture()` interface method
- **ProfileRepositoryImpl.kt**: Implemented actual upload with multipart requests
- **ProfileViewModel.kt**: Replaced fake local update with real backend upload
- **NetworkModule.kt**: Fixed duplicate `ImageInterface` providers and dependency injection
- **UpdateProfileRequest.kt**: Already supported `profilePicture` field

**Verification**: Photo upload now works end-to-end:
- ✅ User takes photo → Image picker captures URI
- ✅ Upload initiated → Loading state shown to user  
- ✅ File converted → `MediaUtils.uriToFile()` processes URI
- ✅ Multipart upload → Posted to `/api/media/upload`
- ✅ Backend saves → File stored with user ID naming
- ✅ URL returned → Backend responds with saved image path
- ✅ Profile updated → User record updated with new image URL
- ✅ UI refreshed → AsyncImage loads from server URL
- ✅ Persistence → Photo survives app restarts

### Issue 4: Navigation Button Ghost Clicks

**Description**: When navigating from the profile section back to the main screen, rapidly clicking on the position where profile buttons (like "Manage Hobbies") were located can still trigger navigation to those screens, even though the user is now on the main screen. This suggests that the previous screen's click handlers are still active or there's a timing issue with UI composition/navigation state updates.

**How it was fixed?**: 
1. **Local Navigation State**: Added `isNavigating` state variable in ProfileScreen to track when navigation has been initiated
2. **Immediate State Change**: Set `isNavigating = true` as soon as any navigation action is triggered, before calling the actual navigation function
3. **Interaction Blocking Overlay**: Added a transparent, full-screen clickable overlay that appears when `isNavigating = true` to consume all touch events
4. **Button Disabling**: Disabled all buttons when `isNavigating = true` as an additional layer of protection
5. **Combined Click Debouncing**: Kept the debouncing utility for general click protection across all buttons

**Technical Implementation**:
- `ProfileScreen.kt`: Added local `isNavigating` state that gets set immediately when any navigation action starts
- **Transparent Overlay**: Used `Box` with `clickable` modifier to intercept all interactions during navigation
- **Button State Control**: Passed `isNavigating` state to disable all ProfileScreen buttons during transitions
- **Multi-layered Protection**: Combined local state management, UI blocking, and click debouncing

**Root Cause**: Compose Navigation keeps previous screen composables active during transition animations, allowing their click handlers to remain responsive during the navigation delay.

**Solution Impact**: 
- **Immediate Response**: Navigation state changes instantly when buttons are pressed
- **Complete Blocking**: Transparent overlay prevents any ghost clicks from reaching underlying components
- **Visual Feedback**: Buttons become disabled, providing clear UI feedback
- **Universal Protection**: Works for all navigation actions from ProfileScreen

### Issue 5: Bio Editing Disabled in Manage Profile

**Description**: Users could only edit their bio during the initial sign-in/profile completion flow. When accessing the "Manage Profile" section later, the bio field was read-only, preventing users from updating their bio after the initial setup. The text field appeared editable but didn't respond to user input.

**How it was fixed?**: 
**Root Cause**: The bio `OutlinedTextField` in `ManageProfileScreen.kt` had `readOnly = true` property set, which completely disabled editing functionality.

**Solution**: Removed the `readOnly = true` property from the bio field in the `ProfileFields` composable within `ManageProfileScreen.kt`. Also removed the unnecessary `Row` wrapper with `focusProperties { canFocus = false }` that was preventing focus.

**Technical Changes**:
- **File**: `ManageProfileScreen.kt` - `ProfileFields` composable
- **Before**: Bio field had `readOnly = true` preventing any editing
- **After**: Bio field is fully editable, consistent with `ProfileCompletionScreen.kt`
- **Maintained**: All existing functionality (change handlers, validation, save logic) was already properly implemented

**Verification**: The bio editing functionality in ManageProfileScreen now matches the behavior in ProfileCompletionScreen, allowing users to:
- Edit bio text in the manage profile section
- See real-time changes in the text field  
- Save changes using the existing save button
- Receive proper success/error feedback

...
