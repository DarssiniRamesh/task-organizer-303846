package org.example.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a to-do task.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val dueDate: Long? = null
)
