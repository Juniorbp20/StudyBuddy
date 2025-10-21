package com.example.studybuddy; // Asegúrate de que este sea tu package name

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;

import com.example.studybuddy.notification.AlarmManagerHelper; // *** Importación para la alarma ***

import adapter.TaskAdapter;
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
    public static final String EXTRA_TASK_COMPLETED = "extra_task_complete";
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

        // *** VERIFICAR SI LA FECHA Y HORA ESTÁN VACÍAS ***
        if (date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Por favor, seleccione una fecha y hora válidas", Toast.LENGTH_SHORT).show();
            return; // No continuar si falta fecha/hora
        }

        // *** INICIO: LÓGICA PARA DETERMINAR SI ES EDICIÓN O NUEVA TAREA ***
        if (isEditing) {
            // *** ESCENARIO: Editar tarea existente ***
            Task taskToUpdate = new Task(title, description, date, time, false);
            taskToUpdate.setId(taskId);
            taskViewModel.update(taskToUpdate);

            // *** ALARMA: Reprogramar la alarma para la tarea actualizada ***
            // Llamamos al método auxiliar
            scheduleAlarmForTask(taskToUpdate, date, time);

            Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
        } else {
            // *** ESCENARIO: Crear nueva tarea ***
            Task newTask = new Task(title, description, date, time, false);
            taskViewModel.insert(newTask);

            // *** ALARMA: Programar la alarma para la nueva tarea ***
            // Llamamos al método auxiliar
            scheduleAlarmForTask(newTask, date, time);

            Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show();
        }
        // *** FIN: LÓGICA DE GUARDADO Y ALARMA ***

        finish();
    }

    // *** MÉTODO AUXILIAR PARA PROGRAMAR LA ALARMA ***
    /**
     * Programa una alarma para una tarea específica usando AlarmManagerHelper.
     * @param task El objeto Task (debe tener un ID asignado si es nueva, o el ID existente si es editada).
     * @param dateString La fecha en formato String (esperado "yyyy-MM-dd").
     * @param timeString La hora en formato String (esperado "HH:mm").
     */
    private void scheduleAlarmForTask(Task task, String dateString, String timeString) {
        try {
            // *** PARSEAR FECHA Y HORA ***
            // Asumiendo formato "yyyy-MM-dd" para date y "HH:mm" para time
            String[] dateParts = dateString.split("-");
            int year = Integer.parseInt(dateParts[0]);
            // ¡OJO! Los meses en Calendar van de 0 (Enero) a 11 (Diciembre)
            int month = Integer.parseInt(dateParts[1]) - 1;
            int day = Integer.parseInt(dateParts[2]);

            String[] timeParts = timeString.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Validar rangos razonables
            if (year < 2000 || year > 2100 || month < 0 || month > 11 || day < 1 || day > 31 ||
                    hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new IllegalArgumentException("Fecha/Hora fuera de rango válido");
            }

            // *** USO DE ALARM MANAGER HELPER PARA PROGRAMAR LA ALARMA ***
            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper((TaskAdapter.OnItemClickListener) AddTaskActivity.this);
            alarmManagerHelper.setAlarm(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    year,
                    month,
                    day,
                    hour,
                    minute
            );

        } catch (NumberFormatException e) {
            // Manejar errores de parsing (formato incorrecto, valores no numéricos)
            Log.e("AddTaskActivity", "Error al parsear fecha/hora: " + e.getMessage());
            Toast.makeText(this, "Error al programar recordatorio: Fecha/Hora inválida", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            // Manejar errores de rango
            Log.e("AddTaskActivity", "Error al validar fecha/hora: " + e.getMessage());
            Toast.makeText(this, "Error al programar recordatorio: Fecha/Hora inválida", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Capturar otros errores inesperados
            Log.e("AddTaskActivity", "Error inesperado al programar alarma: " + e.getMessage());
            Toast.makeText(this, "Error al programar recordatorio: Fecha/Hora inválida", Toast.LENGTH_SHORT).show();
        }
    }
}