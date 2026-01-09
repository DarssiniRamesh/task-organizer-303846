package org.example.app.ui

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.TaskEntity
import java.util.Date

/**
 * Adapter for tasks list.
 */
class TasksAdapter(
    private val listener: TaskItemListener
) : ListAdapter<TaskEntity, TasksAdapter.TaskViewHolder>(DIFF) {

    interface TaskItemListener {
        fun onToggleCompleted(task: TaskEntity, isCompleted: Boolean)
        fun onEdit(task: TaskEntity)
        fun onDelete(task: TaskEntity)
    }

    fun getItemOrNull(position: Int): TaskEntity? {
        return if (position in 0 until itemCount) getItem(position) else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        itemView: View,
        private val listener: TaskItemListener
    ) : RecyclerView.ViewHolder(itemView) {

        private val checkCompleted = itemView.findViewById<CheckBox>(R.id.checkCompleted)
        private val textTitle = itemView.findViewById<TextView>(R.id.textTitle)
        private val textDescription = itemView.findViewById<TextView>(R.id.textDescription)
        private val textDue = itemView.findViewById<TextView>(R.id.textDue)
        private val buttonMore = itemView.findViewById<ImageButton>(R.id.buttonMore)

        fun bind(task: TaskEntity) {
            textTitle.text = task.title
            checkCompleted.setOnCheckedChangeListener(null)
            checkCompleted.isChecked = task.isCompleted

            val desc = task.description?.trim().orEmpty()
            if (desc.isNotBlank()) {
                textDescription.visibility = View.VISIBLE
                textDescription.text = desc
            } else {
                textDescription.visibility = View.GONE
            }

            if (task.dueDate != null) {
                val formatted = DateFormat.getMediumDateFormat(itemView.context)
                    .format(Date(task.dueDate))
                textDue.visibility = View.VISIBLE
                textDue.text = itemView.context.getString(R.string.due_date_value, formatted)
            } else {
                textDue.visibility = View.GONE
            }

            checkCompleted.setOnCheckedChangeListener { _, isChecked ->
                listener.onToggleCompleted(task, isChecked)
            }

            // Tap row to edit quickly
            itemView.setOnClickListener {
                listener.onEdit(task)
            }

            buttonMore.setOnClickListener { v ->
                val popup = PopupMenu(v.context, v)
                MenuInflater(v.context).inflate(R.menu.menu_task_item, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            listener.onEdit(task); true
                        }
                        R.id.action_delete -> {
                            listener.onDelete(task); true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TaskEntity>() {
            override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
