package org.example.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.example.app.data.TaskDao
import org.example.app.data.TaskEntity
import org.example.app.data.TasksRepository
import org.example.app.testutil.MainDispatcherExtension
import org.example.app.testutil.getOrAwaitValue
import org.example.app.viewmodel.TasksViewModel
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class test_TasksViewModel {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private class FakeTaskDao : TaskDao {
        private val items = linkedMapOf<Long, TaskEntity>()
        private var nextId = 1L
        private val state = MutableStateFlow<List<TaskEntity>>(emptyList())

        override fun observeAll(): Flow<List<TaskEntity>> = state

        override suspend fun getById(id: Long): TaskEntity? = items[id]

        override suspend fun count(): Int = items.size

        override suspend fun insert(task: TaskEntity): Long {
            val id = if (task.id == 0L) nextId++ else task.id
            items[id] = task.copy(id = id)
            emitSorted()
            return id
        }

        override suspend fun insertAll(tasks: List<TaskEntity>) {
            tasks.forEach { insert(it) }
            emitSorted()
        }

        override suspend fun update(task: TaskEntity) {
            items[task.id] = task
            emitSorted()
        }

        override suspend fun delete(task: TaskEntity) {
            items.remove(task.id)
            emitSorted()
        }

        override suspend fun deleteById(id: Long) {
            items.remove(id)
            emitSorted()
        }

        private fun emitSorted() {
            state.value = items.values.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenByDescending { it.updatedAt }
            )
        }
    }

    @Test
    fun `initial load seeds welcome tasks when repository is empty`() = runTest {
        val repo = TasksRepository(FakeTaskDao())

        val vm = TasksViewModel(repo)

        // Seed runs on Dispatchers.IO; allow coroutines to progress.
        advanceUntilIdle()

        val list = vm.tasks.getOrAwaitValue()
        assertTrue(list.size >= 2)
        assertTrue(list.any { it.title.contains("Welcome", ignoreCase = true) })
    }

    @Test
    fun `addTask inserts trimmed title and normalizes blank description to null`() = runTest {
        val repo = TasksRepository(FakeTaskDao())
        val vm = TasksViewModel(repo)

        // Allow potential seeding; we don't assert size, just presence of our item.
        advanceUntilIdle()

        vm.addTask("  My Task  ", "   ", dueDate = null)
        advanceUntilIdle()

        val list = vm.tasks.getOrAwaitValue()
        val added = list.firstOrNull { it.title == "My Task" }
        assertNotNull(added)
        assertEquals(null, added!!.description)
        assertEquals(false, added.isCompleted)
    }

    @Test
    fun `updateTask updates title description dueDate and updatedAt`() = runTest {
        val repo = TasksRepository(FakeTaskDao())
        val vm = TasksViewModel(repo)

        // Add a base task
        vm.addTask("Old", "Old desc", dueDate = null)
        advanceUntilIdle()

        val before = vm.tasks.getOrAwaitValue().first { it.title == "Old" }
        val beforeUpdatedAt = before.updatedAt

        vm.updateTask(before, " New ", "  ", dueDate = 999L)
        advanceUntilIdle()

        val after = vm.tasks.getOrAwaitValue().first { it.id == before.id }
        assertEquals("New", after.title)
        assertEquals(null, after.description) // blank -> null
        assertEquals(999L, after.dueDate)
        assertTrue(after.updatedAt >= beforeUpdatedAt)
    }

    @Test
    fun `setCompleted toggles completion and updates updatedAt`() = runTest {
        val repo = TasksRepository(FakeTaskDao())
        val vm = TasksViewModel(repo)

        vm.addTask("Toggle", null, dueDate = null)
        advanceUntilIdle()

        val before = vm.tasks.getOrAwaitValue().first { it.title == "Toggle" }
        val beforeUpdatedAt = before.updatedAt

        vm.setCompleted(before, true)
        advanceUntilIdle()

        val after = vm.tasks.getOrAwaitValue().first { it.id == before.id }
        assertEquals(true, after.isCompleted)
        assertTrue(after.updatedAt >= beforeUpdatedAt)
    }

    @Test
    fun `deleteTask removes task and restoreTask re-inserts it (undo delete behavior)`() = runTest {
        val repo = TasksRepository(FakeTaskDao())
        val vm = TasksViewModel(repo)

        vm.addTask("To delete", null, dueDate = null)
        advanceUntilIdle()

        val task = vm.tasks.getOrAwaitValue().first { it.title == "To delete" }

        vm.deleteTask(task)
        advanceUntilIdle()

        val afterDelete = vm.tasks.getOrAwaitValue()
        assertTrue(afterDelete.none { it.id == task.id })

        vm.restoreTask(task)
        advanceUntilIdle()

        val afterRestore = vm.tasks.getOrAwaitValue()
        assertTrue(afterRestore.any { it.title == "To delete" })
    }

    @Test
    fun `getTaskById returns entity when present`() = runTest {
        val repo = TasksRepository(FakeTaskDao())
        val vm = TasksViewModel(repo)

        vm.addTask("Find me", "x", dueDate = null)
        advanceUntilIdle()

        val task = vm.tasks.getOrAwaitValue().first { it.title == "Find me" }

        val loaded = vm.getTaskById(task.id)
        assertEquals(task.id, loaded?.id)
        assertEquals("Find me", loaded?.title)
    }
}
