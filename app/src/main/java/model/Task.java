package model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "date") // Almacenar como String o usar Date/Long (requiere TypeConverter)
    private String date; // Formato sugerido: "yyyy-MM-dd"

    @ColumnInfo(name = "time") // Almacenar como String o usar Date/Long (requiere TypeConverter)
    private String time; // Formato sugerido: "HH:mm"

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    // Constructor vacío requerido por Room
    public Task() {
    }

    // Constructor completo (opcional pero útil)
    public Task(String title, String description, String date, String time, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.isCompleted = isCompleted;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}