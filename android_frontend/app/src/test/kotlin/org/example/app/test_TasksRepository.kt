package org.example.app

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.app.data.TaskDao
import org.example.app.data.TaskEntity
import org.example.app.data.TasksRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class test_TasksRepository {

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
            // Match TaskDao query:
            // ORDER BY isCompleted ASC, updatedAt DESC
            state.value = items.values
                .sortedWith(
                    compareBy<TaskEntity> { it.isCompleted }
                        .thenByDescending { it.updatedAt }
                )
        }
    }

    @Test
    fun `insert then getTask returns entity`() = runTest {
        val dao = FakeTaskDao()
        val repo = TasksRepository(dao)

        val now = 1234L
        val id = repo.insert(
            TaskEntity(
                title = "t",
                description = "d",
                isCompleted = false,
                createdAt = now,
                updatedAt = now,
                dueDate = null
            )
        )

        val loaded = repo.getTask(id)
        assertEquals(id, loaded?.id)
        assertEquals("t", loaded?.title)
        assertEquals("d", loaded?.description)
    }

    @Test
    fun `update persists changes`() = runTest {
        val dao = FakeTaskDao()
        val repo = TasksRepository(dao)

        val now = 10L
        val id = repo.insert(
            TaskEntity(
                title = "old",
                description = null,
                isCompleted = false,
                createdAt = now,
                updatedAt = now,
                dueDate = null
            )
        )
        val before = repo.getTask(id)!!

        repo.update(before.copy(title = "new", updatedAt = 20L))

        val after = repo.getTask(id)
        assertEquals("new", after?.title)
        assertEquals(20L, after?.updatedAt)
    }

    @Test
    fun `delete removes item`() = runTest {
        val dao = FakeTaskDao()
        val repo = TasksRepository(dao)

        val id = repo.insert(
            TaskEntity(
                title = "t",
                description = null,
                isCompleted = false,
                createdAt = 1L,
                updatedAt = 1L,
                dueDate = null
            )
        )
        val task = repo.getTask(id)!!

        repo.delete(task)

        assertEquals(null, repo.getTask(id))
        assertEquals(0, repo.count())
    }

    @Test
    fun `observeTasks emits ordered list incomplete first then updatedAt desc`() = runTest {
        val dao = FakeTaskDao()
        val repo = TasksRepository(dao)

        // Completed task with newer updatedAt should still come after incompletes.
        val id1 = repo.insert(
            TaskEntity(
                title = "incomplete older",
                description = null,
                isCompleted = false,
                createdAt = 1L,
                updatedAt = 10L,
                dueDate = null
            )
        )
        val id2 = repo.insert(
            TaskEntity(
                title = "incomplete newer",
                description = null,
                isCompleted = false,
                createdAt = 1L,
                updatedAt = 99L,
                dueDate = null
            )
        )
        val id3 = repo.insert(
            TaskEntity(
                title = "complete newest",
                description = null,
                isCompleted = true,
                createdAt = 1L,
                updatedAt = 1000L,
                dueDate = null
            )
        )

        val titles = repo.observeTasks().first().map { it.title }

        assertEquals(listOf("incomplete newer", "incomplete older", "complete newest"), titles)
        // sanity ids exist
        assertEquals(true, setOf(id1, id2, id3).all { it > 0L })
    }
}
