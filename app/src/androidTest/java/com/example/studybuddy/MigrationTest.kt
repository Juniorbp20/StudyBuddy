package com.example.studybuddy

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.studybuddy.model.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        "com.example.studybuddy.model.AppDatabase"
    )

    @Test
    fun migrate1To2KeepsDataAndUsesKeys() {
        createRawDatabase(1, TEST_DB_1_2) { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "time TEXT NOT NULL, " +
                    "is_completed INTEGER NOT NULL DEFAULT 0)"
            )
            db.execSQL(
                "INSERT INTO tasks (title, description, date, time, is_completed) " +
                    "VALUES ('Estudiar', 'Capítulo 3', '2026-08-16', '18:00', 0)"
            )
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB_1_2, 3, true, AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3
        )

        db.query("SELECT * FROM tasks").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.count)
            assertEquals("Estudiar", cursor.getString(cursor.getColumnIndexOrThrow("title")))
            assertEquals("GENERAL", cursor.getString(cursor.getColumnIndexOrThrow("category")))
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("priority")))
            assertTrue(cursor.getLong(cursor.getColumnIndexOrThrow("due_date")) > 0)
        }
        db.close()
    }

    @Test
    fun migrate2To3ConvertsCategoriesAndCreatesSubtasks() {
        createRawDatabase(2, TEST_DB_2_3) { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS tasks (" +
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
                "INSERT INTO tasks (title, description, category, priority, due_date) " +
                    "VALUES ('Leer', 'Cap 2', 'Estudio', 2, 1786800000000)"
            )
        }

        val db = helper.runMigrationsAndValidate(TEST_DB_2_3, 3, true, AppDatabase.MIGRATION_2_3)
        db.execSQL("PRAGMA foreign_keys = ON")

        db.query("SELECT category, repeat_interval, tags FROM tasks").use { cursor ->
            cursor.moveToFirst()
            assertEquals("STUDY", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
            assertEquals("", cursor.getString(2))
        }
        db.execSQL("INSERT INTO subtasks (task_id, title, is_completed) VALUES (1, 'Parte 1', 0)")
        db.query("SELECT COUNT(*) FROM subtasks").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1L, cursor.getLong(0))
        }
        db.execSQL("DELETE FROM tasks WHERE id = 1")
        db.query("SELECT COUNT(*) FROM subtasks").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0L, cursor.getLong(0))
        }
        db.close()
    }

    private fun createRawDatabase(version: Int, name: String, create: (SupportSQLiteDatabase) -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(name)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    create(db)
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                }
            })
            .build()
        FrameworkSQLiteOpenHelperFactory().create(configuration).writableDatabase.close()
    }

    companion object {
        private const val TEST_DB_1_2 = "migration-test-1-2"
        private const val TEST_DB_2_3 = "migration-test-2-3"
    }
}