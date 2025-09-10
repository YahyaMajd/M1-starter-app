package com.cpen321.usermanagement.data.remote.dto

import com.google.gson.annotations.SerializedName

// GitHub Repository Model
data class GitHubRepository(
    val id: Long,
    val name: String,
    @SerializedName("full_name")
    val fullName: String,
    val description: String?,
    val private: Boolean,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("clone_url")
    val cloneUrl: String,
    val language: String?,
    @SerializedName("stargazers_count")
    val stargazersCount: Int,
    @SerializedName("watchers_count")
    val watchersCount: Int,
    @SerializedName("forks_count")
    val forksCount: Int,
    @SerializedName("open_issues_count")
    val openIssuesCount: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("pushed_at")
    val pushedAt: String?,
    val owner: GitHubUser
)

// GitHub User Model (for repository owner)
data class GitHubUser(
    val id: Long,
    val login: String,
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    val type: String
)

// GitHub Commit Model
data class GitHubCommit(
    val sha: String,
    val commit: GitHubCommitDetails,
    val author: GitHubUser?,
    val committer: GitHubUser?,
    @SerializedName("html_url")
    val htmlUrl: String
)

data class GitHubCommitDetails(
    val message: String,
    val author: GitHubCommitAuthor,
    val committer: GitHubCommitAuthor
)

data class GitHubCommitAuthor(
    val name: String,
    val email: String,
    val date: String
)

// GitHub Actions Workflow Model
data class GitHubWorkflow(
    val id: Long,
    val name: String,
    val state: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("html_url")
    val htmlUrl: String
)

// GitHub Actions Workflow Run Model
data class GitHubWorkflowRun(
    val id: Long,
    val name: String?,
    @SerializedName("head_branch")
    val headBranch: String?,
    @SerializedName("head_sha")
    val headSha: String,
    val status: String, // queued, in_progress, completed
    val conclusion: String?, // success, failure, neutral, cancelled, skipped, timed_out, action_required
    val event: String?, // push, pull_request, schedule, etc.
    @SerializedName("workflow_id")
    val workflowId: Long,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("html_url")
    val htmlUrl: String
)

// GitHub API Response Wrappers
data class GitHubRepositoriesResponse(
    @SerializedName("workflow_runs")
    val workflowRuns: List<GitHubWorkflowRun>
)

data class GitHubWorkflowRunsResponse(
    @SerializedName("workflow_runs")
    val workflowRuns: List<GitHubWorkflowRun>
)
