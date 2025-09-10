package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.GitHubCommit
import com.cpen321.usermanagement.data.remote.dto.GitHubRepository
import com.cpen321.usermanagement.data.remote.dto.GitHubUser
import com.cpen321.usermanagement.data.remote.dto.GitHubWorkflowRun

interface GitHubRepository {
    suspend fun getUserRepositories(userId: String): Result<List<GitHubRepository>>
    suspend fun getRepository(userId: String, owner: String, repo: String): Result<GitHubRepository>
    suspend fun getRepositoryCommits(userId: String, owner: String, repo: String): Result<List<GitHubCommit>>
    suspend fun getWorkflowRuns(userId: String, owner: String, repo: String): Result<List<GitHubWorkflowRun>>
    suspend fun getAuthenticatedUser(userId: String): Result<GitHubUser>
    suspend fun connectGitHub(userId: String, accessToken: String): Result<Boolean>
    suspend fun isGitHubConnected(userId: String): Boolean
    suspend fun disconnectGitHub(userId: String)
}
