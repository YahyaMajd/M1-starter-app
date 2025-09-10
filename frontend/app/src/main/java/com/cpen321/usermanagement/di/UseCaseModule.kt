package com.cpen321.usermanagement.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * Dependency injection module for use cases.
 * Use cases contain business logic and coordinate between repositories.
 */
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    
    // GitHub use cases are automatically provided via @Inject constructor
    // All use cases are @Singleton and have @Inject constructor
    // ReactiveOAuthCredentialsUseCase, ManageGitHubConnectionUseCase, CheckOAuthCredentialsUseCase
}
