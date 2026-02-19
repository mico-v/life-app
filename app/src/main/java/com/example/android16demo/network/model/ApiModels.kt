package com.example.android16demo.network.model

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement

/**
 * Data Transfer Objects for API communication
 */

data class PostDtoV2(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String,
    @SerializedName("tags") val tags: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("visibility") val visibility: String?,
    @SerializedName("created_at") val createdAt: Long?,
    @SerializedName("updated_at") val updatedAt: Long?
)

data class StatusSourceDto(
    @SerializedName("source") val source: String,
    @SerializedName("status") val status: String,
    @SerializedName("observed_at") val observedAt: Long?,
    @SerializedName("expires_at") val expiresAt: Long?,
    @SerializedName("meta") val meta: JsonElement?
)

data class FeedStatusDto(
    @SerializedName("primary") val primary: StatusSourceDto,
    @SerializedName("sources") val sources: List<StatusSourceDto>,
    @SerializedName("offline") val offline: Boolean
)

data class FeedStatsDto(
    @SerializedName("totalPosts") val totalPosts: Int,
    @SerializedName("activeSources") val activeSources: Int
)

data class PublicFeedResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status") val status: FeedStatusDto,
    @SerializedName("posts") val posts: List<PostDtoV2>,
    @SerializedName("stats") val stats: FeedStatsDto,
    @SerializedName("server_time") val serverTime: Long
)

data class StatusEventRequest(
    @SerializedName("source") val source: String,
    @SerializedName("status") val status: String,
    @SerializedName("observed_at") val observedAt: Long,
    @SerializedName("expires_at") val expiresAt: Long,
    @SerializedName("meta") val meta: Map<String, Any>? = null
)

data class CreatePostRequest(
    @SerializedName("content") val content: String,
    @SerializedName("tags") val tags: String? = null,
    @SerializedName("location") val location: String? = null
)

data class StatusEventResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("source_status") val sourceStatus: StatusSourceDto?
)

data class CreatePostResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("post") val post: PostDtoV2?
)

data class PostsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("posts") val posts: List<PostDtoV2>
)

data class ApiMessageResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)
