package com.example.android16demo.network.api

import com.example.android16demo.network.model.AuthRequest
import com.example.android16demo.network.model.AuthResponse
import com.example.android16demo.network.model.SyncRequest
import com.example.android16demo.network.model.SyncResponse
import com.example.android16demo.network.model.UserStatusResponse
import retrofit2.Response
import retrofit2.http.Body
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
    
    /**
     * Sync tasks with server
     */
    @POST("api/$API_VERSION/sync")
    suspend fun syncTasks(
        @Header("Authorization") token: String,
        @Body request: SyncRequest
    ): Response<SyncResponse>
    
    /**
     * Get user's public status
     */
    @GET("api/$API_VERSION/u/{username}/status")
    suspend fun getUserStatus(
        @Path("username") username: String
    ): Response<UserStatusResponse>
    
    /**
     * Login
     */
    @POST("api/$API_VERSION/auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>
    
    /**
     * Register new user
     */
    @POST("api/$API_VERSION/auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): Response<AuthResponse>
    
    /**
     * Logout / invalidate token
     */
    @POST("api/$API_VERSION/auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>
}
