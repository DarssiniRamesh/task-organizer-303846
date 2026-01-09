package org.example.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.app.data.TaskDao
import org.example.app.data.TaskEntity
import org.example.app.data.TasksDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class test_TaskDao {

    private lateinit var db: TasksDatabase
    private lateinit var dao: TaskDao

    @BeforeEach
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, TasksDatabase::class.java)
            .allowMainThreadQueries() // unit tests only
            .build()
        dao = db.taskDao()
    }

    @AfterEach
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insert and getById returns entity`() = runTest {
        val now = 100L
        val id = dao.insert(
            TaskEntity(
                title = "t",
                description = "d",
                isCompleted = false,
                createdAt = now,
                updatedAt = now,
                dueDate = null
            )
        )

        val loaded = dao.getById(id)
        assertNotNull(loaded)
        assertEquals(id, loaded!!.id)
        assertEquals("t", loaded.title)
        assertEquals("d", loaded.description)
    }

    @Test
    fun `update persists change`() = runTest {
        val id = dao.insert(
            TaskEntity(
                title = "old",
                description = null,
                isCompleted = false,
                createdAt = 1L,
                updatedAt = 1L,
                dueDate = null
            )
        )
        val before = dao.getById(id)!!

        dao.update(before.copy(title = "new", updatedAt = 2L))

        val after = dao.getById(id)!!
        assertEquals("new", after.title)
        assertEquals(2L, after.updatedAt)
    }

    @Test
    fun `delete removes row`() = runTest {
        val id = dao.insert(
            TaskEntity(
                title = "x",
                description = null,
                isCompleted = false,
                createdAt = 1L,
                updatedAt = 1L,
                dueDate = null
            )
        )
        val task = dao.getById(id)!!
        assertEquals(1, dao.count())

        dao.delete(task)

        assertEquals(0, dao.count())
        assertEquals(null, dao.getById(id))
    }

    @Test
    fun `observeAll returns incomplete first then updatedAt desc`() = runTest {
        val idA = dao.insert(
            TaskEntity(
                title = "incomplete older",
                description = null,
                isCompleted = false,
                createdAt = 1L,
                updatedAt = 10L,
                dueDate = null
            )
        )
        val idB = dao.insert(
            TaskEntity(
                title = "complete newest",
                description = null,
                isCompleted = true,
                createdAt = 1L,
                updatedAt = 1000L,
                dueDate = null
            )
        )
        val idC = dao.insert(
            TaskEntity(
                title = "incomplete newer",
                description = null,
                isCompleted = false,
                createdAt = 1L,
                updatedAt = 99L,
                dueDate = null
            )
        )

        val list = dao.observeAll().first()
        val titles = list.map { it.title }

        assertEquals(listOf("incomplete newer", "incomplete older", "complete newest"), titles)
        assertTrue(setOf(idA, idB, idC).all { it > 0L })
    }

    @Test
    fun `toggle complete via update changes ordering`() = runTest {
        val id1 = dao.insert(
            TaskEntity(
                title = "task",
                description = null,
                isCompleted = false,
                createdAt = 1L,
                updatedAt = 1L,
                dueDate = null
            )
        )
        val inserted = dao.getById(id1)!!
        assertEquals(false, inserted.isCompleted)

        // Mark complete with higher updatedAt
        dao.update(inserted.copy(isCompleted = true, updatedAt = 999L))

        val list = dao.observeAll().first()
        val updated = dao.getById(id1)!!
        assertEquals(true, updated.isCompleted)
        // If there are no incompletes, it should be first; otherwise completes go after.
        assertEquals(id1, list.last().id)
    }
}
