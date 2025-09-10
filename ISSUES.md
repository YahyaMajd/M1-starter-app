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

### Issue 3: Navigation Button Ghost Clicks

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

### Issue 4: Bio Editing Disabled in Manage Profile

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
