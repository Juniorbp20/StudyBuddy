package com.example.studybuddy.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.studybuddy.MainActivity;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarma activada");

        // Obtener los datos de la tarea desde el Intent (debe haber sido establecido por AlarmManagerHelper)
        String taskTitle = intent.getStringExtra("task_title");
        String taskDescription = intent.getStringExtra("task_description");

        // Crear una instancia de NotificationHelper y mostrar la notificación
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.showTaskReminderNotification(
                "Recordatorio de Tarea",
                "Tienes una tarea pendiente: " + taskTitle + "\n" + taskDescription
        );
    }
}