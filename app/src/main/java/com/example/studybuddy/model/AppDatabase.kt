package com.example.studybuddy.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Task::class, SubTask::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subTaskDao(): SubTaskDao

    companion object {
        private const val DB_NAME = "task_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS tasks_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "due_date INTEGER NOT NULL DEFAULT 0, " +
                        "is_completed INTEGER NOT NULL DEFAULT 0, " +
                        "category TEXT NOT NULL DEFAULT 'GENERAL', " +
                        "priority INTEGER NOT NULL DEFAULT 1, " +
                        "reminder_enabled INTEGER NOT NULL DEFAULT 0, " +
                        "created_at INTEGER NOT NULL DEFAULT 0, " +
                        "completed_at INTEGER NOT NULL DEFAULT 0)"
                )
                db.execSQL(
                    "INSERT INTO tasks_new (id, title, description, due_date, is_completed, " +
                        "category, priority, reminder_enabled, created_at, completed_at) " +
                        "SELECT id, COALESCE(title, ''), COALESCE(description, ''), " +
                        "COALESCE(strftime('%s', date || ' ' || time), 0), is_completed, " +
                        "'GENERAL', 1, 0, strftime('%s', 'now'), 0 " +
                        "FROM tasks"
                )
                db.execSQL("DROP TABLE tasks")
                db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN repeat_interval INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN tags TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "UPDATE tasks SET category = 'STUDY' WHERE category = 'Estudio'"
                )
                db.execSQL(
                    "UPDATE tasks SET category = 'WORK' WHERE category = 'Trabajo'"
                )
                db.execSQL(
                    "UPDATE tasks SET category = 'PERSONAL' WHERE category = 'Personal'"
                )
                db.execSQL(
                    "UPDATE tasks SET category = 'GENERAL' WHERE category = 'General'"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS subtasks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "task_id INTEGER NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "is_completed INTEGER NOT NULL, " +
                        "FOREIGN KEY(task_id) REFERENCES tasks(id) ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_subtasks_task_id ON subtasks(task_id)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}