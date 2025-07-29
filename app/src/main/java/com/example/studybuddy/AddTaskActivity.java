package com.example.studybuddy;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import model.Task;
import viewmodel.TaskViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText editTextTitle, editTextDescription;
    private TextView textViewSelectedDate, textViewSelectedTime;
    private Button buttonSelectDate, buttonSelectTime, buttonSaveTask;

    private TaskViewModel taskViewModel;

    private Calendar selectedDateCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initViews();
        setupViewModel();
        setupClickListeners();
        initializeCalendar();
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        textViewSelectedDate = findViewById(R.id.text_view_selected_date);
        textViewSelectedTime = findViewById(R.id.text_view_selected_time);
        buttonSelectDate = findViewById(R.id.button_select_date);
        buttonSelectTime = findViewById(R.id.button_select_time);
        buttonSaveTask = findViewById(R.id.button_save_task);
    }

    private void setupViewModel() {
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
    }

    private void setupClickListeners() {
        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog());
        buttonSelectTime.setOnClickListener(v -> showTimePickerDialog());
        buttonSaveTask.setOnClickListener(v -> saveTask());
    }

    private void initializeCalendar() {
        selectedDateCalendar = Calendar.getInstance();
        updateDateAndTimeViews();
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateCalendar.set(Calendar.YEAR, year);
                    selectedDateCalendar.set(Calendar.MONTH, month);
                    selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateAndTimeViews();
                },
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateCalendar.set(Calendar.MINUTE, minute);
                    updateDateAndTimeViews();
                },
                selectedDateCalendar.get(Calendar.HOUR_OF_DAY),
                selectedDateCalendar.get(Calendar.MINUTE),
                true // Formato 24 horas
        );
        timePickerDialog.show();
    }

    private void updateDateAndTimeViews() {
        textViewSelectedDate.setText(dateFormat.format(selectedDateCalendar.getTime()));
        textViewSelectedTime.setText(timeFormat.format(selectedDateCalendar.getTime()));
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String date = textViewSelectedDate.getText().toString();
        String time = textViewSelectedTime.getText().toString();

        if (title.isEmpty()) {
            editTextTitle.setError("El título es obligatorio");
            editTextTitle.requestFocus();
            return;
        }

        // Crear objeto Task
        Task task = new Task(title, description, date, time, false);

        // Guardar usando ViewModel
        taskViewModel.insert(task);

        Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show();

        // Finalizar la actividad para volver a com.example.studybuddy.MainActivity
        finish();
    }
}