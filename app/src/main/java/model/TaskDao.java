package model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    // Obtener todas las tareas ordenadas por ID
    @Query("SELECT * FROM tasks ORDER BY id ASC")
    LiveData<List<Task>> getAllTasks();

    // Obtener tareas no completadas ordenadas por fecha/hora (opcional para futuras mejoras)
    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY date ASC, time ASC")
    LiveData<List<Task>> getPendingTasks();
}