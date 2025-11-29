package com.example.android16demo.data.sync

import com.example.android16demo.data.entity.Task
import com.example.android16demo.data.repository.TaskRepository
import com.example.android16demo.network.api.LifeAppApi
import com.example.android16demo.network.model.SyncRequest
import com.example.android16demo.network.model.SyncResponse
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

/**
 * Unit tests for SyncRepository
 */
class SyncRepositoryTest {
    
    private lateinit var api: LifeAppApi
    private lateinit var taskRepository: TaskRepository
    private lateinit var syncPreferences: SyncPreferences
    private lateinit var syncRepository: SyncRepository
    
    @Before
    fun setup() {
        api = mock()
        taskRepository = mock()
        syncPreferences = mock()
        syncRepository = SyncRepository(api, taskRepository, syncPreferences)
    }
    
    @Test
    fun `syncTasks returns NotLoggedIn when user is not logged in`() = runTest {
        // Given
        whenever(syncPreferences.isLoggedIn).thenReturn(false)
        
        // When
        val result = syncRepository.syncTasks()
        
        // Then
        assertTrue(result is SyncRepository.SyncResult.NotLoggedIn)
    }
    
    @Test
    fun `syncTasks returns NotLoggedIn when auth token is null`() = runTest {
        // Given
        whenever(syncPreferences.isLoggedIn).thenReturn(true)
        whenever(syncPreferences.authToken).thenReturn(null)
        
        // When
        val result = syncRepository.syncTasks()
        
        // Then
        assertTrue(result is SyncRepository.SyncResult.NotLoggedIn)
    }
    
    @Test
    fun `syncTasks returns NotLoggedIn when user id is null`() = runTest {
        // Given
        whenever(syncPreferences.isLoggedIn).thenReturn(true)
        whenever(syncPreferences.authToken).thenReturn("token")
        whenever(syncPreferences.userId).thenReturn(null)
        
        // When
        val result = syncRepository.syncTasks()
        
        // Then
        assertTrue(result is SyncRepository.SyncResult.NotLoggedIn)
    }
    
    @Test
    fun `syncTasks returns Success when sync succeeds`() = runTest {
        // Given
        val tasks = listOf(Task(title = "Test Task"))
        val syncResponse = SyncResponse(
            success = true,
            message = null,
            serverTime = 12345L,
            updatedTasks = null
        )
        
        whenever(syncPreferences.isLoggedIn).thenReturn(true)
        whenever(syncPreferences.authToken).thenReturn("token")
        whenever(syncPreferences.userId).thenReturn("user-123")
        whenever(syncPreferences.lastSyncTime).thenReturn(0L)
        whenever(taskRepository.getAllTasks()).thenReturn(flowOf(tasks))
        whenever(api.syncTasks(any(), any())).thenReturn(Response.success(syncResponse))
        
        // When
        val result = syncRepository.syncTasks()
        
        // Then
        assertTrue(result is SyncRepository.SyncResult.Success)
        assertEquals(1, (result as SyncRepository.SyncResult.Success).syncedCount)
        assertEquals(12345L, result.serverTime)
    }
    
    @Test
    fun `syncTasks returns Error when sync fails`() = runTest {
        // Given
        val tasks = listOf(Task(title = "Test Task"))
        val syncResponse = SyncResponse(
            success = false,
            message = "Sync failed",
            serverTime = 0L,
            updatedTasks = null
        )
        
        whenever(syncPreferences.isLoggedIn).thenReturn(true)
        whenever(syncPreferences.authToken).thenReturn("token")
        whenever(syncPreferences.userId).thenReturn("user-123")
        whenever(syncPreferences.lastSyncTime).thenReturn(0L)
        whenever(taskRepository.getAllTasks()).thenReturn(flowOf(tasks))
        whenever(api.syncTasks(any(), any())).thenReturn(Response.success(syncResponse))
        
        // When
        val result = syncRepository.syncTasks()
        
        // Then
        assertTrue(result is SyncRepository.SyncResult.Error)
        assertEquals("Sync failed", (result as SyncRepository.SyncResult.Error).message)
    }
    
    @Test
    fun `syncTasks returns Error on server error`() = runTest {
        // Given
        val tasks = listOf(Task(title = "Test Task"))
        
        whenever(syncPreferences.isLoggedIn).thenReturn(true)
        whenever(syncPreferences.authToken).thenReturn("token")
        whenever(syncPreferences.userId).thenReturn("user-123")
        whenever(syncPreferences.lastSyncTime).thenReturn(0L)
        whenever(taskRepository.getAllTasks()).thenReturn(flowOf(tasks))
        whenever(api.syncTasks(any(), any())).thenReturn(
            Response.error(500, "Server error".toResponseBody())
        )
        
        // When
        val result = syncRepository.syncTasks()
        
        // Then
        assertTrue(result is SyncRepository.SyncResult.Error)
        assertTrue((result as SyncRepository.SyncResult.Error).message.contains("500"))
    }
    
    @Test
    fun `syncTasks updates lastSyncTime on success`() = runTest {
        // Given
        val tasks = listOf(Task(title = "Test Task"))
        val syncResponse = SyncResponse(
            success = true,
            message = null,
            serverTime = 99999L,
            updatedTasks = null
        )
        
        whenever(syncPreferences.isLoggedIn).thenReturn(true)
        whenever(syncPreferences.authToken).thenReturn("token")
        whenever(syncPreferences.userId).thenReturn("user-123")
        whenever(syncPreferences.lastSyncTime).thenReturn(0L)
        whenever(taskRepository.getAllTasks()).thenReturn(flowOf(tasks))
        whenever(api.syncTasks(any(), any())).thenReturn(Response.success(syncResponse))
        
        // When
        syncRepository.syncTasks()
        
        // Then
        verify(syncPreferences).lastSyncTime = 99999L
    }
    
    @Test
    fun `logout clears auth credentials`() = runTest {
        // Given
        whenever(syncPreferences.authToken).thenReturn("token")
        whenever(api.logout(any())).thenReturn(Response.success(Unit))
        
        // When
        val result = syncRepository.logout()
        
        // Then
        assertTrue(result.isSuccess)
        verify(syncPreferences).clearAuth()
    }
    
    @Test
    fun `logout clears auth even on server error`() = runTest {
        // Given
        whenever(syncPreferences.authToken).thenReturn("token")
        whenever(api.logout(any())).thenThrow(RuntimeException("Network error"))
        
        // When
        val result = syncRepository.logout()
        
        // Then
        assertTrue(result.isSuccess)
        verify(syncPreferences).clearAuth()
    }
}
