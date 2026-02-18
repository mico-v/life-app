package com.example.android16demo.data.sync

import com.example.android16demo.data.repository.TaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SyncRepositoryTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var syncPreferences: SyncPreferences
    private lateinit var syncRepository: SyncRepository

    @Before
    fun setup() {
        taskRepository = mock()
        syncPreferences = mock()
        syncRepository = SyncRepository(taskRepository, syncPreferences)
    }

    @Test
    fun `syncTasks returns NotConfigured when sync is not configured`() = runTest {
        whenever(syncPreferences.isSyncConfigured).thenReturn(false)

        val result = syncRepository.syncTasks()

        assertTrue(result is SyncRepository.SyncResult.NotConfigured)
    }

    @Test
    fun `syncPublicTasks returns NotConfigured when sync is not configured`() = runTest {
        whenever(syncPreferences.isSyncConfigured).thenReturn(false)

        val result = syncRepository.syncPublicTasks()

        assertTrue(result is SyncRepository.SyncResult.NotConfigured)
    }
}
