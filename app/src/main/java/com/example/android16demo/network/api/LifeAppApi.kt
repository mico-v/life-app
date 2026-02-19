package com.example.android16demo.network.api

import com.example.android16demo.network.model.CreatePostRequest
import com.example.android16demo.network.model.CreatePostResponse
import com.example.android16demo.network.model.ApiMessageResponse
import com.example.android16demo.network.model.PostsResponse
import com.example.android16demo.network.model.PublicFeedResponse
import com.example.android16demo.network.model.StatusEventRequest
import com.example.android16demo.network.model.StatusEventResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API interface for Life App backend
 */
interface LifeAppApi {
    
    companion object {
        const val BASE_URL = "https://api.life-app.com/" // Placeholder URL
        const val API_VERSION = "v1"
    }
    
    @GET("api/$API_VERSION/public/feed")
    suspend fun getPublicFeed(): Response<PublicFeedResponse>

    @POST("api/$API_VERSION/status/events")
    suspend fun publishStatusEvent(
        @Header("x-client-token") clientToken: String,
        @Header("x-server-password") serverPassword: String,
        @Body request: StatusEventRequest
    ): Response<StatusEventResponse>

    @POST("api/$API_VERSION/posts")
    suspend fun publishPost(
        @Header("x-client-token") clientToken: String,
        @Header("x-server-password") serverPassword: String,
        @Body request: CreatePostRequest
    ): Response<CreatePostResponse>

    @GET("api/$API_VERSION/posts")
    suspend fun getMyPosts(
        @Header("x-client-token") clientToken: String,
        @Header("x-server-password") serverPassword: String
    ): Response<PostsResponse>

    @DELETE("api/$API_VERSION/posts/{postId}")
    suspend fun deletePost(
        @Header("x-client-token") clientToken: String,
        @Header("x-server-password") serverPassword: String,
        @Path("postId") postId: String
    ): Response<ApiMessageResponse>
}
