package com.example.studybuddy; // Asegúrate de que este sea tu package name

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;

import model.Task;
import viewmodel.TaskViewModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_TASK_DESCRIPTION = "extra_task_description";
    public static final String EXTRA_TASK_DATE = "extra_task_date";
    public static final String EXTRA_TASK_TIME = "extra_task_time";
    private TextInputEditText editTextTitle, editTextDescription;
    private Button buttonSelectDate, buttonSelectTime, buttonSaveTask;
    private TextView textViewSelectedDate, textViewSelectedTime, textViewAddTitle;


    private TaskViewModel taskViewModel;

    private Calendar selectedDateCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private int taskId = -1;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initViews();
        setupViewModel();
        setupClickListeners();
        checkForEditIntent(); // Verificar si es edición o nueva tarea
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        textViewSelectedDate = findViewById(R.id.text_view_selected_date);
        textViewSelectedTime = findViewById(R.id.text_view_selected_time);
        buttonSelectDate = findViewById(R.id.button_select_date);
        buttonSelectTime = findViewById(R.id.button_select_time);
        buttonSaveTask = findViewById(R.id.button_save_task);
        textViewAddTitle = findViewById(R.id.text_view_add_title);

    }

    private void setupViewModel() {
        // Asegúrate de que TaskViewModel esté correctamente definido y en el paquete correcto
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
    }

    private void setupClickListeners() {
        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog());
        buttonSelectTime.setOnClickListener(v -> showTimePickerDialog());
        buttonSaveTask.setOnClickListener(v -> saveTask());
    }

    private void checkForEditIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_TASK_ID)) {
            // Es una edición
            isEditing = true;
            taskId = intent.getIntExtra(EXTRA_TASK_ID, -1);
            String title = intent.getStringExtra(EXTRA_TASK_TITLE);
            String description = intent.getStringExtra(EXTRA_TASK_DESCRIPTION);
            String date = intent.getStringExtra(EXTRA_TASK_DATE);
            String time = intent.getStringExtra(EXTRA_TASK_TIME);

            editTextTitle.setText(title != null ? title : "");
            editTextDescription.setText(description != null ? description : "");

            // Parsear fecha y hora para el Calendar
            try {
                String dateTimeString = date + " " + time;
                SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date parsedDate = fullFormat.parse(dateTimeString);
                if (parsedDate != null) {
                    selectedDateCalendar = Calendar.getInstance();
                    selectedDateCalendar.setTime(parsedDate);
                } else {
                    selectedDateCalendar = Calendar.getInstance();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                selectedDateCalendar = Calendar.getInstance();
            }
            updateDateAndTimeViews();
            buttonSaveTask.setText("Actualizar Tarea");
            TextView textViewAddTitle = findViewById(R.id.text_view_add_title);
            textViewAddTitle.setText("Editar Tarea");
        } else {
            // Es una nueva tarea
            isEditing = false;
            taskId = -1;
            selectedDateCalendar = Calendar.getInstance(); // *** Corregido ***
            updateDateAndTimeViews();
            buttonSaveTask.setText("Guardar Tarea");
            TextView textViewAddTitle = findViewById(R.id.text_view_add_title);
            textViewAddTitle.setText("Agregar Tarea");
        }
    }

    private void showDatePickerDialog() {
        if (selectedDateCalendar == null) selectedDateCalendar = Calendar.getInstance();
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
        if (selectedDateCalendar == null) selectedDateCalendar = Calendar.getInstance();
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
        if (selectedDateCalendar != null) {
            textViewSelectedDate.setText(dateFormat.format(selectedDateCalendar.getTime()));
            textViewSelectedTime.setText(timeFormat.format(selectedDateCalendar.getTime()));
        } else {
            textViewSelectedDate.setText("Seleccionar Fecha");
            textViewSelectedTime.setText("Seleccionar Hora");
        }
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

        if (isEditing) {
            // Actualizar tarea existente
            // Creamos un objeto Task con el ID existente
            Task taskToUpdate = new Task(title, description, date, time, false); // isCompleted se maneja por separado
            taskToUpdate.setId(taskId); // *** Muy importante ***
            taskViewModel.update(taskToUpdate);
            Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
        } else {
            // Crear nueva tarea
            Task newTask = new Task(title, description, date, time, false);
            taskViewModel.insert(newTask);
            Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show();
        }

        // Finalizar la actividad para volver a MainActivity
        finish();
    }
}