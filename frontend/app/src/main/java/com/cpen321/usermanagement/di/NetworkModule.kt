package com.cpen321.usermanagement.di

import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.api.AuthInterface
import com.cpen321.usermanagement.data.remote.api.GitHubApiInterface
import com.cpen321.usermanagement.data.remote.api.HobbyInterface
import com.cpen321.usermanagement.data.remote.api.ImageInterface
import com.cpen321.usermanagement.data.remote.api.RetrofitClient
import com.cpen321.usermanagement.data.remote.api.UserInterface
import com.cpen321.usermanagement.network.auth.GitHubAuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthService(): AuthInterface {
        return RetrofitClient.authInterface
    }

    @Provides
    @Singleton
    fun provideUserService(): UserInterface {
        return RetrofitClient.userInterface
    }

    @Provides
    @Singleton
    fun provideImageInterface(): ImageInterface {
        return RetrofitClient.imageInterface
    }

    @Provides
    @Singleton
    fun provideHobbyService(): HobbyInterface {
        return RetrofitClient.hobbyInterface
    }

    @Provides
    @Singleton
    @Named("github")
    fun provideGitHubRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGitHubApiInterface(@Named("github") retrofit: Retrofit): GitHubApiInterface {
        return retrofit.create(GitHubApiInterface::class.java)
    }
}
