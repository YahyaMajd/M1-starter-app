package com.cpen321.usermanagement.di

import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.data.repository.AuthRepositoryImpl
import com.cpen321.usermanagement.data.repository.GitHubRepository
import com.cpen321.usermanagement.data.repository.GitHubRepositoryImpl
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.ProfileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun provideProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun provideGitHubRepository(
        gitHubRepositoryImpl: GitHubRepositoryImpl
    ): GitHubRepository
}
