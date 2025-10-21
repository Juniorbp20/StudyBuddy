package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.notification.AlarmManagerHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import adapter.TaskAdapter;
import model.Task;
import viewmodel.TaskViewModel;


public class MainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Si el tamaño no cambia

        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        // Obtener ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Observar cambios en las tareas
        taskViewModel.getAllTasks().observe(this, tasks -> {
            // Actualizar el adaptador con la nueva lista
            adapter.setTasks(tasks);
        });

        // Configurar FloatingActionButton
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Configurar listener para el adaptador (opcional: manejar clics en items o cambios de estado)
        // Configurar listener para el adaptador
        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                // *** NUEVO: Acción al hacer clic en una tarea -> Editar ***
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putExtra(AddTaskActivity.EXTRA_TASK_ID, task.getId());
                intent.putExtra(AddTaskActivity.EXTRA_TASK_TITLE, task.getTitle());
                intent.putExtra(AddTaskActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
                intent.putExtra(AddTaskActivity.EXTRA_TASK_DATE, task.getDate());
                intent.putExtra(AddTaskActivity.EXTRA_TASK_TIME, task.getTime());
                intent.putExtra(AddTaskActivity.EXTRA_TASK_COMPLETED, task.isCompleted());
                startActivity(intent);
                }

            @Override
            public void onTaskStatusChanged(Task task, boolean isCompleted) {
                task.setCompleted(isCompleted);
                taskViewModel.update(task);
                // Opcional: Si una tarea completada no necesita recordatorio, cancelar alarma
                if (isCompleted) {
                    // *** USO DE ALARM MANAGER HELPER PARA CANCELAR ALARMA ***
                    AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper((TaskAdapter.OnItemClickListener) MainActivity.this);
                    alarmManagerHelper.cancelAlarm(task.getId());
                }
            }

            @Override
            public void onTaskDelete(Task task) {
                taskViewModel.delete(task);
                // *** USO DE ALARM MANAGER HELPER PARA CANCELAR ALARMA CUANDO SE ELIMINA ***
                AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper((TaskAdapter.OnItemClickListener) MainActivity.this);
                alarmManagerHelper.cancelAlarm(task.getId());
                Toast.makeText(MainActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTaskEdit(Task task) {

            }
        });
    }
}