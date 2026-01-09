package org.example.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.example.app.data.TasksDatabase
import org.example.app.data.TasksRepository

/**
 * Factory for TasksViewModel that wires Room database + repository.
 */
class TasksViewModelFactory(
    private val appContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = TasksDatabase.getInstance(appContext)
        val repo = TasksRepository(db.taskDao())
        return TasksViewModel(repo) as T
    }
}
