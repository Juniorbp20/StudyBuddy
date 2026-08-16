package com.example.studybuddy

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.studybuddy.databinding.ActivityAddTaskBinding
import com.example.studybuddy.model.Category
import com.example.studybuddy.model.Priority
import com.example.studybuddy.model.RepeatInterval
import com.example.studybuddy.model.Task
import com.example.studybuddy.notification.AlarmManagerHelper
import com.example.studybuddy.ui.TaskViewModel
import com.example.studybuddy.util.DateUtils
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class AddTaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var alarmHelper: AlarmManagerHelper

    private var taskId = -1
    private var isEditing = false
    private var selectedCategory = Category.GENERAL
    private var selectedPriority = Priority.MEDIUM
    private var selectedRepeat = RepeatInterval.NONE
    private var selectedDateCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        alarmHelper = AlarmManagerHelper(this)

        setupCategoryChips()
        setupPriorityChips()
        setupRepeatChips()
        setupClickListeners()
        checkForEditIntent()
        updateDateAndTimeViews()
    }

    private fun setupCategoryChips() {
        binding.chipCategoryGeneral.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedCategory = Category.GENERAL
        }
        binding.chipCategoryStudy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedCategory = Category.STUDY
        }
        binding.chipCategoryWork.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedCategory = Category.WORK
        }
        binding.chipCategoryPersonal.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedCategory = Category.PERSONAL
        }
    }

    private fun setupPriorityChips() {
        binding.chipPriorityLow.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedPriority = Priority.LOW
        }
        binding.chipPriorityMedium.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedPriority = Priority.MEDIUM
        }
        binding.chipPriorityHigh.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedPriority = Priority.HIGH
        }
    }

    private fun setupRepeatChips() {
        binding.chipRepeatNone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedRepeat = RepeatInterval.NONE
        }
        binding.chipRepeatDaily.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedRepeat = RepeatInterval.DAILY
        }
        binding.chipRepeatWeekly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedRepeat = RepeatInterval.WEEKLY
        }
    }

    private fun setupClickListeners() {
        binding.buttonSelectDate.setOnClickListener { showDatePickerDialog() }
        binding.buttonSelectTime.setOnClickListener { showTimePickerDialog() }
        binding.buttonSaveTask.setOnClickListener { saveTask() }
    }

    private fun checkForEditIntent() {
        taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        isEditing = taskId != -1

        if (isEditing) {
            binding.textViewAddTitle.text = getString(R.string.edit_task_title)
            binding.buttonSaveTask.text = getString(R.string.update_task)
            taskViewModel.allTasks.observe(this) { tasks ->
                val task = tasks.firstOrNull { it.id == taskId } ?: return@observe
                binding.editTextTitle.setText(task.title)
                binding.editTextDescription.setText(task.description)
                binding.editTextTags.setText(task.tags)
                selectedCategory = task.category
                selectedPriority = task.priority
                selectedRepeat = task.repeatInterval
                selectedDateCalendar.timeInMillis = task.dueDate
                binding.switchReminder.isChecked = task.reminderEnabled
                binding.switchReminder.isEnabled = task.reminderEnabled
                selectChip(binding.chipGroupCategory, task.category)
                selectChip(binding.chipGroupPriority, task.priority.toString())
                selectChip(binding.chipGroupRepeat, task.repeatInterval.toString())
                updateDateAndTimeViews()
            }
        } else {
            binding.textViewAddTitle.text = getString(R.string.add_task_title)
            binding.buttonSaveTask.text = getString(R.string.save_task)
            binding.chipCategoryGeneral.isChecked = true
            binding.chipPriorityMedium.isChecked = true
            binding.chipRepeatNone.isChecked = true
        }
    }

    private fun selectChip(chipGroup: com.google.android.material.chip.ChipGroup, value: String) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip ?: continue
            if (chip.text.toString() == value) {
                chip.isChecked = true
            }
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDateCalendar.set(Calendar.YEAR, year)
                selectedDateCalendar.set(Calendar.MONTH, month)
                selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateAndTimeViews()
            },
            selectedDateCalendar.get(Calendar.YEAR),
            selectedDateCalendar.get(Calendar.MONTH),
            selectedDateCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePickerDialog() {
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDateCalendar.set(Calendar.MINUTE, minute)
                updateDateAndTimeViews()
            },
            selectedDateCalendar.get(Calendar.HOUR_OF_DAY),
            selectedDateCalendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun updateDateAndTimeViews() {
        binding.textViewSelectedDate.text = DateUtils.formatFull(selectedDateCalendar.timeInMillis)
        binding.textViewSelectedTime.text = DateUtils.formatTime(selectedDateCalendar.timeInMillis)
    }

    private fun saveTask() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val tags = binding.editTextTags.text.toString().trim()

        if (title.isEmpty()) {
            binding.editTextTitle.error = getString(R.string.task_title_required)
            binding.editTextTitle.requestFocus()
            return
        }

        val reminderEnabled = binding.switchReminder.isChecked
        val dueDate = selectedDateCalendar.timeInMillis

        if (reminderEnabled && dueDate < System.currentTimeMillis()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.past_date_warning)
                .setPositiveButton(R.string.yes) { _, _ -> persistTask(title, description, tags, reminderEnabled, dueDate) }
                .setNegativeButton(R.string.cancel, null)
                .show()
        } else {
            persistTask(title, description, tags, reminderEnabled, dueDate)
        }
    }

    private fun persistTask(
        title: String,
        description: String,
        tags: String,
        reminderEnabled: Boolean,
        dueDate: Long
    ) {
        if (isEditing) {
            taskViewModel.allTasks.value?.firstOrNull { it.id == taskId }?.let { existing ->
                val updated = existing.copy(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    category = selectedCategory,
                    priority = selectedPriority,
                    reminderEnabled = reminderEnabled,
                    repeatInterval = selectedRepeat,
                    tags = tags
                )
                taskViewModel.update(updated)
                if (reminderEnabled) {
                    alarmHelper.setAlarm(
                        taskId, title, description, dueDate, selectedRepeat
                    )
                } else {
                    alarmHelper.cancelAlarm(taskId)
                }
                Toast.makeText(this, R.string.task_updated, Toast.LENGTH_SHORT).show()
            }
        } else {
            taskViewModel.insert(
                Task(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    category = selectedCategory,
                    priority = selectedPriority,
                    reminderEnabled = reminderEnabled,
                    repeatInterval = selectedRepeat,
                    tags = tags
                )
            ) { id ->
                if (reminderEnabled) {
                    alarmHelper.setAlarm(
                        id.toInt(), title, description, dueDate, selectedRepeat
                    )
                }
            }
            Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}