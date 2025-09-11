package com.cpen321.usermanagement.data.repository

import android.net.Uri
import com.cpen321.usermanagement.data.remote.dto.User

interface ProfileRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(name: String, bio: String, profilePicture: String ? = null): Result<User>
    suspend fun updateUserHobbies(hobbies: List<String>): Result<User>
    suspend fun uploadProfilePicture(pictureUri: Uri): Result<String>
    suspend fun getAvailableHobbies(): Result<List<String>>
    suspend fun logout(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}