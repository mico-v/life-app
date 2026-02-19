package com.example.android16demo.data.repository

import com.example.android16demo.data.sync.SyncPreferences
import com.example.android16demo.network.RetrofitClient
import com.example.android16demo.network.api.LifeAppApi
import com.example.android16demo.network.model.ApiMessageResponse
import com.example.android16demo.network.model.CreatePostRequest
import com.example.android16demo.network.model.CreatePostResponse
import com.example.android16demo.network.model.PostDtoV2
import com.example.android16demo.network.model.PostsResponse
import com.example.android16demo.network.model.PublicFeedResponse
import com.example.android16demo.network.model.StatusEventRequest
import com.example.android16demo.network.model.StatusEventResponse

class WebRepository(private val syncPreferences: SyncPreferences) {

    private fun api(): LifeAppApi {
        val configured = syncPreferences.serverUrl.trim()
        return RetrofitClient.getApi(if (configured.isBlank()) null else configured)
    }

    suspend fun getPublicFeed(): Result<PublicFeedResponse> {
        return runCatching {
            val response = api().getPublicFeed()
            val body = response.body()
            if (!response.isSuccessful || body == null || !body.success) {
                error("Failed to load feed: ${response.code()}")
            }
            body
        }
    }

    suspend fun publishStatus(status: String, ttlMinutes: Int): Result<StatusEventResponse> {
        return runCatching {
            val now = System.currentTimeMillis()
            val expiresAt = now + ttlMinutes.coerceAtLeast(1) * 60_000L
            val response = api().publishStatusEvent(
                clientToken = syncPreferences.clientToken,
                serverPassword = syncPreferences.serverPassword,
                request = StatusEventRequest(
                    source = "manual",
                    status = status,
                    observedAt = now,
                    expiresAt = expiresAt
                )
            )
            val body = response.body()
            if (!response.isSuccessful || body == null || !body.success) {
                error(body?.message ?: "Failed to publish status: ${response.code()}")
            }
            body
        }
    }

    suspend fun publishPost(content: String, tags: String?, location: String?): Result<CreatePostResponse> {
        return runCatching {
            val response = api().publishPost(
                clientToken = syncPreferences.clientToken,
                serverPassword = syncPreferences.serverPassword,
                request = CreatePostRequest(content = content, tags = tags, location = location)
            )
            val body = response.body()
            if (!response.isSuccessful || body == null || !body.success) {
                error(body?.message ?: "Failed to publish post: ${response.code()}")
            }
            body
        }
    }

    suspend fun getMyPosts(): Result<List<PostDtoV2>> {
        return runCatching {
            val response = api().getMyPosts(
                clientToken = syncPreferences.clientToken,
                serverPassword = syncPreferences.serverPassword
            )
            val body: PostsResponse? = response.body()
            if (!response.isSuccessful || body == null || !body.success) {
                error("Failed to load posts: ${response.code()}")
            }
            body.posts
        }
    }

    suspend fun deletePost(postId: String): Result<ApiMessageResponse> {
        return runCatching {
            val response = api().deletePost(
                clientToken = syncPreferences.clientToken,
                serverPassword = syncPreferences.serverPassword,
                postId = postId
            )
            val body = response.body()
            if (!response.isSuccessful || body == null || !body.success) {
                error(body?.message ?: "Failed to delete post: ${response.code()}")
            }
            body
        }
    }
}
