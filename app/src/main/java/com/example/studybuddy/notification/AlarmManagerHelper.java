package com.example.studybuddy.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

import adapter.TaskAdapter;

public class AlarmManagerHelper {

    private Context context;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    public AlarmManagerHelper(TaskAdapter.OnItemClickListener context) {
        this.context = (Context) context;
        this.alarmManager = (AlarmManager) ((Context) context).getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Programa una alarma para una tarea específica.
     * @param taskId ID de la tarea (opcional, pero útil para identificarla)
     * @param taskTitle Título de la tarea
     * @param taskDescription Descripción de la tarea
     * @param year Año de la fecha de vencimiento
     * @param month Mes de la fecha de vencimiento (0-11)
     * @param day Día de la fecha de vencimiento
     * @param hour Hora de la alarma
     * @param minute Minuto de la alarma
     */
    public void setAlarm(int taskId, String taskTitle, String taskDescription, int year, int month, int day, int hour, int minute) {
        // Crear un Intent para el BroadcastReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("task_title", taskTitle);
        intent.putExtra("task_description", taskDescription);

        // Crear un PendingIntent con una acción única para esta alarma
        pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId, // Usar el ID de la tarea como request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Configurar la fecha y hora de la alarma
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);

        // Programar la alarma
        // y manejando posibles excepciones de seguridad.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, // Usar RTC_WAKEUP para despertar el dispositivo
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            Log.d("AlarmManagerHelper", "Alarma programada para: " + calendar.getTime());
        } catch (SecurityException se) {
            Log.e("AlarmManagerHelper", "Error de seguridad al programar la alarma: " + se.getMessage());
            // Considera notificar al usuario que necesita otorgar permisos para alarmas exactas
        }
    }

    /**
     * Cancela una alarma previamente programada.
     * @param taskId ID de la tarea para cancelar su alarma
     */
    public void cancelAlarm(int taskId) {
        // Recrear el Intent y PendingIntent exactamente como se creó al programar la alarma
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntentToCancel = PendingIntent.getBroadcast(
                context,
                taskId, // Usar el mismo request code (ID de la tarea)
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE); // FLAG_NO_CREATE para verificar si existe

        if (pendingIntentToCancel != null && alarmManager != null) {
            alarmManager.cancel(pendingIntentToCancel);
            pendingIntentToCancel.cancel(); // También cancelar el PendingIntent
            Log.d("AlarmManagerHelper", "Alarma cancelada para la tarea ID: " + taskId);
        }
    }
}