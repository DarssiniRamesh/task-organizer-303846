package org.example.app.ui

import android.app.Dialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import org.example.app.R
import org.example.app.data.TaskEntity
import org.example.app.viewmodel.TasksViewModel
import org.example.app.viewmodel.TasksViewModelFactory
import java.util.Date
import kotlinx.coroutines.runBlocking

/**
 * Dialog fragment to add or edit a task.
 */
class TaskEditDialogFragment : DialogFragment() {

    private lateinit var viewModel: TasksViewModel

    private var editingTask: TaskEntity? = null
    private var selectedDueDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            requireActivity(),
            TasksViewModelFactory(requireContext().applicationContext)
        )[TasksViewModel::class.java]

        val taskId = arguments?.getLong(ARG_TASK_ID, NO_TASK_ID) ?: NO_TASK_ID
        if (taskId != NO_TASK_ID) {
            // Resolve synchronously for dialog creation simplicity. Small local DB read.
            editingTask = runBlocking { viewModel.getTaskById(taskId) }
            selectedDueDate = editingTask?.dueDate
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val content = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_task_edit, null)
        val editTitle = content.findViewById<TextInputEditText>(R.id.editTitle)
        val editDescription = content.findViewById<TextInputEditText>(R.id.editDescription)
        val textDueValue = content.findViewById<TextView>(R.id.textDueValue)
        val buttonPickDue = content.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonPickDue)
        val buttonClearDue = content.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonClearDue)

        val isEdit = editingTask != null
        val titleRes = if (isEdit) R.string.edit_task else R.string.add_task

        editingTask?.let { task ->
            editTitle.setText(task.title)
            editDescription.setText(task.description.orEmpty())
            updateDueLabel(textDueValue, selectedDueDate)
        } ?: run {
            updateDueLabel(textDueValue, null)
        }

        buttonPickDue.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.pick_due_date)
                .setSelection(selectedDueDate ?: MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            picker.addOnPositiveButtonClickListener { millis ->
                selectedDueDate = millis
                updateDueLabel(textDueValue, selectedDueDate)
            }
            picker.show(parentFragmentManager, "due_date_picker")
        }

        buttonClearDue.setOnClickListener {
            selectedDueDate = null
            updateDueLabel(textDueValue, null)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(titleRes)
            .setView(content)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val titleText = editTitle.text?.toString().orEmpty().trim()
                val descriptionText = editDescription.text?.toString()

                if (titleText.isBlank()) {
                    editTitle.error = getString(R.string.error_title_required)
                    return@setOnClickListener
                }

                val existing = editingTask
                if (existing == null) {
                    viewModel.addTask(titleText, descriptionText, selectedDueDate)
                } else {
                    viewModel.updateTask(existing, titleText, descriptionText, selectedDueDate)
                }
                dismiss()
            }
        }

        return dialog
    }

    private fun updateDueLabel(target: TextView, dueMillis: Long?) {
        if (dueMillis == null) {
            target.text = getString(R.string.due_date_none)
        } else {
            val formatted = DateFormat.getMediumDateFormat(requireContext()).format(Date(dueMillis))
            target.text = getString(R.string.due_date_value, formatted)
        }
    }

    companion object {
        const val TAG = "TaskEditDialogFragment"
        private const val ARG_TASK_ID = "arg_task_id"
        private const val NO_TASK_ID = -1L

        // PUBLIC_INTERFACE
        fun newAddInstance(): TaskEditDialogFragment {
            return TaskEditDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_TASK_ID, NO_TASK_ID) }
            }
        }

        // PUBLIC_INTERFACE
        fun newEditInstance(taskId: Long): TaskEditDialogFragment {
            return TaskEditDialogFragment().apply {
                arguments = Bundle().apply { putLong(ARG_TASK_ID, taskId) }
            }
        }
    }
}
