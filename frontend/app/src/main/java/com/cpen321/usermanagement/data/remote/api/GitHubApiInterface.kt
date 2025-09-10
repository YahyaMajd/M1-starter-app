package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.GitHubCommit
import com.cpen321.usermanagement.data.remote.dto.GitHubRepository
import com.cpen321.usermanagement.data.remote.dto.GitHubWorkflowRunsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiInterface {
    
    // Get user's repositories
    @GET("user/repos")
    suspend fun getUserRepositories(
        @Header("Authorization") token: String,
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 30
    ): Response<List<GitHubRepository>>
    
    // Get repository details
    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GitHubRepository>
    
    // Get recent commits for a repository
    @GET("repos/{owner}/{repo}/commits")
    suspend fun getRepositoryCommits(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 10
    ): Response<List<GitHubCommit>>
    
    // Get workflow runs for a repository (GitHub Actions)
    @GET("repos/{owner}/{repo}/actions/runs")
    suspend fun getWorkflowRuns(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 10
    ): Response<GitHubWorkflowRunsResponse>
    
    // Get authenticated user info
    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") token: String
    ): Response<com.cpen321.usermanagement.data.remote.dto.GitHubUser>
}
