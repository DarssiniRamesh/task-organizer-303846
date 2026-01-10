package org.example.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.example.app.data.TaskEntity
import org.example.app.ui.TaskEditDialogFragment
import org.example.app.ui.TasksAdapter
import org.example.app.ui.TasksSwipeToDeleteCallback
import org.example.app.viewmodel.TasksViewModel
import org.example.app.viewmodel.TasksViewModelFactory

/**
 * Main entry point activity showing the task list with add/edit/delete interactions.
 */
class MainActivity : AppCompatActivity(), TasksAdapter.TaskItemListener {

    private lateinit var viewModel: TasksViewModel
    private lateinit var adapter: TasksAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateContainer: View
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.tasks_title)
        setSupportActionBar(toolbar)

        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_clear_completed -> {
                    viewModel.clearCompletedTasks()
                    true
                }
                R.id.action_about -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }

        recyclerView = findViewById(R.id.recyclerTasks)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        fab = findViewById(R.id.fabAdd)

        // Create ViewModel with app-level dependencies (Room DB + repository).
        viewModel = ViewModelProvider(
            this,
            TasksViewModelFactory(applicationContext)
        )[TasksViewModel::class.java]

        adapter = TasksAdapter(this)
        recyclerView.adapter = adapter

        // Swipe-to-delete with undo.
        val swipeCallback = TasksSwipeToDeleteCallback { position ->
            val task = adapter.getItemOrNull(position) ?: return@TasksSwipeToDeleteCallback
            viewModel.deleteTask(task)

            Snackbar.make(recyclerView, R.string.snackbar_task_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    viewModel.restoreTask(task)
                }
                .show()
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        // Observe tasks list.
        viewModel.tasks.observe(this) { tasks ->
            adapter.submitList(tasks)
            emptyStateContainer.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
        }

        fab.setOnClickListener {
            TaskEditDialogFragment.newAddInstance()
                .show(supportFragmentManager, TaskEditDialogFragment.TAG)
        }
    }

    private fun showAboutDialog() {
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(getString(R.string.about_message, versionName))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onToggleCompleted(task: TaskEntity, isCompleted: Boolean) {
        viewModel.setCompleted(task, isCompleted)
    }

    override fun onEdit(task: TaskEntity) {
        TaskEditDialogFragment.newEditInstance(task.id)
            .show(supportFragmentManager, TaskEditDialogFragment.TAG)
    }

    override fun onDelete(task: TaskEntity) {
        viewModel.deleteTask(task)
        Snackbar.make(recyclerView, R.string.snackbar_task_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                viewModel.restoreTask(task)
            }
            .show()
    }
}
