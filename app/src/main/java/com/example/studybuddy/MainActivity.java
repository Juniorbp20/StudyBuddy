package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                // Acción al hacer clic en una tarea (puede ser editarla, por ejemplo)
                // Por ahora, puedes dejarlo vacío o mostrar un Toast
            }

            @Override
            public void onTaskStatusChanged(Task task, boolean isCompleted) {
                // Actualizar el estado de completado de la tarea
                task.setCompleted(isCompleted);
                taskViewModel.update(task); // Guardar el cambio
            }
        });
    }
}