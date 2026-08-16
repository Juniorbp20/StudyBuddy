package com.example.studybuddy.util

import android.content.Context
import android.net.Uri
import com.example.studybuddy.model.Task
import org.json.JSONArray
import org.json.JSONObject

object ExportImportHelper {

    private const val MIME_TYPE = "application/json"

    fun exportToJson(tasks: List<Task>): String {
        val array = JSONArray()
        tasks.forEach { task ->
            val obj = JSONObject()
                .put("id", task.id)
                .put("title", task.title)
                .put("description", task.description)
                .put("dueDate", task.dueDate)
                .put("isCompleted", task.isCompleted)
                .put("category", task.category)
                .put("priority", task.priority)
                .put("reminderEnabled", task.reminderEnabled)
                .put("createdAt", task.createdAt)
                .put("repeatInterval", task.repeatInterval)
                .put("tags", task.tags)
            array.put(obj)
        }
        return JSONObject().put("tasks", array).toString(2)
    }

    fun importFromJson(json: String): List<Task> {
        val root = JSONObject(json)
        val array = root.getJSONArray("tasks")
        val tasks = mutableListOf<Task>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            tasks.add(
                Task(
                    id = 0,
                    title = obj.optString("title", ""),
                    description = obj.optString("description", ""),
                    dueDate = obj.optLong("dueDate", 0L),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    category = obj.optString("category", "GENERAL"),
                    priority = obj.optInt("priority", 1),
                    reminderEnabled = obj.optBoolean("reminderEnabled", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    completedAt = obj.optLong("completedAt", 0L),
                    repeatInterval = obj.optInt("repeatInterval", 0),
                    tags = obj.optString("tags", "")
                )
            )
        }
        return tasks.filter { it.title.isNotBlank() }
    }

    fun exportToCsv(tasks: List<Task>): String {
        val header = "title,description,dueDate,isCompleted,category,priority,reminderEnabled,tags"
        val rows = tasks.joinToString("\n") { task ->
            listOf(
                escapeCsv(task.title),
                escapeCsv(task.description),
                task.dueDate.toString(),
                task.isCompleted.toString(),
                task.category,
                task.priority.toString(),
                task.reminderEnabled.toString(),
                escapeCsv(task.tags)
            ).joinToString(",")
        }
        return "$header\n$rows"
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(',') || escaped.contains('"') || escaped.contains('\n')) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    fun writeToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray())
            } != null
        } catch (e: Exception) {
            false
        }
    }

    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun defaultExportFileName(): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
            .format(java.util.Date())
        return "studybuddy_tasks_$timestamp.json"
    }
}