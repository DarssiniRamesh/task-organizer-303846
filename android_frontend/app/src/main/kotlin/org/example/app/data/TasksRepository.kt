package org.example.app.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository for TaskEntity data access.
 */
class TasksRepository(
    private val dao: TaskDao
) {

    fun observeTasks(): Flow<List<TaskEntity>> = dao.observeAll()

    suspend fun getTask(id: Long): TaskEntity? = dao.getById(id)

    suspend fun insert(task: TaskEntity): Long = dao.insert(task)

    suspend fun update(task: TaskEntity) = dao.update(task)

    suspend fun delete(task: TaskEntity) = dao.delete(task)

    suspend fun count(): Int = dao.count()

    suspend fun insertAll(tasks: List<TaskEntity>) = dao.insertAll(tasks)
}
